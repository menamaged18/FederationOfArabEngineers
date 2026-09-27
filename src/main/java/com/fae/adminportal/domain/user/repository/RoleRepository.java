package com.fae.adminportal.domain.user.repository;

import com.fae.adminportal.domain.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findBySlug(String slug);

    Optional<Role> findByName(String name);

    boolean existsBySlug(String slug);
}