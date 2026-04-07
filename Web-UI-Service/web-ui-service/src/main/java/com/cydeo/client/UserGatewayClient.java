package com.cydeo.client;

import com.cydeo.dto.UserDto;
import com.cydeo.dto.UserForm;
import com.cydeo.util.GatewayErrorHandler;
import com.cydeo.wrapper.ResponseWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserGatewayClient {

    private static final String BASE = "/user-service/api/v1/user";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final GatewayErrorHandler errorHandler;

    // ── READ (list) ────────────────────────────────────────────────────────

    /** GET /read/all — Admin. Used by the users list page. */
    public List<UserDto> getUsers() {
        try {
            ResponseWrapper response = webClient.get()
                    .uri(BASE + "/read/all")
                    .retrieve()
                    .bodyToMono(ResponseWrapper.class)
                    .block();
            if (response != null && response.isSuccess() && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), new TypeReference<List<UserDto>>() {});
            }
        } catch (Exception e) {
            log.error("getUsers failed: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /** GET /read/{userName} — Admin. Used to populate the edit form. */
    public ClientResult<UserDto> getUserByUsername(String username) {
        try {
            ResponseWrapper response = webClient.get()
                    .uri(BASE + "/read/{u}", username)
                    .retrieve()
                    .bodyToMono(ResponseWrapper.class)
                    .block();
            if (response != null && response.isSuccess() && response.getData() != null) {
                UserDto dto = objectMapper.convertValue(response.getData(), UserDto.class);
                return ClientResult.ok(dto);
            }
            return ClientResult.error("User not found.");
        } catch (WebClientResponseException ex) {
            return ClientResult.error(errorHandler.extractMessage(ex));
        } catch (Exception ex) {
            log.error("getUserByUsername failed: {}", ex.getMessage());
            return ClientResult.error(errorHandler.extractMessage(ex));
        }
    }

    // ── CREATE ─────────────────────────────────────────────────────────────

    /** POST /create — Admin. */
    public ClientResult<UserDto> createUser(UserForm form) {
        try {
            ResponseWrapper response = webClient.post()
                    .uri(BASE + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(toRequestBody(form))
                    .retrieve()
                    .bodyToMono(ResponseWrapper.class)
                    .block();
            if (response != null && response.isSuccess() && response.getData() != null) {
                UserDto dto = objectMapper.convertValue(response.getData(), UserDto.class);
                return ClientResult.ok(dto);
            }
            return ClientResult.error("User could not be created.");
        } catch (WebClientResponseException ex) {
            return ClientResult.error(errorHandler.extractMessage(ex));
        } catch (Exception ex) {
            log.error("createUser failed: {}", ex.getMessage());
            return ClientResult.error(errorHandler.extractMessage(ex));
        }
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────

    /** PUT /update/{username} — Admin. */
    public ClientResult<UserDto> updateUser(String username, UserForm form) {
        try {
            ResponseWrapper response = webClient.put()
                    .uri(BASE + "/update/{u}", username)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(toRequestBody(form))
                    .retrieve()
                    .bodyToMono(ResponseWrapper.class)
                    .block();
            if (response != null && response.isSuccess() && response.getData() != null) {
                UserDto dto = objectMapper.convertValue(response.getData(), UserDto.class);
                return ClientResult.ok(dto);
            }
            return ClientResult.error("User could not be updated.");
        } catch (WebClientResponseException ex) {
            return ClientResult.error(errorHandler.extractMessage(ex));
        } catch (Exception ex) {
            log.error("updateUser failed: {}", ex.getMessage());
            return ClientResult.error(errorHandler.extractMessage(ex));
        }
    }

    // ── DELETE ─────────────────────────────────────────────────────────────

    /** DELETE /delete/{userName} — Admin. Returns 204 No Content on success. */
    public ClientResult<Void> deleteUser(String username) {
        try {
            webClient.delete()
                    .uri(BASE + "/delete/{u}", username)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            return ClientResult.ok();
        } catch (WebClientResponseException ex) {
            return ClientResult.error(errorHandler.extractMessage(ex));
        } catch (Exception ex) {
            log.error("deleteUser failed: {}", ex.getMessage());
            return ClientResult.error(errorHandler.extractMessage(ex));
        }
    }

    // ── private helpers ────────────────────────────────────────────────────

    /**
     * Builds the JSON request body for create/update.
     * role is nested as { "description": "Admin" } to match UserDTO.
     * Using a Map avoids needing the WebClient encoder to handle LocalDate, etc.
     */
    private Map<String, Object> toRequestBody(UserForm form) {
        Map<String, String> role = new LinkedHashMap<>();
        role.put("description", form.getRoleDescription());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("firstName", form.getFirstName());
        body.put("lastName", form.getLastName());
        body.put("userName", form.getUserName());
        body.put("password", form.getPassword());
        body.put("phone", form.getPhone());
        body.put("enabled", form.isEnabled());
        body.put("role", role);
        body.put("gender", form.getGender() != null ? form.getGender().name() : null);
        return body;
    }
}
