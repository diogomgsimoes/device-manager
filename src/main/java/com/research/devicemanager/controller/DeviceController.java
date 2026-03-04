package com.research.devicemanager.controller;

import com.research.devicemanager.dto.DeviceRequestDTO;
import com.research.devicemanager.dto.DeviceResponseDTO;
import com.research.devicemanager.dto.UpdateDeviceRequestDTO;
import com.research.devicemanager.model.State;
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

    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponseDTO> getDeviceById(@PathVariable UUID id) {
        return ResponseEntity.ok(deviceService.findDeviceById(id));
    }

    @GetMapping
    public ResponseEntity<List<DeviceResponseDTO>> getDevices(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand) {
        return ResponseEntity.ok(deviceService.findDevices(name, brand));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DeviceResponseDTO> updateDevice(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateDeviceRequestDTO device) {
        DeviceResponseDTO deviceResponse = deviceService.updateDevice(id, device);
        return ResponseEntity.ok(deviceResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable UUID id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }
}