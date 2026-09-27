package com.fae.adminportal.domain.contact.service;

import com.fae.adminportal.common.dto.PagedResponse;
import com.fae.adminportal.common.exception.BadRequestException;
import com.fae.adminportal.common.exception.ResourceNotFoundException;
import com.fae.adminportal.domain.contact.dto.ContactUsRequest;
import com.fae.adminportal.domain.contact.dto.ContactUsResponse;
import com.fae.adminportal.domain.contact.dto.ReviewContactRequest;
import com.fae.adminportal.domain.contact.entity.ContactUs;
import com.fae.adminportal.domain.contact.repository.ContactUsRepository;
import com.fae.adminportal.domain.user.entity.User;
import com.fae.adminportal.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContactUsService {

    private final ContactUsRepository contactUsRepository;
    private final UserRepository userRepository;

    /* ---------- Public ---------- */

    @Transactional
    public ContactUsResponse submit(ContactUsRequest request) {
        ContactUs entity = ContactUs.builder()
                .name(request.name().trim())
                .email(request.email().trim().toLowerCase())
                .phone(request.phone() == null ? null : request.phone().trim())
                .message(request.message().trim())
                .status(ContactUs.Status.pending)
                .build();

        return toResponse(contactUsRepository.save(entity));
    }

    /* ---------- Admin reads ---------- */

    @Transactional(readOnly = true)
    public PagedResponse<ContactUsResponse> search(ContactUs.Status status,
                                                   String search,
                                                   Pageable pageable) {
        String cleaned = (search == null || search.isBlank()) ? null : search.trim();
        Page<ContactUs> page = contactUsRepository.search(status, cleaned, pageable);
        return PagedResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public ContactUsResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> countByStatus() {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("pending",  contactUsRepository.countByStatus(ContactUs.Status.pending));
        counts.put("reviewed", contactUsRepository.countByStatus(ContactUs.Status.reviewed));
        counts.put("resolved", contactUsRepository.countByStatus(ContactUs.Status.resolved));
        counts.put("total",    contactUsRepository.count());
        return counts;
    }

    /* ---------- Admin writes ---------- */

    @Transactional
    public ContactUsResponse review(Long id, ReviewContactRequest request) {
        ContactUs entity = getOrThrow(id);

        if (request.status() == ContactUs.Status.pending) {
            throw new BadRequestException("Cannot revert a message to 'pending'");
        }

        User reviewer = currentUser();
        if (reviewer == null) {
            throw new BadRequestException("No authenticated user found for review");
        }

        entity.setStatus(request.status());
        entity.setReviewedBy(reviewer);
        entity.setReviewedAt(LocalDateTime.now());

        return toResponse(contactUsRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        ContactUs entity = getOrThrow(id);
        contactUsRepository.delete(entity);
    }

    /* ---------- Helpers ---------- */

    private ContactUs getOrThrow(Long id) {
        return contactUsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact message not found: " + id));
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    private ContactUsResponse toResponse(ContactUs c) {
        return new ContactUsResponse(
                c.getId(),
                c.getName(),
                c.getEmail(),
                c.getPhone(),
                c.getMessage(),
                c.getStatus(),
                c.getReviewedBy() != null ? c.getReviewedBy().getId()   : null,
                c.getReviewedBy() != null ? c.getReviewedBy().getName() : null,
                c.getReviewedAt(),
                c.getCreatedAt()
        );
    }
}