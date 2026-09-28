package com.fae.adminportal.domain.user.controller.api;

import com.fae.adminportal.common.dto.ApiResponse;
import com.fae.adminportal.common.dto.PagedResponse;
import com.fae.adminportal.domain.user.dto.CreateRoleRequest;
import com.fae.adminportal.domain.user.dto.RoleResponse;
import com.fae.adminportal.domain.user.dto.UpdateRoleRequest;
import com.fae.adminportal.domain.user.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST API endpoints for role management.
 * Base path: /api/v1/roles
 * <p>
 * Authenticated via JWT (see {@code JwtAuthenticationFilter}).
 */
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_super_admin')")
public class RoleApiController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> create(
            @Valid @RequestBody CreateRoleRequest request) {
        RoleResponse created = roleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Role created", created));
    }

    @GetMapping
    public ApiResponse<PagedResponse<RoleResponse>> list(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return ApiResponse.ok(roleService.list(pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<RoleResponse> getById(@PathVariable Integer id) {
        return ApiResponse.ok(roleService.getById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<RoleResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateRoleRequest request) {
        return ApiResponse.ok("Role updated", roleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        roleService.delete(id);
        return ApiResponse.ok("Role deleted", null);
    }
}