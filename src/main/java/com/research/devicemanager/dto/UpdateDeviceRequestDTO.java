package com.research.devicemanager.dto;

import com.research.devicemanager.model.State;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateDeviceRequestDTO {

    @Size(max = 100, message = "Device name cannot exceed 100 characters")
    private String name;

    @Size(max = 50, message = "Brand cannot exceed 50 characters")
    private String brand;

    private State state;
}