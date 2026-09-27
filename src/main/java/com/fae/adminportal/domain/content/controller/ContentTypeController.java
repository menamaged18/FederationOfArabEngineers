package com.fae.adminportal.domain.content.controller;

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

@RestController
@RequestMapping("/api/content-types")
@RequiredArgsConstructor
@Tag(name = "Content Types", description = "Manage dynamic content types")
public class ContentTypeController {

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
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<ContentTypeResponse> create(@Valid @RequestBody ContentTypeRequest request) {
        return ApiResponse.ok("Content type created", contentTypeService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<ContentTypeResponse> update(@PathVariable Integer id,
                                                   @Valid @RequestBody ContentTypeRequest request) {
        return ApiResponse.ok("Content type updated", contentTypeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        contentTypeService.delete(id);
        return ApiResponse.ok("Content type deleted", null);
    }
}