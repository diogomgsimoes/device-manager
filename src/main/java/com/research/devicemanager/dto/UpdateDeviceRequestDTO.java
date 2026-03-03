package com.research.devicemanager.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateDeviceRequestDTO {

    @Size(max = 100, message = "Device name cannot exceed 100 characters")
    private String name;

    @Size(max = 50, message = "Brand cannot exceed 50 characters")
    private String brand;

    @Pattern(regexp = "available|in-use|inactive", flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "State must be one of: available, in-use, inactive")
    private String state;
}