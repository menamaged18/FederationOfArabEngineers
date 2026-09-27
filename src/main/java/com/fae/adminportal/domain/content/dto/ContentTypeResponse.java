package com.fae.adminportal.domain.content.dto;

public record ContentTypeResponse(
    Integer id,
    String name,
    String slug
) {}