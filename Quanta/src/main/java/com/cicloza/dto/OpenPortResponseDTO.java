package com.cicloza.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenPortResponseDTO extends BaseResponseDTO {
    private boolean portOpened;

    public void setOpenPortResponse(boolean portOpened, String message) {
        this.setMessage(message);
        this.portOpened = portOpened;
    }
} 