package com.fae.adminportal.domain.contact.repository;

import com.fae.adminportal.domain.contact.entity.ContactUs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactUsRepository extends JpaRepository<ContactUs, Long> {

    Page<ContactUs> findByStatus(ContactUs.Status status, Pageable pageable);

    long countByStatus(ContactUs.Status status);

    @Query("""
        SELECT c FROM ContactUs c
        WHERE (:status IS NULL OR c.status = :status)
          AND (:search IS NULL
               OR LOWER(c.name)    LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(c.email)   LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(c.message) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    Page<ContactUs> search(@Param("status") ContactUs.Status status,
                           @Param("search") String search,
                           Pageable pageable);
}