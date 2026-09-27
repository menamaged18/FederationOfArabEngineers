package com.fae.adminportal.domain.content.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;

public record ContentUpdateRequest(
    Integer contentTypeId,
    @Size(max = 255) String title,
    String slug,
    String description,
    String imagePath,
    LocalDateTime eventDate,
    Boolean isPublished,
    Set<Long> fileIds
) {}