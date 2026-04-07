package com.cydeo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Form model for Project create and update.
 * Mirrors ProjectDTO validation constraints from the backend.
 */
@Data
@NoArgsConstructor
public class ProjectForm {

    @NotBlank(message = "Project name is required.")
    @Size(min = 3, max = 16, message = "Project name must be 3–16 characters.")
    private String projectName;

    @NotBlank(message = "Project code is required.")
    @Size(min = 5, max = 5, message = "Project code must be exactly 5 characters.")
    private String projectCode;

    @NotNull(message = "Start date is required.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "End date is required.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String projectDetail;
}
