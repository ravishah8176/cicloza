package com.cicloza.controller;

import com.cicloza.service.SerialPortManager;
import com.cicloza.util.ColorLogger;
import com.cicloza.dto.BaseRequestDTO;
import com.cicloza.dto.OpenPortResponseDTO;
import com.cicloza.dto.FindPortResponseDTO;
import com.cicloza.dto.IsPortOpenResponseDTO;
import com.cicloza.dto.OpenPortRequestDTO;
import com.fazecast.jSerialComm.SerialPort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/serial")
public class SerialPortController {
    private static final ColorLogger logger = ColorLogger.getLogger(SerialPortController.class);
    private final SerialPortManager serialPortManager;

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
}
