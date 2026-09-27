package com.fae.adminportal.domain.file.dto;

import java.time.LocalDateTime;

public record FileResponse(
    Long id,
    String originalName,
    String filePath,
    String url,
    String mimeType,
    Integer fileSize,
    Long uploadedById,
    String uploadedByName,
    LocalDateTime createdAt
) {}

// url = publicBaseUrl + "/" + filePath — e.g. /uploads/2025/01/9c3b...jpg.