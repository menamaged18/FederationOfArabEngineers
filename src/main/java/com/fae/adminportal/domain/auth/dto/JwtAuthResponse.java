package com.fae.adminportal.domain.auth.dto;

import com.fae.adminportal.domain.user.dto.RoleSummary;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JwtAuthResponse(
        String accessToken,
        String tokenType,     // "Bearer"
        long expiresIn,       // seconds — convenient for the SPA
        UserSummary user
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record UserSummary(
            Long id,
            String name,
            String email,
            RoleSummary role
    ) {
    }
}