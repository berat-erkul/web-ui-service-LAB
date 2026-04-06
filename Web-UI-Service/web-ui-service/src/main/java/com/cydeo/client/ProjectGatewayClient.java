package com.cydeo.client;

import com.cydeo.dto.ProjectDto;
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
public class ProjectGatewayClient {

    private static final String PROJECTS_URL = "/project-service/api/v1/project/read/all/admin";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public List<ProjectDto> getProjects() {
        try {
            ResponseWrapper response = webClient.get()
                    .uri(PROJECTS_URL)
                    .retrieve()
                    .bodyToMono(ResponseWrapper.class)
                    .block();

            if (response != null && response.isSuccess() && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), new TypeReference<List<ProjectDto>>() {});
            }
        } catch (Exception e) {
            log.error("Failed to fetch projects from gateway: {}", e.getMessage());
        }
        return Collections.emptyList();
    }
}
