package com.fae.adminportal.domain.content.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ContentResponse(
    Long id,
    Integer contentTypeId,
    String contentTypeName,
    String contentTypeSlug,
    String title,
    String slug,
    String description,
    String imagePath,
    LocalDateTime eventDate,
    Boolean isPublished,
    Long createdById,
    String createdByName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<FileSummary> files
) {
    public record FileSummary(
        Long id,
        String originalName,
        String filePath,
        String mimeType,
        Integer fileSize
    ) {}
}