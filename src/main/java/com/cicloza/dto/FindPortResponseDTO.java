package com.cicloza.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindPortResponseDTO extends BaseResponseDTO {
    private boolean portFound;

    public void setFindPortResponse(boolean portFound, String message) {
        this.setMessage(message);
        this.portFound = portFound;
    }
} 