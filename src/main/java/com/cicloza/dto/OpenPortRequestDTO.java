package com.cicloza.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenPortRequestDTO extends BaseRequestDTO {
    private int baudRate;
    private int dataBits;
}