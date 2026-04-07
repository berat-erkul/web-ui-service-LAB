package com.cydeo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskDto {

    private String taskCode;
    private String taskSubject;
    private String taskDetail;
    private String taskStatus;       // OPEN | IN_PROGRESS | COMPLETED
    private LocalDate assignedDate;  // READ_ONLY — set server-side
    private String projectCode;
    private String assignedEmployee;
    private String assignedManager;  // READ_ONLY
    private LocalDateTime lastUpdateDateTime; // READ_ONLY
}
