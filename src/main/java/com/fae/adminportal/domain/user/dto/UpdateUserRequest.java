package com.fae.adminportal.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Email
        @Size(max = 150)
        String email,

        @Size(max = 30)
        String phone,

        // roleId is nullable — omit to keep current role
        Integer roleId
) {
    // password change is a separate endpoint
}