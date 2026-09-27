package com.fae.adminportal.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateRoleRequest(

        @NotBlank
        @Size(max = 50)
        String name,

        @Size(max = 255)
        String description
) {
    // slug is intentionally immutable — it's the machine key that Spring Security relies on
}