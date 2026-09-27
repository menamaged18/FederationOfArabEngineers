package com.fae.adminportal.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Maps to the `roles` table.
 * Machine key is `slug` (e.g. "super_admin"); `name` is the display label.
 */
@Entity
@Table(
        name = "roles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_roles_name", columnNames = "name"),
                @UniqueConstraint(name = "uk_roles_slug", columnNames = "slug")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Integer id;

    /** Display name: 'Super Admin', 'Member' */
    @Column(name = "name", nullable = false, unique = true, length = 50)
    @ToString.Include
    private String name;

    /** Machine key: 'super_admin', 'member' — used by Spring Security authorities. */
    @Column(name = "slug", nullable = false, unique = true, length = 50)
    @ToString.Include
    private String slug;

    @Column(name = "description", length = 255)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Inverse side — never cascade deletes from here (DB uses ON DELETE RESTRICT). */
    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<User> users = new HashSet<>();
}