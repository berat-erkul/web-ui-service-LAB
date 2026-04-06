package com.cydeo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {

    private String firstName;
    private String lastName;
    private String userName;
    private String phone;
    private boolean enabled;
    private RoleDto role;
    private String gender;
}
