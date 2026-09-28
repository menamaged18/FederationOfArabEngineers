package com.fae.adminportal.domain.content.controller.api;

import com.fae.adminportal.common.dto.ApiResponse;
import com.fae.adminportal.domain.content.dto.ContentTypeRequest;
import com.fae.adminportal.domain.content.dto.ContentTypeResponse;
import com.fae.adminportal.domain.content.service.ContentTypeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API endpoints for content types.
 * Base path: /api/v1/content-types
 */
@RestController
@RequestMapping("/api/v1/content-types")
@RequiredArgsConstructor
@Tag(name = "Content Types", description = "Manage dynamic content types")
public class ContentTypeApiController {

    private final ContentTypeService contentTypeService;

    @GetMapping
    public ApiResponse<List<ContentTypeResponse>> list() {
        return ApiResponse.ok(contentTypeService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<ContentTypeResponse> getById(@PathVariable Integer id) {
        return ApiResponse.ok(contentTypeService.findById(id));
    }

    @GetMapping("/slug/{slug}")
    public ApiResponse<ContentTypeResponse> getBySlug(@PathVariable String slug) {
        return ApiResponse.ok(contentTypeService.findBySlug(slug));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_super_admin')")
    public ApiResponse<ContentTypeResponse> create(@Valid @RequestBody ContentTypeRequest request) {
        return ApiResponse.ok("Content type created", contentTypeService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_super_admin')")
    public ApiResponse<ContentTypeResponse> update(@PathVariable Integer id,
                                                   @Valid @RequestBody ContentTypeRequest request) {
        return ApiResponse.ok("Content type updated", contentTypeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_super_admin')")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        contentTypeService.delete(id);
        return ApiResponse.ok("Content type deleted", null);
    }
}