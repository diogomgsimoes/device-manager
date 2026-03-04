package com.research.devicemanager.service;

import com.research.devicemanager.dto.DeviceRequestDTO;
import com.research.devicemanager.dto.DeviceResponseDTO;
import com.research.devicemanager.dto.UpdateDeviceRequestDTO;
import com.research.devicemanager.exception.DeviceNotFoundException;
import com.research.devicemanager.exception.ResourceAlreadyExistsException;
import com.research.devicemanager.exception.StateConflictException;
import com.research.devicemanager.mapper.DeviceMapper;
import com.research.devicemanager.model.Device;
import com.research.devicemanager.model.State;
import com.research.devicemanager.repository.DeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.research.devicemanager.model.State.canTransition;

@Slf4j
@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Transactional
    public DeviceResponseDTO createDevice(DeviceRequestDTO deviceRequestDTO) {
        log.info("Creating device with name={}, brand={}", deviceRequestDTO.getName(), deviceRequestDTO.getBrand());

        Optional<Device> deviceOptional = deviceRepository.findByNameAndBrand(
                deviceRequestDTO.getName(),
                deviceRequestDTO.getBrand());
        if (deviceOptional.isPresent()) {
            log.info("Device with name={} and brand={} already exists",
                    deviceRequestDTO.getName(),
                    deviceRequestDTO.getBrand());
            throw new ResourceAlreadyExistsException("Device with name " + deviceRequestDTO.getName() + " and brand " +
                    deviceRequestDTO.getBrand() + " already exist");
        }

        Device deviceEntity = DeviceMapper.INSTANCE.toEntity(deviceRequestDTO);
        deviceEntity = deviceRepository.saveAndFlush(deviceEntity);
        log.info("Device created with id={}", deviceEntity.getId());

        return DeviceMapper.INSTANCE.toDTO(deviceEntity);
    }

    public DeviceResponseDTO findDeviceById(UUID id) {
        log.info("Finding device with id={}", id);

        Optional<Device> optionalDevice = deviceRepository.findById(id);
        if (optionalDevice.isEmpty()) {
            log.warn("Device with id={} not found", id);
            throw new DeviceNotFoundException(id);
        }

        return DeviceMapper.INSTANCE.toDTO(optionalDevice.get());
    }

    public List<DeviceResponseDTO> findDevices(String name, String brand) {
        log.info("Searching devices with filters: name={}, brand={}", name, brand);

        Specification<Device> spec = null;
        if (name != null) {
            spec = (root, query, cb) ->
                    cb.equal(root.get("name"), name);
        }
        if (brand != null) {
            Specification<Device> brandSpec = (root, query, cb) ->
                    cb.equal(root.get("state"), brand);
            spec = (spec == null) ? brandSpec : spec.and(brandSpec);
        }

        List<Device> devices = (spec == null) ? deviceRepository.findAll() : deviceRepository.findAll(spec);
        log.info("Found {} devices found with filters: name={}, brand={}", devices.size(), name, brand);

        return DeviceMapper.INSTANCE.toDTOs(devices);
    }

    @Transactional
    public DeviceResponseDTO updateDevice(UUID id, UpdateDeviceRequestDTO updateDeviceRequestDTO) {
        log.info("Updating device with id={}", id);

        Optional<Device> existingDeviceOptional = deviceRepository.findById(id);
        if (existingDeviceOptional.isEmpty()) {
            log.warn("Device with id={} not found", id);
            throw new DeviceNotFoundException(id);
        }

        Device existingDevice = existingDeviceOptional.get();
        if (existingDevice.getState().equals(State.IN_USE) &&
                (updateDeviceRequestDTO.getName() != null || updateDeviceRequestDTO.getBrand() != null)) {
            log.warn("Device is IN_USE and it's name and/or brand cannot be updated");
            throw new StateConflictException("Device is IN_USE and it's name and/or brand cannot be updated");
        } else {
            if (updateDeviceRequestDTO.getName() != null) {
                existingDevice.setName(updateDeviceRequestDTO.getName());
            }

            if (updateDeviceRequestDTO.getBrand() != null) {
                existingDevice.setBrand(updateDeviceRequestDTO.getBrand());
            }
        }

        if (updateDeviceRequestDTO.getState() != null) {
            if (!canTransition(existingDevice.getState(), updateDeviceRequestDTO.getState())) {
                log.warn("Device transition is not allowed");
                throw new StateConflictException("Device state cannot transition from " + existingDevice.getState()
                        + " to " + updateDeviceRequestDTO.getState());
            } else {
                existingDevice.setState(updateDeviceRequestDTO.getState());
            }
        }

        deviceRepository.save(existingDevice);
        log.info("Device with id={} updated successfully", id);

        return DeviceMapper.INSTANCE.toDTO(existingDevice);
    }

    @Transactional
    public void deleteDevice(UUID id) {
        log.info("Deleting device with id={}", id);

        Optional<Device> deviceOptional = deviceRepository.findById(id);

        if (deviceOptional.isEmpty()) {
            log.warn("Device with id={} not found", id);
            throw new DeviceNotFoundException(id);
        }

        Device device = deviceOptional.get();
        if (device.getState().equals(State.IN_USE)) {
            log.warn("Cannot delete device with state IN_USE and id={}", id);
            throw new StateConflictException("Device with id " + id + " is in-use and cannot be deleted");
        }

        deviceRepository.delete(device);
        log.info("Device with id={} deleted successfully", id);
    }
}