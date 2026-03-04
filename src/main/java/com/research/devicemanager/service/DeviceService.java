package com.research.devicemanager.service;

import com.research.devicemanager.dto.DeviceRequestDTO;
import com.research.devicemanager.dto.DeviceResponseDTO;
import com.research.devicemanager.dto.UpdateDeviceRequestDTO;
import com.research.devicemanager.exception.DeviceNotFoundException;
import com.research.devicemanager.exception.StateConflictException;
import com.research.devicemanager.mapper.DeviceMapper;
import com.research.devicemanager.model.Device;
import com.research.devicemanager.model.State;
import com.research.devicemanager.repository.DeviceRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.research.devicemanager.model.State.canTransition;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public DeviceResponseDTO createDevice(DeviceRequestDTO device) {
        // TODO: validate if exists first
        Device request = DeviceMapper.INSTANCE.toEntity(device);
        deviceRepository.save(request);
        return DeviceMapper.INSTANCE.toDTO(request);
    }

    public List<DeviceResponseDTO> findDevices(UUID id, String brand, String state) {
        Specification<Device> spec = null;

        if (id != null) {
            spec = (root, query, cb) ->
                    cb.equal(root.get("id"), id);
        }
        if (brand != null) {
            Specification<Device> brandSpec = (root, query, cb) ->
                    cb.equal(root.get("brand"), brand);
            spec = (spec == null) ? brandSpec : spec.and(brandSpec);
        }
        if (state != null) {
            Specification<Device> stateSpec = (root, query, cb) ->
                    cb.equal(root.get("state"), state);
            spec = (spec == null) ? stateSpec : spec.and(stateSpec);
        }

        List<Device> devices = (spec == null)
                ? deviceRepository.findAll()
                : deviceRepository.findAll(spec);

        if (devices.isEmpty() && id != null) {
            throw new DeviceNotFoundException("Device with id " + id + " not found");
        }

        return DeviceMapper.INSTANCE.toDTOs(devices);
    }

    public DeviceResponseDTO updateDevice(UUID id, UpdateDeviceRequestDTO updatedDevice) {
        Optional<Device> existingDeviceOptional = deviceRepository.findById(id);
        if (existingDeviceOptional.isEmpty()) {
            throw new DeviceNotFoundException("Device with id " + id + " not found");
        }

        Device existingDevice = existingDeviceOptional.get();
        if (existingDevice.getState().equals(State.IN_USE) &&
                (updatedDevice.getName() != null || updatedDevice.getBrand() != null)) {
            throw new StateConflictException("Device is in-use and it's name and/or brand cannot be updated");
        } else {
            if (updatedDevice.getName() != null) {
                existingDevice.setName(updatedDevice.getName());
            }

            if (updatedDevice.getBrand() != null) {
                existingDevice.setBrand(updatedDevice.getBrand());
            }
        }

        if (updatedDevice.getState() != null) {
            if (!canTransition(existingDevice.getState(), State.fromValue(updatedDevice.getState()))) {
                throw new StateConflictException("Device state cannot transition from " + existingDevice.getState()
                        + " to " + updatedDevice.getState());
            } else {
                existingDevice.setState(State.valueOf(updatedDevice.getState()));
            }
        }

        deviceRepository.save(existingDevice);

        return DeviceMapper.INSTANCE.toDTO(existingDevice);
    }

    public void deleteDevice(UUID id) {
        Optional<Device> deviceOptional = deviceRepository.findById(id);

        if (deviceOptional.isEmpty()) {
            throw new DeviceNotFoundException("Device with id " + id + " not found");
        }

        Device device = deviceOptional.get();
        if (device.getState().equals(State.IN_USE)) {
            throw new StateConflictException("Device with id " + id + " is in-use and cannot be deleted");
        }

        deviceRepository.delete(device);
    }
}