package com.fae.adminportal.domain.user.entity;

/**
 * Canonical values for roles.slug.
 * Keep in sync with the seed/migration data.
 */
public final class RoleSlug {

    public static final String SUPER_ADMIN = "super_admin";
    public static final String MEMBER      = "member";

    private RoleSlug() {
    }
}

// Usage in security: hasRole("SUPER_ADMIN") maps to authority ROLE_super_admin