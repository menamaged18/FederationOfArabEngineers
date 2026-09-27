package com.fae.adminportal.domain.content.repository;

import com.fae.adminportal.domain.content.entity.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

    Optional<Content> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<Content> findByContentType_Id(Integer contentTypeId, Pageable pageable);

    Page<Content> findByIsPublished(Boolean isPublished, Pageable pageable);

    @Query("""
        SELECT c FROM Content c
        WHERE (:typeId IS NULL OR c.contentType.id = :typeId)
          AND (:typeSlug IS NULL OR c.contentType.slug = :typeSlug)
          AND (:isPublished IS NULL OR c.isPublished = :isPublished)
          AND (:search IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    Page<Content> search(
        @Param("typeId") Integer typeId,
        @Param("typeSlug") String typeSlug,
        @Param("isPublished") Boolean isPublished,
        @Param("search") String search,
        Pageable pageable
    );
}