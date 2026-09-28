package com.fae.adminportal.domain.content.service;

import com.fae.adminportal.common.dto.PagedResponse;
import com.fae.adminportal.common.exception.BadRequestException;
import com.fae.adminportal.common.exception.ResourceNotFoundException;
import com.fae.adminportal.common.util.SlugUtils;
import com.fae.adminportal.domain.content.dto.ContentCreateRequest;
import com.fae.adminportal.domain.content.dto.ContentResponse;
import com.fae.adminportal.domain.content.dto.ContentUpdateRequest;
import com.fae.adminportal.domain.content.entity.Content;
import com.fae.adminportal.domain.content.entity.ContentType;
import com.fae.adminportal.domain.content.repository.ContentRepository;
import com.fae.adminportal.domain.content.repository.ContentTypeRepository;
import com.fae.adminportal.domain.file.entity.FileAsset;
import com.fae.adminportal.domain.file.repository.FileRepository;
import com.fae.adminportal.domain.user.entity.User;
import com.fae.adminportal.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final ContentTypeRepository contentTypeRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    /* ---------- Reads ---------- */

    @Transactional(readOnly = true)
    public PagedResponse<ContentResponse> search(Integer contentTypeId,
                                                 String typeSlug,
                                                 Boolean isPublished,
                                                 String search,
                                                 Pageable pageable) {
        String cleanedSearch = (search == null || search.isBlank()) ? null : search.trim();
        Page<Content> page = contentRepository.search(
                contentTypeId, typeSlug, isPublished, cleanedSearch, pageable);
        return PagedResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public ContentResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public ContentResponse findBySlug(String slug) {
        return contentRepository.findBySlug(slug)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found: " + slug));
    }

    /* ---------- Writes ---------- */

    @Transactional
    public ContentResponse create(ContentCreateRequest request) {
        ContentType type = contentTypeRepository.findById(request.contentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Content type not found: " + request.contentTypeId()));

        String slug = resolveUniqueSlug(request.slug(), request.title(), null);

        Content content = Content.builder()
                .contentType(type)
                .title(request.title())
                .slug(slug)
                .description(request.description())
                .imagePath(request.imagePath())
                .eventDate(request.eventDate())
                .isPublished(request.isPublished() == null ? Boolean.TRUE : request.isPublished())
                .createdBy(currentUserOrNull())
                .files(resolveFiles(request.fileIds()))
                .build();

        return toResponse(contentRepository.save(content));
    }

    @Transactional
    public ContentResponse update(Long id, ContentUpdateRequest request) {
        Content content = getOrThrow(id);

        if (request.contentTypeId() != null
                && !request.contentTypeId().equals(content.getContentType().getId())) {
            ContentType newType = contentTypeRepository.findById(request.contentTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Content type not found: " + request.contentTypeId()));
            content.setContentType(newType);
        }

        if (request.title() != null && !request.title().isBlank()) {
            content.setTitle(request.title());
        }

        // Re-slug only if title/slug changed
        if (request.slug() != null || (request.title() != null && !request.title().isBlank())) {
            content.setSlug(resolveUniqueSlug(request.slug(), content.getTitle(), content.getId()));
        }

        if (request.description() != null) content.setDescription(request.description());
        if (request.imagePath() != null)   content.setImagePath(request.imagePath());
        if (request.eventDate() != null)   content.setEventDate(request.eventDate());
        if (request.isPublished() != null) content.setIsPublished(request.isPublished());

        if (request.fileIds() != null) {
            content.setFiles(resolveFiles(request.fileIds()));
        }

        return toResponse(contentRepository.save(content));
    }

    @Transactional
    public void delete(Long id) {
        Content content = getOrThrow(id);
        contentRepository.delete(content);
    }

    /* ---------- Helpers ---------- */

    private Content getOrThrow(Long id) {
        return contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found: " + id));
    }

    private String resolveUniqueSlug(String requestedSlug, String title, Long currentId) {
        String base = (requestedSlug == null || requestedSlug.isBlank())
                ? SlugUtils.toSlug(title)
                : SlugUtils.toSlug(requestedSlug);

        String candidate = base;
        int suffix = 1;
        while (true) {
            var existing = contentRepository.findBySlug(candidate);
            if (existing.isEmpty() || existing.get().getId().equals(currentId)) {
                return candidate;
            }
            candidate = base + "-" + (++suffix);
        }
    }

    private Set<FileAsset> resolveFiles(Set<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) return new HashSet<>();
        List<FileAsset> found = fileRepository.findAllById(fileIds);
        if (found.size() != fileIds.size()) {
            throw new BadRequestException("One or more file IDs are invalid");
        }
        return new HashSet<>(found);
    }

    private User currentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    @Transactional
    public ContentResponse setPublished(Long id, boolean published) {
        Content c = contentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Content", id));
        c.setIsPublished(published);
        return toResponse(c);
    }

    private ContentResponse toResponse(Content c) {
        List<ContentResponse.FileSummary> files = c.getFiles() == null
                ? Collections.emptyList()
                : c.getFiles().stream()
                    .map(f -> new ContentResponse.FileSummary(
                            f.getId(), f.getOriginalName(), f.getFilePath(),
                            f.getMimeType(), f.getFileSize()))
                    .toList();

        return new ContentResponse(
                c.getId(),
                c.getContentType() != null ? c.getContentType().getId() : null,
                c.getContentType() != null ? c.getContentType().getName() : null,
                c.getContentType() != null ? c.getContentType().getSlug() : null,
                c.getTitle(),
                c.getSlug(),
                c.getDescription(),
                c.getImagePath(),
                c.getEventDate(),
                c.getIsPublished(),
                c.getCreatedBy() != null ? c.getCreatedBy().getId() : null,
                c.getCreatedBy() != null ? c.getCreatedBy().getName() : null,
                c.getCreatedAt(),
                c.getUpdatedAt(),
                files
        );
    }
}