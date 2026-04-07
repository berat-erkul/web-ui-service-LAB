package com.cydeo.util;

import com.cydeo.wrapper.ExceptionWrapper;
import com.cydeo.wrapper.ValidationExceptionWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Parses WebClient 4xx/5xx error responses into a single user-facing message string.
 *
 * Usage in a gateway client:
 *   } catch (WebClientResponseException ex) {
 *       throw new GatewayException(gatewayErrorHandler.extractMessage(ex));
 *   }
 *
 * Or directly in a controller:
 *   } catch (WebClientResponseException ex) {
 *       model.addAttribute("fetchError", gatewayErrorHandler.extractMessage(ex));
 *   }
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayErrorHandler {

    private final ObjectMapper objectMapper;

    /**
     * Extracts a user-facing message from a WebClientResponseException.
     * For 400 validation errors, concatenates every field-level reason.
     * For all other errors, returns the backend message or a generic fallback.
     */
    public String extractMessage(WebClientResponseException ex) {
        String body = ex.getResponseBodyAsString();
        if (body != null && !body.isBlank()) {
            try {
                ExceptionWrapper wrapper = objectMapper.readValue(body, ExceptionWrapper.class);
                List<ValidationExceptionWrapper> validations = wrapper.getValidationExceptions();
                if (validations != null && !validations.isEmpty()) {
                    return validations.stream()
                            .map(v -> v.getErrorField() + ": " + v.getReason())
                            .collect(Collectors.joining("; "));
                }
                if (wrapper.getMessage() != null && !wrapper.getMessage().isBlank()) {
                    return wrapper.getMessage();
                }
            } catch (Exception parseEx) {
                log.debug("Could not parse error body as ExceptionWrapper: {}", parseEx.getMessage());
            }
        }
        return "Request failed (" + ex.getStatusCode().value() + " " + ex.getStatusText() + ").";
    }

    /**
     * Convenience overload for general Throwable — delegates to the typed overload
     * when the cause is a WebClientResponseException, otherwise returns a generic message.
     */
    public String extractMessage(Throwable ex) {
        if (ex instanceof WebClientResponseException) {
            return extractMessage((WebClientResponseException) ex);
        }
        Throwable cause = ex.getCause();
        if (cause instanceof WebClientResponseException) {
            return extractMessage((WebClientResponseException) cause);
        }
        log.warn("Non-HTTP error communicating with gateway: {}", ex.getMessage());
        return "Could not connect to the backend service. Please try again later.";
    }
}
