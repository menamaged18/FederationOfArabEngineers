package com.fae.adminportal.domain.contact.controller;

import com.fae.adminportal.common.dto.ApiResponse;
import com.fae.adminportal.common.dto.PagedResponse;
import com.fae.adminportal.domain.contact.dto.ContactUsRequest;
import com.fae.adminportal.domain.contact.dto.ContactUsResponse;
import com.fae.adminportal.domain.contact.dto.ReviewContactRequest;
import com.fae.adminportal.domain.contact.entity.ContactUs;
import com.fae.adminportal.domain.contact.service.ContactUsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contact-us")
@RequiredArgsConstructor
@Tag(name = "Contact Us", description = "Public inquiries and admin review workflow")
public class ContactUsController {

    private final ContactUsService contactUsService;

    /* ---------- Public ---------- */

    @PostMapping
    public ApiResponse<ContactUsResponse> submit(@Valid @RequestBody ContactUsRequest request) {
        return ApiResponse.ok("Message received. We will get back to you soon.",
                contactUsService.submit(request));
    }

    /* ---------- Admin ---------- */

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<PagedResponse<ContactUsResponse>> list(
            @RequestParam(required = false) ContactUs.Status status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ApiResponse.ok(contactUsService.search(status, search, pageable));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<Map<String, Long>> stats() {
        return ApiResponse.ok(contactUsService.countByStatus());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<ContactUsResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(contactUsService.findById(id));
    }

    @PatchMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<ContactUsResponse> review(@PathVariable Long id,
                                                 @Valid @RequestBody ReviewContactRequest request) {
        return ApiResponse.ok("Message reviewed", contactUsService.review(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        contactUsService.delete(id);
        return ApiResponse.ok("Message deleted", null);
    }
}