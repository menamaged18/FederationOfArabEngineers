package com.fae.adminportal.domain.user.dto;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        String phone,
        boolean active,
        RoleSummary role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}