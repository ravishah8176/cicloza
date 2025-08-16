package com.cicloza.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenPortRequestDTO {
    private String portName;
    private int baudRate;
    private int dataBits;
}