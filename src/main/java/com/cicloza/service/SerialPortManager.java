package com.cicloza.service;

import com.cicloza.util.ColorLogger;
import com.fazecast.jSerialComm.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Service
public class SerialPortManager {
    private static final ColorLogger logger = ColorLogger.getLogger(SerialPortManager.class);
    private static final StringBuilder dataBuffer = new StringBuilder();
    private static final Map<String, SerialPort> openedPorts = new HashMap<>();
    private static final List<SerialPort> systemPorts = new ArrayList<>();

    /**
     * Get a list of available serial ports on the system
     * @return List of strings containing port names and descriptions
     */
    public List<String> getAvailablePorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        if (ports.length == 0) {
            logger.warn("No serial ports found.");
            return List.of();
        }

        logger.info("Serial ports found: {}", ports.length);
        for (SerialPort port : ports) {
            systemPorts.add(port);
        }
        return Arrays.stream(ports)
                     .map(port -> port.getSystemPortName() + " - " + port.getDescriptivePortName())
                     .toList();
    }

    /**
     * Find a serial port by its system name.
     * This method now checks the internal map first.
     * @param portName The name of the port to search for
     * @return SerialPort object if found, null otherwise
     */
    public SerialPort findPortByName(String portName) {
        logger.info("Finding port by name: {}", portName);
        if (openedPorts.containsKey(portName)) {
            SerialPort port = openedPorts.get(portName);
            logger.info("Port {} found in cache.", portName);
            return port;
        }

        return systemPorts.stream()
                     .filter(p -> p.getSystemPortName().equals(portName))
                     .findFirst()
                     .map(p -> {
                         logger.info("Port {} found on system, not yet in cache or not opened via this manager.", portName);
                         return p;
                     })
                     .orElse(null);
    }

    /**
     * Open the specified serial port with default parameters
     * @param portName The SerialPort name to open
     * @param baudRate The baud rate to use
     * @param dataBits The data bits to use
     * @return true if the port was opened successfully, false otherwise
     */
    public boolean openPort(String  portName, int baudRate, int dataBits) {
        logger.info("Attempting to open port: {}", portName);
        SerialPort port = findPortByName(portName);

        if (port == null) {
            port = systemPorts.stream()
                         .filter(p -> p.getSystemPortName().equals(portName))
                         .findFirst()
                         .orElse(null);
            if (port == null) {
                logger.error("Port {} not found on system.", portName);
                return false;
            }
        }

        port.setComPortParameters(baudRate, dataBits, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        if (port.openPort()) {
            logger.info("Port {} opened successfully.", port.getSystemPortName());
            openedPorts.put(portName, port);
            return true;
        } else {
            logger.error("Failed to open port {}. Last Error: {}", port.getSystemPortName(), port.getLastErrorCode());
            return false;
        }
    }

    /**
     * Close the specified serial port
     * @param portName The name of the port to close
     * @return true if the port was closed successfully or already closed, false if port not found.
     */
    public boolean closePort(String portName) {
        SerialPort port = findPortByName(portName);
        if (port == null) {
            logger.warn("Port {} not found, cannot close.", portName);
            return false;
        }

        if (port.isOpen()) {
            if (port.closePort()) {
                logger.info("Port {} closed successfully.", portName);
                openedPorts.remove(portName);
            } else {
                logger.error("Failed to close port {}. Last Error: {}", portName, port.getLastErrorCode());
                return false;
            }
        } else {
            logger.warn("Port {} is already closed.", portName);
            openedPorts.remove(portName);
        }
        return true;
    }

    /**
     * Check if the specified serial port is open using our managed list.
     * @param portName The name of the port to check
     * @return true if the port is managed by us and is open, false otherwise
     */
    public boolean isPortOpen(String portName) {
        logger.info("Checking if port {} is open (managed list check)", portName);
        SerialPort port = openedPorts.get(portName);
        if (port != null) {
            if (port.isOpen()) {
                logger.info("Managed port {} is open.", portName);
                return true;
            } else {
                logger.warn("Managed port {} is not open. Removing from active list.", portName);
                openedPorts.remove(portName);
                return false;
            }
        }
        logger.info("Port {} is not in the managed list of opened ports or was found to be closed.", portName);
        return false;
    }

    /**
     * Start listening for data on the serial port
     * This method sets up a listener that reads data continuously
     */
    public void startListening(String portName) {
        SerialPort port = findPortByName(portName);
        if (port == null || !port.isOpen()) {
            logger.error("Serial port {} is not open or not found. Cannot start listening.", portName);
            return;
        }
        SerialPort managedPort = openedPorts.get(portName);
        if (managedPort == null || !managedPort.isOpen()) {
             logger.error("Managed serial port {} is not available or not open. Cannot start listening.", portName);
            return;
        }

        try {
            managedPort.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                }

                @Override
                public void serialEvent(SerialPortEvent event) {
                    if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                        try (InputStream inputStream = managedPort.getInputStream();
                             Scanner scanner = new Scanner(inputStream)) {

                            while (scanner.hasNextLine()) {
                                String line = scanner.nextLine();
                                dataBuffer.append(line).append("\n");
                                processCompleteMessages();
                            }
                        } catch (IOException e) {
                            logger.error("Error reading from serial port {}: {}", managedPort.getSystemPortName(), e.getMessage());
                        } catch (Exception ex) {
                            logger.error("Unexpected error during serial event for port {}: {}", managedPort.getSystemPortName(), ex.getMessage());
                        }
                    }
                }
            });
            logger.info("Successfully started listener for port {}", portName);
        } catch (Exception e) {
            logger.error("Error starting listener for port {}: {}", portName, e.getMessage());
        }
    }

    public void stopListening(String portName) {
        SerialPort port = openedPorts.get(portName);
        if (port != null && port.isOpen()) {
            port.removeDataListener();
            logger.info("Stopped listening on port: {}", port.getSystemPortName());
        } else {
            logger.warn("Port {} not found in managed list or not open. No active listener to stop.", portName);
        }
    }

    /**
     * Process complete messages from the buffer
     * Looks for message delimiters like comma or newline
     */
    private static String processCompleteMessages() {
        int commaIndex;
        int newlineIndex;
        String newMessage = "";

        while (true) {
            commaIndex = dataBuffer.indexOf(",");
            newlineIndex = dataBuffer.indexOf("\n");

            int endIndex = -1;

            if (commaIndex != -1 && newlineIndex != -1) {
                endIndex = Math.min(commaIndex, newlineIndex);
            } else if (commaIndex != -1) {
                endIndex = commaIndex;
            } else if (newlineIndex != -1) {
                endIndex = newlineIndex;
            } else {
                break;
            }

            newMessage = dataBuffer.substring(0, endIndex + 1);
            logger.info("Received complete message: {}", newMessage.trim());

            dataBuffer.delete(0, endIndex + 1);
        }
        return newMessage;
    }
}
