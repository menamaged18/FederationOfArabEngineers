package com.fae.adminportal.domain.user.service;

import com.fae.adminportal.common.dto.PagedResponse;
import com.fae.adminportal.common.exception.BadRequestException;
import com.fae.adminportal.common.exception.ResourceNotFoundException;
import com.fae.adminportal.domain.user.dto.CreateRoleRequest;
import com.fae.adminportal.domain.user.dto.RoleResponse;
import com.fae.adminportal.domain.user.dto.UpdateRoleRequest;
import com.fae.adminportal.domain.user.dto.RoleSummary;
import com.fae.adminportal.domain.user.entity.Role;
import com.fae.adminportal.domain.user.repository.RoleRepository;
import com.fae.adminportal.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Transactional
    public RoleResponse create(CreateRoleRequest req) {
        if (roleRepository.existsBySlug(req.slug())) {
            throw new BadRequestException("Role slug already exists: " + req.slug());
        }
        if (roleRepository.findByName(req.name()).isPresent()) {
            throw new BadRequestException("Role name already exists: " + req.name());
        }

        Role role = Role.builder()
                .name(req.name())
                .slug(req.slug())
                .description(req.description())
                .build();

        return toResponse(roleRepository.save(role));
    }

    @Transactional(readOnly = true)
    public RoleResponse getById(Integer id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public RoleResponse getBySlug(String slug) {
        Role role = roleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + slug));
        return toResponse(role);
    }

    @Transactional(readOnly = true)
    public PagedResponse<RoleResponse> list(Pageable pageable) {
        return PagedResponse.from(roleRepository.findAll(pageable), this::toResponse);
    }

    @Transactional
    public RoleResponse update(Integer id, UpdateRoleRequest req) {
        Role role = findOrThrow(id);

        if (!role.getName().equals(req.name())
                && roleRepository.findByName(req.name()).isPresent()) {
            throw new BadRequestException("Role name already exists: " + req.name());
        }

        role.setName(req.name());
        role.setDescription(req.description());
        // slug is immutable — enforcing this in the API surface, not the DB
        return toResponse(role);
    }

    @Transactional
    public void delete(Integer id) {
        Role role = findOrThrow(id);
        if (userRepository.existsByRoleId(id)) {
            throw new BadRequestException(
                    "Cannot delete role '" + role.getSlug() + "': users are still assigned to it");
        }
        roleRepository.delete(role);
    }

    // ---- helpers ----

    public List<RoleSummary> listAll() {
        return roleRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(r -> new RoleSummary(r.getId(), r.getName(), r.getSlug()))
                .toList();
    }

    Role findOrThrow(Integer id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Role", id));
    }

    RoleResponse toResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getSlug(),
                role.getDescription(),
                role.getCreatedAt()
        );
    }
}