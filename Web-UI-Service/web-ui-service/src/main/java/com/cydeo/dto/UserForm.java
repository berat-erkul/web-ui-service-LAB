package com.cydeo.dto;

import com.cydeo.enums.Gender;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Form model for User create and update.
 * Mirrors UserDTO validation constraints from the backend.
 * password is required by the backend on both create and update.
 */
@Data
@NoArgsConstructor
public class UserForm {

    @NotBlank(message = "First name is required.")
    @Size(min = 3, max = 16, message = "First name must be 3–16 characters.")
    private String firstName;

    @NotBlank(message = "Last name is required.")
    @Size(min = 3, max = 16, message = "Last name must be 3–16 characters.")
    private String lastName;

    @NotBlank(message = "Username (email) is required.")
    @Email(message = "Username must be a valid email address.")
    @Size(min = 3, max = 16, message = "Username must be 3–16 characters.")
    private String userName;

    @NotBlank(message = "Password is required.")
    @Pattern(
        regexp = "(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{4,}",
        message = "Password must be ≥ 4 characters and contain at least one uppercase letter, one lowercase letter, and one digit."
    )
    private String password;

    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits.")
    private String phone;

    private boolean enabled = true;

    /** Maps to RoleDTO.description — e.g. "Admin", "Manager", "Employee" */
    @NotBlank(message = "Role is required.")
    private String roleDescription;

    @NotNull(message = "Gender is required.")
    private Gender gender;
}
