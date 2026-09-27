package com.fae.adminportal.domain.file.repository;

import com.fae.adminportal.domain.file.entity.FileAsset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileAsset, Long> {
    Optional<FileAsset> findByFilePath(String filePath);
    Page<FileAsset> findByUploadedBy_Id(Long userId, Pageable pageable);
}