package com.research.devicemanager.dto;

import lombok.Data;

import java.util.Date;

@Data
public class DeviceResponseDTO {
    private String id;
    private String name;
    private String brand;
    private String state;
    private Date creationDate;
}