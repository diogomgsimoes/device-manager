package com.research.devicemanager.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DeviceResponseDTO {
    private UUID id;
    private String name;
    private String brand;
    private String state;
    private LocalDateTime creationDate;
}