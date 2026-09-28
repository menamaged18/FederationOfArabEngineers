package com.fae.adminportal.domain.content.controller.api;

import com.fae.adminportal.common.dto.ApiResponse;
import com.fae.adminportal.common.dto.PagedResponse;
import com.fae.adminportal.domain.content.dto.ContentCreateRequest;
import com.fae.adminportal.domain.content.dto.ContentResponse;
import com.fae.adminportal.domain.content.dto.ContentUpdateRequest;
import com.fae.adminportal.domain.content.service.ContentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST API endpoints for content.
 * Base path: /api/v1/contents
 * <p>
 * Reads are public (news/meetings/committees are shown on the public site).
 * Writes require {@code ROLE_super_admin}.
 */
@RestController
@RequestMapping("/api/v1/contents")
@RequiredArgsConstructor
@Tag(name = "Contents", description = "Manage news, meetings, committees, specialized bodies")
public class ContentApiController {

    private final ContentService contentService;

    /* ------------------------------------------------------------------ */
    /*  Public reads                                                       */
    /* ------------------------------------------------------------------ */

    @GetMapping
    public ApiResponse<PagedResponse<ContentResponse>> list(
            @RequestParam(required = false) Integer contentTypeId,
            @RequestParam(required = false) String typeSlug,
            @RequestParam(required = false) Boolean isPublished,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return ApiResponse.ok(contentService.search(
                contentTypeId, typeSlug, isPublished, search, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<ContentResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(contentService.findById(id));
    }

    @GetMapping("/slug/{slug}")
    public ApiResponse<ContentResponse> getBySlug(@PathVariable String slug) {
        return ApiResponse.ok(contentService.findBySlug(slug));
    }

    /* ------------------------------------------------------------------ */
    /*  Writes — super_admin only                                          */
    /* ------------------------------------------------------------------ */

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_super_admin')")
    public ApiResponse<ContentResponse> create(@Valid @RequestBody ContentCreateRequest request) {
        return ApiResponse.ok("Content created", contentService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_super_admin')")
    public ApiResponse<ContentResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody ContentUpdateRequest request) {
        return ApiResponse.ok("Content updated", contentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_super_admin')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        contentService.delete(id);
        return ApiResponse.ok("Content deleted", null);
    }
}