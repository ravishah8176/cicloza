package com.cicloza.controller;

import com.cicloza.dto.ClosePortRequestDTO;
import com.cicloza.dto.OpenPortRequestDTO;
import com.cicloza.service.SerialPortManager;
import com.cicloza.util.ColorLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing serial port operations and real-time data streaming.
 * 
 * <p>This controller provides endpoints for:
 * <ul>
 *   <li>Discovering available serial ports on the system</li>
 *   <li>Opening serial ports and streaming real-time data via Server-Sent Events</li>
 *   <li>Closing active serial port connections</li>
 * </ul>
 * 
 * <p>All endpoints follow REST conventions and provide appropriate HTTP status codes
 * for different scenarios (success, client errors, server errors).
 * 
 * @author Cicloza Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/serial")
public class SerialPortController {
    
    private static final ColorLogger logger = ColorLogger.getLogger(SerialPortController.class);
    
    private final SerialPortManager serialPortManager;

    /**
     * Constructs a new SerialPortController with the required dependencies.
     * 
     * @param serialPortManager The service responsible for serial port operations
     */
    public SerialPortController(SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
        logger.info("SerialPortController initialized successfully");
    }

    /**
     * Retrieves a list of all available serial ports on the system.
     * 
     * <p>This endpoint scans the system for available serial ports and returns
     * detailed information about each port including both system names and
     * human-readable descriptions.
     * 
     * <p><strong>Example Response:</strong>
     * <pre>
     * [
     *   {
     *     "systemPortName": "COM3",
     *     "descriptivePortName": "USB Serial Port (COM3)"
     *   },
     *   {
     *     "systemPortName": "COM4", 
     *     "descriptivePortName": "Arduino Uno (COM4)"
     *   }
     * ]
     * </pre>
     * 
     * @return ResponseEntity containing a list of available ports with their details
     */
    @GetMapping("/ports")
    public ResponseEntity<List<Map<String, String>>> getAvailablePorts() {
        logger.info("Received request to scan for available serial ports");
        
        try {
            List<Map<String, String>> availablePorts = serialPortManager.getAvailablePorts();
            
            logger.info("Successfully retrieved {} available ports", availablePorts.size());
            return ResponseEntity.ok(availablePorts);
            
        } catch (Exception e) {
            logger.error("Error occurred while scanning for serial ports: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Opens a serial port and immediately begins streaming real-time data via Server-Sent Events.
     * 
     * <p>This endpoint performs the following operations atomically:
     * <ol>
     *   <li>Opens the specified serial port with the provided configuration</li>
     *   <li>Starts listening for incoming data on the port</li>
     *   <li>Establishes a Server-Sent Events stream for real-time data transmission</li>
     * </ol>
     * 
     * <p>The response is a continuous stream of Server-Sent Events with the following format:
     * <pre>
     * event: serialData
     * id: 1642123456789
     * data: [actual serial data]
     * 
     * </pre>
     * 
     * <p><strong>Request Body Example:</strong>
     * <pre>
     * {
     *   "portName": "COM3",
     *   "baudRate": 9600,
     *   "dataBits": 8
     * }
     * </pre>
     * 
     * @param request The port configuration including name, baud rate, and data bits
     * @return ResponseEntity with a Flux stream of Server-Sent Events containing serial data
     *         Returns 400 Bad Request if the port cannot be opened
     */
    @PostMapping(value = "/openAndStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<ServerSentEvent<String>>> openPortAndStream(
            @Valid @RequestBody OpenPortRequestDTO request) {
        
        logger.info("Received request to open port '{}' with configuration: baudRate={}, dataBits={}", 
                   request.getPortName(), request.getBaudRate(), request.getDataBits());

        try {
            // Attempt to open the serial port with specified parameters
            boolean portOpened = serialPortManager.openPort(
                request.getPortName(), 
                request.getBaudRate(), 
                request.getDataBits()
            );

            if (!portOpened) {
                logger.warn("Failed to open serial port: {}", request.getPortName());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // Start the data listener for real-time processing
            serialPortManager.startListening(request.getPortName());
            logger.info("Successfully started data streaming for port: {}", request.getPortName());

            // Create the reactive stream of Server-Sent Events
            Flux<ServerSentEvent<String>> eventStream = createSerialDataEventStream();

            return ResponseEntity.ok(eventStream);
            
        } catch (IllegalStateException e) {
            logger.error("Invalid state while opening port {}: {}", request.getPortName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Unexpected error while opening port {}: {}", request.getPortName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Closes an active serial port connection.
     * 
     * <p>This endpoint safely closes the specified serial port, ensuring proper
     * cleanup of resources including data listeners and the physical connection.
     * 
     * <p><strong>Request Body Example:</strong>
     * <pre>
     * {
     *   "portName": "COM3"
     * }
     * </pre>
     * 
     * @param request The request containing the name of the port to close
     * @return ResponseEntity with a success message
     *         Returns 200 OK with confirmation message regardless of initial port state
     */
    @PostMapping("/closePort")
    public ResponseEntity<String> closePort(@Valid @RequestBody ClosePortRequestDTO request) {
        logger.info("Received request to close port: {}", request.getPortName());
        
        try {
            boolean closed = serialPortManager.closePort(request.getPortName());
            
            if (closed) {
                logger.info("Successfully closed port: {}", request.getPortName());
            } else {
                logger.info("Port {} was not currently managed - no action taken", request.getPortName());
            }
            
            return ResponseEntity.ok(
                String.format("Port %s closed successfully", request.getPortName())
            );
            
        } catch (Exception e) {
            logger.error("Error occurred while closing port {}: {}", request.getPortName(), e.getMessage());
            return ResponseEntity.ok(
                String.format("Port %s close attempted - %s", request.getPortName(), e.getMessage())
            );
        }
    }

    // ===== Private Helper Methods =====

    /**
     * Creates a reactive stream of Server-Sent Events from serial data.
     * 
     * <p>This method transforms the raw serial data flux into properly formatted
     * Server-Sent Events with unique IDs, event types, and data payload.
     * 
     * @return A Flux of ServerSentEvent objects containing serial data
     */
    private Flux<ServerSentEvent<String>> createSerialDataEventStream() {
        return serialPortManager.getSerialDataFlux()
            .map(this::createServerSentEvent)
            .doOnNext(event -> logger.debug("Emitting SSE event with ID: {}", event.id()))
            .doOnError(error -> logger.error("Error in serial data stream: {}", error.getMessage()));
    }

    /**
     * Creates a single Server-Sent Event from serial data.
     * 
     * @param data The serial data to wrap in an SSE event
     * @return A properly formatted ServerSentEvent
     */
    private ServerSentEvent<String> createServerSentEvent(String data) {
        return ServerSentEvent.<String>builder()
            .id(String.valueOf(System.currentTimeMillis()))
            .event("serialData")
            .data(data)
            .build();
    }
}