package com.cicloza.controller;

import com.cicloza.service.SerialPortManager;
import com.cicloza.util.ColorLogger;
import com.cicloza.dto.BaseRequestDTO;
import com.cicloza.dto.OpenPortResponseDTO;
import com.cicloza.dto.FindPortResponseDTO;
import com.cicloza.dto.IsPortOpenResponseDTO;
import com.cicloza.dto.OpenPortRequestDTO;
import com.fazecast.jSerialComm.SerialPort;
import reactor.core.publisher.Flux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/serial")
public class SerialPortController {
    private static final ColorLogger logger = ColorLogger.getLogger(SerialPortController.class);
    private final SerialPortManager serialPortManager;
    private ExecutorService nonBlockingService = Executors
      .newCachedThreadPool();

    @Autowired
    public SerialPortController(SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
        logger.info("SerialPortController initialized.");
    }

    @GetMapping("/ports")
    public ResponseEntity<List<String>> getAvailablePorts() {
        logger.info("Scanning for available serial ports...");
        List<String> ports = serialPortManager.getAvailablePorts();
        logger.info("Found {} ports", ports.size());
        return ResponseEntity.ok(ports);
    }

    @PostMapping("/findPort")
    public ResponseEntity<FindPortResponseDTO> findPortPost(@RequestBody BaseRequestDTO baseRequestDTO) {
        logger.info("Searching for port: {}", baseRequestDTO.getPortName());
        FindPortResponseDTO findPortResponseDTO = new FindPortResponseDTO();
        SerialPort port = serialPortManager.findPortByName(baseRequestDTO.getPortName());
        if (port != null) {
            logger.info("Port found: {}", port);
            findPortResponseDTO.setFindPortResponse(true, baseRequestDTO.getPortName() + " found");
            return ResponseEntity.ok(findPortResponseDTO);
        } else {
            logger.warn("Port not found: {}", baseRequestDTO.getPortName());
            findPortResponseDTO.setFindPortResponse(false, baseRequestDTO.getPortName() + " not found");
            return ResponseEntity.ok(findPortResponseDTO);
        }
    }

    @PostMapping("/openPort")
    public ResponseEntity<OpenPortResponseDTO> openPortPost(@RequestBody OpenPortRequestDTO openPortRequestDTO) {
        logger.info("Opening port: {}", openPortRequestDTO.getPortName());
        OpenPortResponseDTO openPortResponseDTO = new OpenPortResponseDTO();
        boolean portAlreadyOpen = serialPortManager.isPortOpen(openPortRequestDTO.getPortName());
        if (portAlreadyOpen) {
            openPortResponseDTO.setOpenPortResponse(true, "Port " + openPortRequestDTO.getPortName() + " already open");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(openPortResponseDTO);
        }
        boolean portOpened = serialPortManager.openPort(openPortRequestDTO.getPortName(), openPortRequestDTO.getBaudRate(), openPortRequestDTO.getDataBits());
        if (portOpened) {
            openPortResponseDTO.setOpenPortResponse(true, "Port " + openPortRequestDTO.getPortName() + " opened");
            return ResponseEntity.status(HttpStatus.OK).body(openPortResponseDTO);
        } else {
            openPortResponseDTO.setOpenPortResponse(false, "unable to open port " + openPortRequestDTO.getPortName());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(openPortResponseDTO);
        }
    }

    @PostMapping("/portStatus")
    public ResponseEntity<IsPortOpenResponseDTO> isPortOpenPost(@RequestBody BaseRequestDTO baseRequestDTO) {
        logger.info("Checking if port {} is open", baseRequestDTO.getPortName());
        boolean isPortOpen = serialPortManager.isPortOpen(baseRequestDTO.getPortName());
        IsPortOpenResponseDTO isPortOpenResponseDTO = new IsPortOpenResponseDTO(isPortOpen);
        return ResponseEntity.ok(isPortOpenResponseDTO);
    }

    @PostMapping("/closePort")
    public ResponseEntity<Boolean> closePortPost(@RequestBody BaseRequestDTO baseRequestDTO) {
        logger.info("Closing port: {}", baseRequestDTO.getPortName());
        boolean portClosed = serialPortManager.closePort(baseRequestDTO.getPortName());
        return ResponseEntity.ok(portClosed);
    }

    @PostMapping("/startListening")
    public ResponseEntity<String> startListening(@RequestBody BaseRequestDTO baseRequestDTO) {
        logger.info("Attempting to start listener for port: {}", baseRequestDTO.getPortName());
        if (!serialPortManager.isPortOpen(baseRequestDTO.getPortName())) {
            logger.warn("Port {} is not open. Cannot start listener.", baseRequestDTO.getPortName());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Port " + baseRequestDTO.getPortName() + " is not open.");
        }
        serialPortManager.startListening(baseRequestDTO.getPortName());
        return ResponseEntity.ok("Listener started for port " + baseRequestDTO.getPortName());
    }

    @PostMapping("/stopListening")
    public ResponseEntity<String> stopListening(@RequestBody BaseRequestDTO baseRequestDTO) {
        logger.info("Attempting to stop listener for port: {}", baseRequestDTO.getPortName());
        serialPortManager.stopListening(baseRequestDTO.getPortName());
        return ResponseEntity.ok("Listener stopped for port " + baseRequestDTO.getPortName());
    }

    /**
     * SSE endpoint to stream serial port data to clients
     * @return Flux of ServerSentEvents containing the serial port data
     */
    @GetMapping(path = "/stream-serial", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamSerial() {
        logger.info("Client connected to SSE stream");
        return serialPortManager.getSerialDataFlux()
            .map(data -> ServerSentEvent.<String>builder()
                .id(String.valueOf(System.currentTimeMillis()))
                .event("message")
                .data(data + "  received at: " + LocalTime.now())
                .build());
    }
}
