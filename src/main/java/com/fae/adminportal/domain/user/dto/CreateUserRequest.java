package com.fae.adminportal.domain.user.dto;

import jakarta.validation.constraints.*;

public record CreateUserRequest(

        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Email
        @Size(max = 150)
        String email,

        @Size(max = 30)
        String phone,

        @NotBlank
        @Size(min = 8, max = 72)
        String password,

        @NotNull
        Integer roleId,

        Boolean active
) {
}