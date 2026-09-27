package com.fae.adminportal.domain.contact.dto;

import com.fae.adminportal.domain.contact.entity.ContactUs;
import jakarta.validation.constraints.NotNull;

public record ReviewContactRequest(
    @NotNull(message = "status is required") ContactUs.Status status
) {}