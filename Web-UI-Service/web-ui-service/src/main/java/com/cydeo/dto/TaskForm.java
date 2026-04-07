package com.cydeo.dto;

import com.cydeo.enums.Status;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Form model for Task create and update (Manager).
 * Mirrors TaskDTO validation constraints from the backend.
 * assignedDate, assignedManager, lastUpdateDateTime are READ_ONLY server-side — not included here.
 */
@Data
@NoArgsConstructor
public class TaskForm {

    @NotBlank(message = "Task code is required.")
    @Size(min = 5, max = 5, message = "Task code must be exactly 5 characters.")
    private String taskCode;

    @NotBlank(message = "Task subject is required.")
    @Size(min = 3, max = 16, message = "Task subject must be 3–16 characters.")
    private String taskSubject;

    private String taskDetail;

    /** Optional on create (backend defaults to OPEN); required for full update. */
    private Status taskStatus;

    @NotBlank(message = "Project code is required.")
    private String projectCode;

    @NotBlank(message = "Assigned employee username is required.")
    private String assignedEmployee;
}
