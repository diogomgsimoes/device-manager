package com.research.devicemanager.controller;

import com.research.devicemanager.dto.DeviceRequestDTO;
import com.research.devicemanager.dto.DeviceResponseDTO;
import com.research.devicemanager.dto.UpdateDeviceRequestDTO;
import com.research.devicemanager.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    public ResponseEntity<DeviceResponseDTO> createDevice(@RequestBody @Valid DeviceRequestDTO device) {
        DeviceResponseDTO deviceResponse = deviceService.createDevice(device);
        return new ResponseEntity<>(deviceResponse, HttpStatus.CREATED);
    }

    @GetMapping
    public List<DeviceResponseDTO> getDevices(
            @RequestParam(required = false) UUID id,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String state) {
        return deviceService.findDevices(id, brand, state);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DeviceResponseDTO> updateDevice(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateDeviceRequestDTO device) {
        DeviceResponseDTO deviceResponse = deviceService.updateDevice(id, device);
        return new ResponseEntity<>(deviceResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable UUID id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }
}