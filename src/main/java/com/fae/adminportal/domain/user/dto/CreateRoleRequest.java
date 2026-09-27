package com.fae.adminportal.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateRoleRequest(

        @NotBlank
        @Size(max = 50)
        String name,

        @NotBlank
        @Size(max = 50)
        @Pattern(
                regexp = "^[a-z][a-z0-9_]*$",
                message = "slug must be lowercase alphanumeric/underscore, starting with a letter"
        )
        String slug,

        @Size(max = 255)
        String description
) {
}