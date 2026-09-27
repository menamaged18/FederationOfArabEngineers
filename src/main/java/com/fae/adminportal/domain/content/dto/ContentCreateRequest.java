package com.fae.adminportal.domain.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;

public record ContentCreateRequest(
    @NotNull(message = "contentTypeId is required") Integer contentTypeId,
    @NotBlank @Size(max = 255) String title,
    String slug,                 // optional; auto-generated from title
    String description,
    String imagePath,
    LocalDateTime eventDate,
    Boolean isPublished,
    Set<Long> fileIds
) {}