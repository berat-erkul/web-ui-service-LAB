package com.cydeo.client;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Typed result returned by every gateway client mutation (create / update / delete)
 * and single-item reads used for edit forms.
 *
 * Controllers check isSuccess() before accessing getData() / getErrorMessage().
 *
 * Example:
 *   ClientResult<UserDto> result = userGatewayClient.createUser(form);
 *   if (!result.isSuccess()) {
 *       model.addAttribute("errorMessage", result.getErrorMessage());
 *       return "users/create";
 *   }
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientResult<T> {

    private final boolean success;
    private final T data;
    private final String errorMessage;

    /** Successful result carrying a response body. */
    public static <T> ClientResult<T> ok(T data) {
        return new ClientResult<>(true, data, null);
    }

    /** Successful result with no body (e.g. 204 DELETE). */
    public static <T> ClientResult<T> ok() {
        return new ClientResult<>(true, null, null);
    }

    /** Failed result with a user-facing error message. */
    public static <T> ClientResult<T> error(String message) {
        return new ClientResult<>(false, null, message);
    }
}
