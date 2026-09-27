package com.fae.adminportal.domain.content.service;

import com.fae.adminportal.common.exception.BadRequestException;
import com.fae.adminportal.common.exception.ResourceNotFoundException;
import com.fae.adminportal.common.util.SlugUtils;
import com.fae.adminportal.domain.content.dto.ContentTypeRequest;
import com.fae.adminportal.domain.content.dto.ContentTypeResponse;
import com.fae.adminportal.domain.content.entity.ContentType;
import com.fae.adminportal.domain.content.repository.ContentTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentTypeService {

    private final ContentTypeRepository contentTypeRepository;

    @Transactional(readOnly = true)
    public List<ContentTypeResponse> findAll() {
        return contentTypeRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContentTypeResponse findById(Integer id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public ContentTypeResponse findBySlug(String slug) {
        return contentTypeRepository.findBySlug(slug)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Content type not found: " + slug));
    }

    @Transactional
    public ContentTypeResponse create(ContentTypeRequest request) {
        String slug = (request.slug() == null || request.slug().isBlank())
                ? SlugUtils.toSlug(request.name())
                : SlugUtils.toSlug(request.slug());

        if (contentTypeRepository.existsByName(request.name())) {
            throw new BadRequestException("Content type name already exists");
        }
        if (contentTypeRepository.existsBySlug(slug)) {
            throw new BadRequestException("Content type slug already exists");
        }

        ContentType type = ContentType.builder()
                .name(request.name())
                .slug(slug)
                .build();
        return toResponse(contentTypeRepository.save(type));
    }

    @Transactional
    public ContentTypeResponse update(Integer id, ContentTypeRequest request) {
        ContentType type = getOrThrow(id);
        String slug = (request.slug() == null || request.slug().isBlank())
                ? SlugUtils.toSlug(request.name())
                : SlugUtils.toSlug(request.slug());

        if (!type.getName().equals(request.name()) && contentTypeRepository.existsByName(request.name())) {
            throw new BadRequestException("Content type name already exists");
        }
        if (!type.getSlug().equals(slug) && contentTypeRepository.existsBySlug(slug)) {
            throw new BadRequestException("Content type slug already exists");
        }

        type.setName(request.name());
        type.setSlug(slug);
        return toResponse(contentTypeRepository.save(type));
    }

    @Transactional
    public void delete(Integer id) {
        ContentType type = getOrThrow(id);
        contentTypeRepository.delete(type);
        // DB FK RESTRICT will prevent deletion if it's referenced by contents.
    }

    /* ---------- helpers ---------- */

    private ContentType getOrThrow(Integer id) {
        return contentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content type not found: " + id));
    }

    private ContentTypeResponse toResponse(ContentType t) {
        return new ContentTypeResponse(t.getId(), t.getName(), t.getSlug());
    }
}