package com.research.devicemanager.exception;

import java.util.UUID;

public class DeviceNotFoundException extends RuntimeException {
    public DeviceNotFoundException(UUID id) {
        super("Device with id '%s' does not exist".formatted(id.toString()));
    }
}