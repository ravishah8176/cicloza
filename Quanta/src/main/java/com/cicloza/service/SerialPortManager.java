package com.cicloza.service;

import com.cicloza.util.ColorLogger;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for managing serial port operations including opening, closing,
 * and listening for data from serial ports. Provides reactive streaming capabilities
 * for real-time serial data consumption.
 * 
 * <p>This service handles:
 * <ul>
 *   <li>Discovery of available serial ports</li>
 *   <li>Opening and configuring serial port connections</li>
 *   <li>Real-time data listening and processing</li>
 *   <li>Reactive streaming of processed serial data</li>
 *   <li>Proper cleanup and resource management</li>
 * </ul>
 * 
 * @author Cicloza Development Team
 * @since 1.0.0
 */
@Service
public class SerialPortManager {
    
    private static final ColorLogger logger = ColorLogger.getLogger(SerialPortManager.class);
    
    // Thread-safe collections for managing serial port state
    private final Map<String, SerialPort> openedPorts = new ConcurrentHashMap<>();
    private final StringBuilder dataBuffer = new StringBuilder();
    
    // Reactive publisher for streaming serial data to subscribers
    private final Sinks.Many<String> messageSink = Sinks.many().multicast().onBackpressureBuffer();
    
    // Constants for serial port configuration
    private static final String DELIMITER_REGEX = "[,\n\r]+$";
    private static final String COMMA_DELIMITER = ",";
    private static final String NEWLINE_DELIMITER = "\n";

    /**
     * Discovers and retrieves all available serial ports on the system.
     * 
     * <p>Each port is represented as a map containing both the system port name 
     * (e.g., "COM3", "/dev/ttyUSB0") and a human-readable descriptive name.
     * 
     * @return List of maps, each containing "systemPortName" and "descriptivePortName" keys.
     *         Returns empty list if no ports are found.
     */
    public List<Map<String, String>> getAvailablePorts() {
        logger.info("Scanning for available serial ports...");
        
        SerialPort[] systemPorts = SerialPort.getCommPorts();
        
        if (systemPorts.length == 0) {
            logger.warn("No serial ports found on the system");
            return List.of();
        }
        
        List<Map<String, String>> portList = Arrays.stream(systemPorts)
            .map(port -> Map.of(
                "systemPortName", port.getSystemPortName(),
                "descriptivePortName", port.getDescriptivePortName()
            ))
            .toList();
            
        logger.info("Found {} serial ports", portList.size());
        return portList;
    }

