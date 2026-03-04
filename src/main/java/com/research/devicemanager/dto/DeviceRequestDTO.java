package com.research.devicemanager.dto;

import com.research.devicemanager.model.State;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeviceRequestDTO {

    @NotBlank(message = "Device name is required")
    @Size(max = 100, message = "Device name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Brand is required")
    @Size(max = 50, message = "Brand cannot exceed 50 characters")
    private String brand;

    @NotBlank(message = "State is required")
    private State state;
}