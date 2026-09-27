package com.fae.adminportal.domain.user.service;

import com.fae.adminportal.common.dto.PagedResponse;
import com.fae.adminportal.common.exception.BadRequestException;
import com.fae.adminportal.common.exception.ResourceNotFoundException;
import com.fae.adminportal.domain.user.dto.CreateUserRequest;
import com.fae.adminportal.domain.user.dto.RoleSummary;
import com.fae.adminportal.domain.user.dto.UpdateUserRequest;
import com.fae.adminportal.domain.user.dto.UserResponse;
import com.fae.adminportal.domain.user.entity.Role;
import com.fae.adminportal.domain.user.entity.User;
import com.fae.adminportal.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    // ---- create ----

    @Transactional
    public UserResponse create(CreateUserRequest req) {
        String email = normalizeEmail(req.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("Email already in use: " + email);
        }

        Role role = roleService.findOrThrow(req.roleId());

        User user = User.builder()
                .name(req.name().trim())
                .email(email)
                .phone(StringUtils.hasText(req.phone()) ? req.phone().trim() : null)
                .password(passwordEncoder.encode(req.password()))
                .role(role)
                .active(req.active() == null || req.active())
                .build();

        return toResponse(userRepository.save(user));
    }

    // ---- read ----

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> list(String q, Pageable pageable) {
        Page<User> page = StringUtils.hasText(q)
                ? userRepository.search(q.trim(), pageable)
                : userRepository.findAll(pageable);
        return PagedResponse.from(page, this::toResponse);
    }

    // ---- update ----

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest req) {
        User user = findOrThrow(id);
        String email = normalizeEmail(req.email());

        userRepository.findByEmailIgnoreCase(email)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new BadRequestException("Email already in use: " + email);
                });

        user.setName(req.name().trim());
        user.setEmail(email);
        user.setPhone(StringUtils.hasText(req.phone()) ? req.phone().trim() : null);

        if (req.roleId() != null && !req.roleId().equals(user.getRole().getId())) {
            user.setRole(roleService.findOrThrow(req.roleId()));
        }

        return toResponse(user);
    }

    @Transactional
    public UserResponse setActive(Long id, boolean active) {
        User user = findOrThrow(id);
        user.setActive(active);
        return toResponse(user);
    }

    @Transactional
    public void changePassword(Long id, String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters");
        }
        User user = findOrThrow(id);
        user.setPassword(passwordEncoder.encode(rawPassword));
    }

    @Transactional
    public void delete(Long id) {
        User user = findOrThrow(id);
        userRepository.delete(user);
    }

    // ---- helpers ----

    User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }

    UserResponse toResponse(User u) {
        Role r = u.getRole();
        return new UserResponse(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getPhone(),
                u.isActive(),
                r == null ? null : new RoleSummary(r.getId(), r.getName(), r.getSlug()),
                u.getCreatedAt(),
                u.getUpdatedAt()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}