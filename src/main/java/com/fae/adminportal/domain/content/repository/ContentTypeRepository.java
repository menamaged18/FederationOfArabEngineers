package com.fae.adminportal.domain.content.repository;

import com.fae.adminportal.domain.content.entity.ContentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContentTypeRepository extends JpaRepository<ContentType, Integer> {

    Optional<ContentType> findBySlug(String slug);

    Optional<ContentType> findByName(String name);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);
}