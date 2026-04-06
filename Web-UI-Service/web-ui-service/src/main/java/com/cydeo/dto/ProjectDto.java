package com.cydeo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDto {

    private String projectName;
    private String projectCode;
    private String assignedManager;
    private LocalDate startDate;
    private LocalDate endDate;
    private String projectDetail;
    private String projectStatus;
    private Integer completedTaskCount;
    private Integer nonCompletedTaskCount;
}
