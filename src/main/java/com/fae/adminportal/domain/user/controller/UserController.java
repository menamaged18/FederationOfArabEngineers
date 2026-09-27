package com.fae.adminportal.domain.user.controller;

import com.fae.adminportal.common.dto.ApiResponse;
import com.fae.adminportal.common.dto.PagedResponse;
import com.fae.adminportal.domain.user.dto.CreateUserRequest;
import com.fae.adminportal.domain.user.dto.UpdateUserRequest;
import com.fae.adminportal.domain.user.dto.UserResponse;
import com.fae.adminportal.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_super_admin')")
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse created = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User created", created));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_super_admin')")
    public ApiResponse<PagedResponse<UserResponse>> list(
            @RequestParam(name = "q", required = false) String q,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ApiResponse.ok(userService.list(q, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_super_admin') or #id == authentication.principal.id")
    public ApiResponse<UserResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(userService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_super_admin')")
    public ApiResponse<UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.ok("User updated", userService.update(id, request));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('ROLE_super_admin')")
    public ApiResponse<UserResponse> activate(@PathVariable Long id) {
        return ApiResponse.ok("User activated", userService.setActive(id, true));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('ROLE_super_admin')")
    public ApiResponse<UserResponse> deactivate(@PathVariable Long id) {
        return ApiResponse.ok("User deactivated", userService.setActive(id, false));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_super_admin')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.ok("User deleted", null);
    }
}