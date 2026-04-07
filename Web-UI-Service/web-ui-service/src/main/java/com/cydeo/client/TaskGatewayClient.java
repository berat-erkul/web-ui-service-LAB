package com.cydeo.client;

import com.cydeo.dto.TaskDto;
import com.cydeo.dto.TaskForm;
import com.cydeo.enums.Status;
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
public class TaskGatewayClient {

    private static final String BASE = "/task-service/api/v1/task";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final GatewayErrorHandler errorHandler;

    // ── READ (lists) ───────────────────────────────────────────────────────

    /**
     * GET /read/all/{projectCode} — Admin or Manager.
     * Returns all tasks belonging to a project.
     */
    public List<TaskDto> getTasksByProject(String projectCode) {
        try {
            ResponseWrapper response = webClient.get()
                    .uri(BASE + "/read/all/{code}", projectCode)
                    .retrieve()
                    .bodyToMono(ResponseWrapper.class)
                    .block();
            if (response != null && response.isSuccess() && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), new TypeReference<List<TaskDto>>() {});
            }
        } catch (Exception e) {
            log.error("getTasksByProject failed for {}: {}", projectCode, e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * GET /read/employee/pending-tasks — Employee.
     * Returns the logged-in employee's non-completed tasks.
     */
    public List<TaskDto> getPendingTasksForEmployee() {
        try {
            ResponseWrapper response = webClient.get()
                    .uri(BASE + "/read/employee/pending-tasks")
                    .retrieve()
                    .bodyToMono(ResponseWrapper.class)
                    .block();
            if (response != null && response.isSuccess() && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), new TypeReference<List<TaskDto>>() {});
            }
        } catch (Exception e) {
            log.error("getPendingTasksForEmployee failed: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * GET /read/employee/archive — Employee.
     * Returns the logged-in employee's completed (archived) tasks.
     */
    public List<TaskDto> getArchivedTasksForEmployee() {
        try {
            ResponseWrapper response = webClient.get()
                    .uri(BASE + "/read/employee/archive")
                    .retrieve()
                    .bodyToMono(ResponseWrapper.class)
                    .block();
            if (response != null && response.isSuccess() && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), new TypeReference<List<TaskDto>>() {});
            }
        } catch (Exception e) {
            log.error("getArchivedTasksForEmployee failed: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    // ── READ (single) ──────────────────────────────────────────────────────

    /**
     * GET /read/{taskCode} — Manager or Employee.
     * Used to populate the edit form or status-update form.
     */
    public ClientResult<TaskDto> getTaskByCode(String taskCode) {
        try {
            ResponseWrapper response = webClient.get()
                    .uri(BASE + "/read/{code}", taskCode)
                    .retrieve()
                    .bodyToMono(ResponseWrapper.class)
                    .block();
            if (response != null && response.isSuccess() && response.getData() != null) {
                TaskDto dto = objectMapper.convertValue(response.getData(), TaskDto.class);
                return ClientResult.ok(dto);
            }
            return ClientResult.error("Task not found.");
        } catch (WebClientResponseException ex) {
            return ClientResult.error(errorHandler.extractMessage(ex));
        } catch (Exception ex) {
            log.error("getTaskByCode failed: {}", ex.getMessage());
            return ClientResult.error(errorHandler.extractMessage(ex));
        }
    }

    // ── CREATE ─────────────────────────────────────────────────────────────

    /** POST /create — Manager. */
    public ClientResult<TaskDto> createTask(TaskForm form) {
        try {
            ResponseWrapper response = webClient.post()
                    .uri(BASE + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(toRequestBody(form))
                    .retrieve()
                    .bodyToMono(ResponseWrapper.class)
                    .block();
            if (response != null && response.isSuccess() && response.getData() != null) {
                TaskDto dto = objectMapper.convertValue(response.getData(), TaskDto.class);
                return ClientResult.ok(dto);
            }
            return ClientResult.error("Task could not be created.");
        } catch (WebClientResponseException ex) {
            return ClientResult.error(errorHandler.extractMessage(ex));
        } catch (Exception ex) {
            log.error("createTask failed: {}", ex.getMessage());
            return ClientResult.error(errorHandler.extractMessage(ex));
        }
    }

    // ── UPDATE (Manager full update) ───────────────────────────────────────

    /** PUT /update/{taskCode} — Manager. Full field update. */
    public ClientResult<TaskDto> updateTask(String taskCode, TaskForm form) {
        try {
            ResponseWrapper response = webClient.put()
                    .uri(BASE + "/update/{code}", taskCode)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(toRequestBody(form))
                    .retrieve()
                    .bodyToMono(ResponseWrapper.class)
                    .block();
            if (response != null && response.isSuccess() && response.getData() != null) {
                TaskDto dto = objectMapper.convertValue(response.getData(), TaskDto.class);
                return ClientResult.ok(dto);
            }
            return ClientResult.error("Task could not be updated.");
        } catch (WebClientResponseException ex) {
            return ClientResult.error(errorHandler.extractMessage(ex));
        } catch (Exception ex) {
            log.error("updateTask failed: {}", ex.getMessage());
            return ClientResult.error(errorHandler.extractMessage(ex));
        }
    }

    // ── UPDATE (Employee status transition) ────────────────────────────────

    /**
     * PUT /update/employee/{taskCode}?status={status} — Employee.
     * Allowed transitions: OPEN → IN_PROGRESS, IN_PROGRESS → COMPLETED.
     */
    public ClientResult<TaskDto> updateTaskStatus(String taskCode, Status status) {
        try {
            ResponseWrapper response = webClient.put()
                    .uri(uriBuilder -> uriBuilder
                            .path(BASE + "/update/employee/{code}")
                            .queryParam("status", status.name())
                            .build(taskCode))
                    .retrieve()
                    .bodyToMono(ResponseWrapper.class)
                    .block();
            if (response != null && response.isSuccess() && response.getData() != null) {
                TaskDto dto = objectMapper.convertValue(response.getData(), TaskDto.class);
                return ClientResult.ok(dto);
            }
            return ClientResult.error("Task status could not be updated.");
        } catch (WebClientResponseException ex) {
            return ClientResult.error(errorHandler.extractMessage(ex));
        } catch (Exception ex) {
            log.error("updateTaskStatus failed: {}", ex.getMessage());
            return ClientResult.error(errorHandler.extractMessage(ex));
        }
    }

    // ── DELETE ─────────────────────────────────────────────────────────────

    /** DELETE /delete/{taskCode} — Manager. Returns 204 No Content on success. */
    public ClientResult<Void> deleteTask(String taskCode) {
        try {
            webClient.delete()
                    .uri(BASE + "/delete/{code}", taskCode)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            return ClientResult.ok();
        } catch (WebClientResponseException ex) {
            return ClientResult.error(errorHandler.extractMessage(ex));
        } catch (Exception ex) {
            log.error("deleteTask failed: {}", ex.getMessage());
            return ClientResult.error(errorHandler.extractMessage(ex));
        }
    }

    // ── private helpers ────────────────────────────────────────────────────

    private Map<String, Object> toRequestBody(TaskForm form) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("taskCode", form.getTaskCode());
        body.put("taskSubject", form.getTaskSubject());
        body.put("taskDetail", form.getTaskDetail());
        body.put("taskStatus", form.getTaskStatus() != null ? form.getTaskStatus().name() : null);
        body.put("projectCode", form.getProjectCode());
        body.put("assignedEmployee", form.getAssignedEmployee());
        return body;
    }
}
