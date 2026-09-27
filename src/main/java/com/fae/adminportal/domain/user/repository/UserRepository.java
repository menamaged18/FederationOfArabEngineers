package com.fae.adminportal.domain.user.repository;

import com.fae.adminportal.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Login lookup — eager-loads the role so authorities can be built without N+1. */
    @EntityGraph(attributePaths = "role")
    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRoleId(Integer roleId);

    List<User> findAllByRoleSlug(String slug);

    Page<User> findAllByActiveTrue(Pageable pageable);

    @Query("""
            select u from User u
            where lower(u.name)  like lower(concat('%', :q, '%'))
            or lower(u.email) like lower(concat('%', :q, '%'))
            """)
    Page<User> search(@Param("q") String q, Pageable pageable);
}