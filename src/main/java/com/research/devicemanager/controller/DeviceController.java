package com.research.devicemanager.controller;

import com.research.devicemanager.dto.DeviceRequestDTO;
import com.research.devicemanager.dto.DeviceResponseDTO;
import com.research.devicemanager.dto.UpdateDeviceRequestDTO;
import com.research.devicemanager.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
@Tag(name = "Devices", description = "Persist and manage devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Operation(summary = "Create a new device")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Device created successfully",
                    content = @Content(schema = @Schema(implementation = DeviceResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Device with name/brand combination already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<DeviceResponseDTO> createDevice(@RequestBody @Valid DeviceRequestDTO device) {
        DeviceResponseDTO deviceResponse = deviceService.createDevice(device);
        return new ResponseEntity<>(deviceResponse, HttpStatus.CREATED);
    }

    @Operation(summary = "Get a device by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device found",
                    content = @Content(schema = @Schema(implementation = DeviceResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Device not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponseDTO> getDeviceById(
            @Parameter(description = "Device UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(deviceService.findDeviceById(id));
    }

    @Operation(summary = "List devices", description = "Returns all devices, optionally filtered by name and/or brand")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devices retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DeviceResponseDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<DeviceResponseDTO>> getDevices(
            @Parameter(description = "Filter by name") @RequestParam(required = false) String name,
            @Parameter(description = "Filter by brand") @RequestParam(required = false) String brand) {
        return ResponseEntity.ok(deviceService.findDevices(name, brand));
    }

    @Operation(summary = "Fully or partially update a device", description = "All fields are optional. " +
            "Devices in IN_USE state cannot have name or brand updated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device updated successfully",
                    content = @Content(schema = @Schema(implementation = DeviceResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Device not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Invalid state transition or device is IN_USE",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<DeviceResponseDTO> updateDevice(
            @Parameter(description = "Device UUID") @PathVariable UUID id,
            @RequestBody @Valid UpdateDeviceRequestDTO device) {
        DeviceResponseDTO deviceResponse = deviceService.updateDevice(id, device);
        return ResponseEntity.ok(deviceResponse);
    }

    @Operation(summary = "Delete a device", description = "Devices in IN_USE state cannot be deleted.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Device deleted successfully",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Device not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Device is IN_USE and cannot be deleted",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(
            @Parameter(description = "Device UUID") @PathVariable UUID id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }
}