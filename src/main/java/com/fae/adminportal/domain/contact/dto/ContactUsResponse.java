package com.fae.adminportal.domain.contact.dto;

import com.fae.adminportal.domain.contact.entity.ContactUs;

import java.time.LocalDateTime;

public record ContactUsResponse(
    Long id,
    String name,
    String email,
    String phone,
    String message,
    ContactUs.Status status,
    Long reviewedById,
    String reviewedByName,
    LocalDateTime reviewedAt,
    LocalDateTime createdAt
) {}