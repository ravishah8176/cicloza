package com.cicloza.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IsPortOpenResponseDTO extends BaseResponseDTO {
    private boolean portAlreadyOpen;

    public IsPortOpenResponseDTO(boolean portAlreadyOpen) {
        this.setMessage(portAlreadyOpen ? "Port is already open" : "Port is not open");
        this.portAlreadyOpen = portAlreadyOpen;
    }
} 