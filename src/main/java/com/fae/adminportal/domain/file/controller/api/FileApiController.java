package com.fae.adminportal.domain.file.controller.api;

import com.fae.adminportal.common.dto.ApiResponse;
import com.fae.adminportal.common.dto.PagedResponse;
import com.fae.adminportal.domain.file.dto.FileResponse;
import com.fae.adminportal.domain.file.entity.FileAsset;
import com.fae.adminportal.domain.file.service.FileStorageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * REST API endpoints for file upload, metadata and download.
 * Base path: /api/v1/files
 * <p>
 * Uploads and deletes require {@code ROLE_super_admin}.
 * Metadata + download are public — the frontend embeds file URLs directly.
 */
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "Files", description = "File upload, metadata and download")
public class FileApiController {

    private final FileStorageService fileStorageService;

    /* ---------- Upload ---------- */

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_super_admin')")
    public ApiResponse<FileResponse> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok("File uploaded", fileStorageService.store(file));
    }

    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_super_admin')")
    public ApiResponse<List<FileResponse>> uploadMultiple(
            @RequestParam("files") MultipartFile[] files) {
        List<FileResponse> result = Arrays.stream(files)
                .map(fileStorageService::store)
                .toList();
        return ApiResponse.ok("Files uploaded", result);
    }

    /* ---------- Metadata ---------- */

    @GetMapping
    public ApiResponse<PagedResponse<FileResponse>> list(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ApiResponse.ok(fileStorageService.list(pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<FileResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(fileStorageService.findById(id));
    }

    /* ---------- Content streaming ---------- */

    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> view(@PathVariable Long id) {
        FileAsset asset = fileStorageService.getAsset(id);
        Resource resource = fileStorageService.loadAsResource(id);
        return ResponseEntity.ok()
                .contentType(resolveMediaType(asset.getMimeType()))
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(resource);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        FileAsset asset = fileStorageService.getAsset(id);
        Resource resource = fileStorageService.loadAsResource(id);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(asset.getOriginalName())
                .build();
        return ResponseEntity.ok()
                .contentType(resolveMediaType(asset.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resource);
    }

    /* ---------- Delete ---------- */

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_super_admin')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        fileStorageService.delete(id);
        return ApiResponse.ok("File deleted", null);
    }

    /* ---------- Helpers ---------- */

    private MediaType resolveMediaType(String mime) {
        if (mime == null || mime.isBlank()) return MediaType.APPLICATION_OCTET_STREAM;
        try {
            return MediaType.parseMediaType(mime);
        } catch (Exception ex) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}