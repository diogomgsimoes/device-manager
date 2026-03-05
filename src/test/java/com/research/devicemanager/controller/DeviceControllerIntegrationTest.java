package com.research.devicemanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.research.devicemanager.dto.DeviceRequestDTO;
import com.research.devicemanager.dto.UpdateDeviceRequestDTO;
import com.research.devicemanager.model.State;
import com.research.devicemanager.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DeviceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeviceRepository deviceRepository;

    private String createdDeviceId;

    @BeforeEach
    void setUp() throws Exception {
        deviceRepository.deleteAll();

        DeviceRequestDTO request = new DeviceRequestDTO();
        request.setName("iPhone 15");
        request.setBrand("Apple");
        request.setState(State.AVAILABLE);

        MvcResult result = mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        createdDeviceId = objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void createDevice_shouldReturn201() throws Exception {
        DeviceRequestDTO request = new DeviceRequestDTO();
        request.setName("Galaxy S24");
        request.setBrand("Samsung");
        request.setState(State.AVAILABLE);

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Galaxy S24"))
                .andExpect(jsonPath("$.brand").value("Samsung"))
                .andExpect(jsonPath("$.state").value("AVAILABLE"));
    }

    @Test
    void createDevice_whenDuplicateDataIsProvided_shouldReturn409() throws Exception {
        DeviceRequestDTO request = new DeviceRequestDTO();
        request.setName("iPhone 15");
        request.setBrand("Apple");
        request.setState(State.AVAILABLE);

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getDeviceById_whenDeviceExists_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/devices/{id}", createdDeviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdDeviceId));
    }

    @Test
    void getDeviceById_whenDeviceDoesNotExist_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/devices/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getDevices_withBrandFilter_shouldReturnFiltered() throws Exception {
        mockMvc.perform(get("/api/devices").param("brand", "Apple"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].brand").value("Apple"));
    }

    @Test
    void updateDevice_shouldReturn200() throws Exception {
        UpdateDeviceRequestDTO request = new UpdateDeviceRequestDTO();
        request.setName("iPhone 15 Pro");

        mockMvc.perform(patch("/api/devices/{id}", createdDeviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("iPhone 15 Pro"));
    }

    @Test
    void updateDevice_whenDeviceIsInUse_shouldReturn409() throws Exception {
        UpdateDeviceRequestDTO deviceInUse = new UpdateDeviceRequestDTO();
        deviceInUse.setState(State.IN_USE);
        mockMvc.perform(patch("/api/devices/{id}", createdDeviceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deviceInUse)));

        UpdateDeviceRequestDTO request = new UpdateDeviceRequestDTO();
        request.setName("Should Fail");

        mockMvc.perform(patch("/api/devices/{id}", createdDeviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteDevice_whenDeviceExists_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/devices/{id}", createdDeviceId)).andExpect(status().isNoContent());
    }

    @Test
    void deleteDevice_whenDeviceIsInUse_shouldReturn409() throws Exception {
        UpdateDeviceRequestDTO deviceInUse = new UpdateDeviceRequestDTO();
        deviceInUse.setState(State.IN_USE);
        mockMvc.perform(patch("/api/devices/{id}", createdDeviceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deviceInUse)));

        mockMvc.perform(delete("/api/devices/{id}", createdDeviceId)).andExpect(status().isConflict());
    }

    @Test
    void deleteDevice_whenDeviceDoesNotExist_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/devices/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