    /**
     * Opens a serial port with the specified communication parameters.
     * 
     * <p>The port is configured with:
     * <ul>
     *   <li>Specified baud rate and data bits</li>
     *   <li>One stop bit</li>
     *   <li>No parity</li>
     *   <li>Semi-blocking read timeout</li>
     * </ul>
     * 
     * @param portName The system name of the port to open (e.g., "COM3")
     * @param baudRate The communication speed in bits per second
     * @param dataBits The number of data bits (typically 7 or 8)
     * @return true if the port was opened successfully, false otherwise
     * @throws IllegalArgumentException if portName is null or empty
     */
    public boolean openPort(String portName, int baudRate, int dataBits) {
        if (portName == null || portName.trim().isEmpty()) {
            logger.error("Port name cannot be null or empty");
            return false;
        }
        
        logger.info("Attempting to open port: {} with baud rate: {}, data bits: {}", 
                   portName, baudRate, dataBits);
        
        try {
            SerialPort port = SerialPort.getCommPort(portName);
            
            // Configure port parameters
            port.setComPortParameters(baudRate, dataBits, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
            port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
            
            if (port.openPort()) {
                openedPorts.put(portName, port);
                logger.info("Port {} opened successfully", port.getSystemPortName());
                return true;
            } else {
                logger.error("Failed to open port {}. Error code: {}", 
                           port.getSystemPortName(), port.getLastErrorCode());
                return false;
            }
        } catch (Exception e) {
            logger.error("Exception occurred while opening port {}: {}", portName, e.getMessage());
            return false;
        }
    }

    /**
     * Closes the specified serial port and removes it from the managed port list.
     * 
     * <p>This method performs cleanup by:
     * <ul>
     *   <li>Removing any active data listeners</li>
     *   <li>Closing the physical port connection</li>
     *   <li>Removing the port from internal tracking</li>
     * </ul>
     * 
     * @param portName The name of the port to close
     * @return true if the port was closed successfully or was already closed, 
     *         false if the port was not found in the managed list
     */
    public boolean closePort(String portName) {
        logger.info("Attempting to close port: {}", portName);
        
        SerialPort port = openedPorts.get(portName);
        if (port == null) {
            logger.warn("Port {} is not currently managed by this service", portName);
            return false;
        }

        try {
            if (port.isOpen()) {
                // Proper cleanup sequence: remove listeners first, then close port
                port.removeDataListener();
                port.closePort();
                logger.info("Port {} closed successfully", portName);
            } else {
                logger.info("Port {} was already closed", portName);
            }
        } catch (Exception e) {
            logger.error("Error occurred while closing port {}: {}", portName, e.getMessage());
        } finally {
            // Always remove from tracking, regardless of close success
            openedPorts.remove(portName);
        }
        
        return true;
    }

    /**
     * Starts listening for incoming data on the specified serial port.
     * 
     * <p>This method sets up an asynchronous data listener that:
     * <ul>
     *   <li>Reads incoming data line by line</li>
     *   <li>Buffers partial messages until complete</li>
     *   <li>Processes messages using comma or newline delimiters</li>
     *   <li>Emits cleaned messages to reactive subscribers</li>
     * </ul>
     * 
     * @param portName The name of the port to listen on
     * @throws IllegalStateException if the port is not open
     */
    public void startListening(String portName) {
        SerialPort port = openedPorts.get(portName);
        if (port == null || !port.isOpen()) {
            String errorMsg = String.format("Cannot start listening: port %s is not open", portName);
            logger.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        logger.info("Starting data listener for port: {}", portName);
        
        try {
            port.addDataListener(createDataListener(port));
            logger.info("Data listener successfully started for port: {}", portName);
        } catch (Exception e) {
            logger.error("Failed to initialize data listener for port {}: {}", portName, e.getMessage());
            throw new RuntimeException("Failed to start listening on port: " + portName, e);
        }
    }

    /**
     * Provides a reactive stream of processed serial data messages.
     * 
     * <p>The returned Flux emits cleaned messages (without delimiters) as they
     * are received and processed from any managed serial port.
     * 
     * @return A Flux that emits String messages from serial ports
     */
    public Flux<String> getSerialDataFlux() {
        return messageSink.asFlux();
    }

    // ===== Private Helper Methods =====

    /**
     * Creates a data listener for the specified serial port.
     * 
     * @param port The serial port to create a listener for
     * @return A configured SerialPortDataListener
     */
    private SerialPortDataListener createDataListener(SerialPort port) {
        return new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                    processIncomingData(port);
                }
            }
        };
    }

    /**
     * Processes incoming data from a serial port.
     * 
     * <p>This method reads available data, buffers it, and processes complete messages
     * based on delimiter detection.
     * 
     * @param port The serial port to read data from
     */
    private void processIncomingData(SerialPort port) {
        try (InputStream inputStream = port.getInputStream();
             Scanner scanner = new Scanner(inputStream)) {
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                
                // Synchronize access to shared buffer
                synchronized (dataBuffer) {
                    dataBuffer.append(line).append(NEWLINE_DELIMITER);
                    String processedMessage = extractCompleteMessage();
                    
                    if (!processedMessage.isEmpty()) {
                        emitCleanedMessage(processedMessage);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("IO error reading from serial port {}: {}", port.getSystemPortName(), e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error processing data from port {}: {}", 
                        port.getSystemPortName(), e.getMessage());
        }
    }

    /**
     * Extracts a complete message from the data buffer based on delimiters.
     * 
     * <p>This method looks for comma or newline delimiters to identify complete messages
     * and removes them from the buffer once extracted.
     * 
     * @return The complete message with delimiters, or empty string if no complete message
     */
    private String extractCompleteMessage() {
        int commaIndex = dataBuffer.indexOf(COMMA_DELIMITER);
        int newlineIndex = dataBuffer.indexOf(NEWLINE_DELIMITER);
        
        int delimiterIndex = determineDelimiterIndex(commaIndex, newlineIndex);
        
        if (delimiterIndex == -1) {
            return ""; // No complete message available
        }
        
        // Extract message including delimiter
        String completeMessage = dataBuffer.substring(0, delimiterIndex + 1);
        dataBuffer.delete(0, delimiterIndex + 1);
        
        return completeMessage;
    }

    /**
     * Determines which delimiter appears first in the buffer.
     * 
     * @param commaIndex Index of first comma, or -1 if none
     * @param newlineIndex Index of first newline, or -1 if none
     * @return Index of the first delimiter, or -1 if neither exists
     */
    private int determineDelimiterIndex(int commaIndex, int newlineIndex) {
        if (commaIndex != -1 && newlineIndex != -1) {
            return Math.min(commaIndex, newlineIndex);
        } else if (commaIndex != -1) {
            return commaIndex;
        } else if (newlineIndex != -1) {
            return newlineIndex;
        }
        return -1;
    }

    /**
     * Cleans and emits a processed message to reactive subscribers.
     * 
     * <p>This method removes trailing delimiters and emits the cleaned message
     * to the reactive stream if it's not empty after cleaning.
     * 
     * @param rawMessage The raw message with potential trailing delimiters
     */
    private void emitCleanedMessage(String rawMessage) {
        String cleanedMessage = rawMessage.replaceAll(DELIMITER_REGEX, "");
        
        if (!cleanedMessage.isEmpty()) {
            messageSink.tryEmitNext(cleanedMessage);
            // Uncomment for debugging: logger.debug("Emitted cleaned message: {}", cleanedMessage);
        }
    }
}