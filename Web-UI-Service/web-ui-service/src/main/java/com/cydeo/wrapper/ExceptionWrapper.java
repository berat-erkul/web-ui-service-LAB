package com.cydeo.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Mirrors backend ExceptionWrapper.
 * Returned by the Gateway/backend on 4xx and 5xx responses.
 *
 * Shape:
 * {
 *   "success": false,
 *   "message": "...",
 *   "httpStatus": "NOT_FOUND",
 *   "localDateTime": "2024-01-15T10:30:00",
 *   "errorCount": 2,                          // only on 400
 *   "validationExceptions": [ ... ]           // only on 400
 * }
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExceptionWrapper {

    private boolean success;
    private String message;
    private String httpStatus;
    private LocalDateTime localDateTime;
    private Integer errorCount;
    private List<ValidationExceptionWrapper> validationExceptions;
}
