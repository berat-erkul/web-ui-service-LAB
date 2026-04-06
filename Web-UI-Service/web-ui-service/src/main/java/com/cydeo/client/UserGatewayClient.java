package com.cydeo.client;

import com.cydeo.dto.UserDto;
import com.cydeo.wrapper.ResponseWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserGatewayClient {

    private static final String USERS_URL = "/user-service/api/v1/user/read/all";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public List<UserDto> getUsers() {
        try {
            ResponseWrapper response = webClient.get()
                    .uri(USERS_URL)
                    .retrieve()
                    .bodyToMono(ResponseWrapper.class)
                    .block();

            if (response != null && response.isSuccess() && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), new TypeReference<List<UserDto>>() {});
            }
        } catch (Exception e) {
            log.error("Failed to fetch users from gateway: {}", e.getMessage());
        }
        return Collections.emptyList();
    }
}
