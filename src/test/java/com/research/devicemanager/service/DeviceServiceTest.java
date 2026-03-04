package com.research.devicemanager.service;

import com.research.devicemanager.dto.DeviceRequestDTO;
import com.research.devicemanager.dto.UpdateDeviceRequestDTO;
import com.research.devicemanager.exception.DeviceNotFoundException;
import com.research.devicemanager.exception.StateConflictException;
import com.research.devicemanager.model.Device;
import com.research.devicemanager.model.State;
import com.research.devicemanager.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeviceServiceTest {

    private DeviceRepository deviceRepository;
    private DeviceService deviceService;

    @BeforeEach
    void setUp() {
        deviceRepository = mock(DeviceRepository.class);
        deviceService = new DeviceService(deviceRepository);
    }

    @Test
    void createDevice_shouldSaveAndReturnDTO() {
        DeviceRequestDTO dto = new DeviceRequestDTO();
        dto.setName("iPhone");
        dto.setBrand("Apple");
        dto.setState("available");

        Device savedDevice = new Device();
        savedDevice.setId(UUID.randomUUID());
        savedDevice.setName(dto.getName());
        savedDevice.setBrand(dto.getBrand());
        savedDevice.setState(State.AVAILABLE);

        when(deviceRepository.saveAndFlush(any(Device.class))).thenReturn(savedDevice);

        var result = deviceService.createDevice(dto);

        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getBrand(), result.getBrand());
        verify(deviceRepository, times(1)).saveAndFlush(any(Device.class));
    }

    @Test
    void findDevices_whenNoFiltersProvided_shouldCallFindAll() {
        Device device = new Device();
        device.setId(UUID.randomUUID());
        device.setName("iPhone");
        device.setBrand("Apple");
        device.setState(State.AVAILABLE);

        when(deviceRepository.findAll()).thenReturn(List.of(device));

        var result = deviceService.findDevices(null, null, null);

        assertEquals(1, result.size());
        verify(deviceRepository, times(1)).findAll();
        verify(deviceRepository, never()).findAll(any(Specification.class));
    }

    @Test
    void findDevices_whenIdIsProvidedAndExists_shouldReturnDevice() {
        UUID id = UUID.randomUUID();
        Device device = new Device();
        device.setId(id);
        device.setName("Pixel");
        device.setBrand("Google");
        device.setState(State.AVAILABLE);

        when(deviceRepository.findAll(any(Specification.class))).thenReturn(List.of(device));

        var results = deviceService.findDevices(id, null, null);
        assertEquals(1, results.size());
        assertEquals("Pixel", results.getFirst().getName());
    }

    @Test
    void findDevices_whenIdIsProvidedButDoesNotExist_shouldThrowNotFoundException() {
        UUID id = UUID.randomUUID();
        when(deviceRepository.findAll()).thenReturn(List.of());

        assertThrows(DeviceNotFoundException.class, () -> deviceService.findDevices(id, null, null));
    }

    @Test
    void findDevices_whenBrandIsProvided_shouldCallFindAllWithSpec() {
        Device device = new Device();
        device.setBrand("Apple");

        when(deviceRepository.findAll(any(Specification.class))).thenReturn(List.of(device));

        var result = deviceService.findDevices(null, "Apple", null);

        assertEquals(1, result.size());
        verify(deviceRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void findDevices_whenStateIsProvided_shouldCallFindAllWithSpec() {
        Device device = new Device();
        device.setState(State.AVAILABLE);

        when(deviceRepository.findAll(any(Specification.class))).thenReturn(List.of(device));

        var result = deviceService.findDevices(null, null, "available");

        assertEquals(1, result.size());
        verify(deviceRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void findDevices_whenBrandIsProvidedButDoesNotExists_shouldReturnEmptyList() {
        when(deviceRepository.findAll(any(Specification.class))).thenReturn(List.of());

        var result = deviceService.findDevices(null, "NonExistingBrand", null);

        assertTrue(result.isEmpty());
    }

    @Test
    void findDevices_whenAllFiltersProvided_shouldReturnDevice() {
        UUID id = UUID.randomUUID();

        Device device = new Device();
        device.setId(id);
        device.setBrand("Apple");
        device.setState(State.AVAILABLE);

        when(deviceRepository.findAll(any(Specification.class))).thenReturn(List.of(device));

        var result = deviceService.findDevices(id, "Apple", "AVAILABLE");

        assertEquals(1, result.size());
        assertEquals(id.toString(), result.getFirst().getId());
    }

    @Test
    void updateDevice_whenFullDataIsProvidedAndValid_shouldUpdateAll() {
        UUID id = UUID.randomUUID();

        Device existing = new Device();
        existing.setId(id);
        existing.setName("OldName");
        existing.setBrand("OldBrand");
        existing.setState(State.AVAILABLE);

        when(deviceRepository.findById(id)).thenReturn(Optional.of(existing));
        when(deviceRepository.save(any(Device.class))).thenReturn(existing);

        UpdateDeviceRequestDTO dto = new UpdateDeviceRequestDTO();
        dto.setName("NewName");
        dto.setBrand("NewBrand");
        dto.setState("INACTIVE");

        var result = deviceService.updateDevice(id, dto);

        assertEquals("NewName", result.getName());
        assertEquals("NewBrand", result.getBrand());
        assertEquals("INACTIVE", result.getState());

    }

    @Test
    void updateDevice_whenNotInUse_shouldUpdateBrandAndName() {
        UUID id = UUID.randomUUID();

        Device existing = new Device();
        existing.setId(id);
        existing.setName("OldName");
        existing.setBrand("OldBrand");
        existing.setState(State.AVAILABLE);

        when(deviceRepository.findById(id)).thenReturn(Optional.of(existing));
        when(deviceRepository.save(any(Device.class))).thenReturn(existing);

        UpdateDeviceRequestDTO dto = new UpdateDeviceRequestDTO();
        dto.setName("NewName");
        dto.setBrand("NewBrand");

        var result = deviceService.updateDevice(id, dto);

        assertEquals("NewName", result.getName());
        assertEquals("NewBrand", result.getBrand());
    }

    @Test
    void updateDevice_whenInUseAndBrandOrNameAreProvided_shouldThrowStateConflictException() {
        UUID id = UUID.randomUUID();

        Device existing = new Device();
        existing.setId(id);
        existing.setName("OldName");
        existing.setBrand("OldBrand");
        existing.setState(State.IN_USE);

        when(deviceRepository.findById(id)).thenReturn(Optional.of(existing));
        when(deviceRepository.save(any(Device.class))).thenReturn(existing);

        UpdateDeviceRequestDTO dto = new UpdateDeviceRequestDTO();
        dto.setName("NewName");
        dto.setBrand("NewBrand");

        assertThrows(StateConflictException.class, () -> deviceService.updateDevice(id, dto));
    }

    @Test
    void updateDevice_whenInUseWithOnlyValidStateTransition_shouldUpdateStateOnly() {
        UUID id = UUID.randomUUID();

        Device existing = new Device();
        existing.setId(id);
        existing.setName("Device");
        existing.setBrand("Brand");
        existing.setState(State.IN_USE);

        when(deviceRepository.findById(id)).thenReturn(Optional.of(existing));
        when(deviceRepository.save(any(Device.class))).thenReturn(existing);

        UpdateDeviceRequestDTO dto = new UpdateDeviceRequestDTO();
        dto.setState("AVAILABLE");

        var result = deviceService.updateDevice(id, dto);

        assertEquals(State.AVAILABLE.toString(), result.getState());
    }

    @Test
    void updateDevice_whenInvalidStateTransition_shouldThrowStateConflictException() {
        UUID id = UUID.randomUUID();

        Device existing = new Device();
        existing.setId(id);
        existing.setState(State.INACTIVE);

        when(deviceRepository.findById(id)).thenReturn(Optional.of(existing));

        UpdateDeviceRequestDTO dto = new UpdateDeviceRequestDTO();
        dto.setState("IN_USE");

        assertThrows(StateConflictException.class, () -> deviceService.updateDevice(id, dto));
    }

    @Test
    void deleteDevice_whenDeviceExists_shouldDelete() {
        UUID id = UUID.randomUUID();
        Device device = new Device();
        device.setId(id);
        device.setState(State.AVAILABLE);

        when(deviceRepository.findById(id)).thenReturn(Optional.of(device));

        deviceService.deleteDevice(id);

        verify(deviceRepository, times(1)).delete(device);
    }

    @Test
    void deleteDevice_whenDeviceDoesNotExist_shouldThrowNotFoundException() {
        UUID id = UUID.randomUUID();
        when(deviceRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(DeviceNotFoundException.class, () -> deviceService.deleteDevice(id));
    }

    @Test
    void deleteDevice_whenDeviceIsInUse_shouldThrowStateConflictException() {
        UUID id = UUID.randomUUID();
        Device device = new Device();
        device.setId(id);
        device.setState(State.IN_USE);

        when(deviceRepository.findById(id)).thenReturn(Optional.of(device));

        assertThrows(StateConflictException.class, () -> deviceService.deleteDevice(id));
    }
}