package com.fae.adminportal.domain.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContentTypeRequest(
    @NotBlank @Size(max = 50) String name,
    @Size(max = 50) String slug  // optional → auto-generated from name
) {}