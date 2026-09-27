package com.fae.adminportal.domain.user.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Maps to the `users` table.
 * Role is a real FK (role_id) instead of a hardcoded ENUM.
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
        indexes = {
                @Index(name = "idx_users_role_id", columnList = "role_id"),
                @Index(name = "idx_users_is_active", columnList = "is_active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    /**
     * FK -> roles.id (ON DELETE RESTRICT).
     * LAZY + optional=false: a user can never exist without a role.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "role_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_users_role_id")
    )
    private Role role;

    @Column(name = "name", nullable = false, length = 100)
    @ToString.Include
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    @ToString.Include
    private String email;

    @Column(name = "phone", length = 30)
    private String phone;

    /** BCrypt/Argon2 hash — never the raw password. */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ---- convenience helpers (keep bidirectional graph consistent in memory) ----

    public void assignRole(Role role) {
        this.role = role;
        if (role != null) {
            role.getUsers().add(this);
        }
    }

    public void deactivate() {
        this.active = false;
    }
}