package com.research.devicemanager.mapper;

import com.research.devicemanager.dto.DeviceRequestDTO;
import com.research.devicemanager.dto.DeviceResponseDTO;
import com.research.devicemanager.model.Device;
import com.research.devicemanager.model.State;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DeviceMapper {

    DeviceMapper INSTANCE = Mappers.getMapper(DeviceMapper.class);

    Device toEntity(DeviceRequestDTO dto);

    DeviceResponseDTO toDTO(Device device);

    List<DeviceResponseDTO> toDTOs(List<Device> devices);

    default State map(String value) {
        return value == null ? null : State.fromValue(value);
    }

    default String map(State state) {
        return state == null ? null : state.getValue().toUpperCase();
    }
}