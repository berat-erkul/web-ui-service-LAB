package com.cydeo.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirrors backend ValidationExceptionWrapper.
 * Present inside ExceptionWrapper.validationExceptions on HTTP 400 responses.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationExceptionWrapper {

    private String errorField;
    private Object rejectedValue;
    private String reason;
}
