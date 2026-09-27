package com.fae.adminportal.domain.user.dto;

import java.time.LocalDateTime;

public record RoleResponse(
        Integer id,
        String name,
        String slug,
        String description,
        LocalDateTime createdAt
) {
}