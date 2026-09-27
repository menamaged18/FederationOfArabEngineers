package com.fae.adminportal.domain.file.service;

import com.fae.adminportal.common.dto.PagedResponse;
import com.fae.adminportal.common.exception.BadRequestException;
import com.fae.adminportal.common.exception.ResourceNotFoundException;
import com.fae.adminportal.config.FileStorageConfig;
import com.fae.adminportal.domain.file.dto.FileResponse;
import com.fae.adminportal.domain.file.entity.FileAsset;
import com.fae.adminportal.domain.file.repository.FileRepository;
import com.fae.adminportal.domain.user.entity.User;
import com.fae.adminportal.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final FileStorageConfig config;

    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ofPattern("yyyy/MM");

    /* ---------- Store ---------- */

    @Transactional
    public FileResponse store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        if (file.getSize() > config.getMaxFileSize()) {
            throw new BadRequestException("File exceeds maximum allowed size");
        }

        String rawName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String originalName = StringUtils.cleanPath(rawName);
        if (originalName.contains("..")) {
            throw new BadRequestException("Invalid file name");
        }

        String extension = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0 && dot < originalName.length() - 1) {
            extension = originalName.substring(dot).toLowerCase();
        }

        // Organize into year/month buckets, unique name per file
        String relativePath = LocalDate.now().format(DATE_DIR) + "/" + UUID.randomUUID() + extension;

        try {
            Path uploadRoot = Paths.get(config.getUploadDir()).toAbsolutePath().normalize();
            Path target = uploadRoot.resolve(relativePath).normalize();

            // Guard against path traversal
            if (!target.startsWith(uploadRoot)) {
                throw new BadRequestException("Invalid file path");
            }

            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            FileAsset asset = FileAsset.builder()
                    .originalName(originalName)
                    .filePath(relativePath.replace('\\', '/')) // store with forward slashes
                    .mimeType(file.getContentType())
                    .fileSize((int) file.getSize())
                    .uploadedBy(currentUserOrNull())
                    .build();

            return toResponse(fileRepository.save(asset));

        } catch (IOException ex) {
            log.error("Failed to store file '{}'", originalName, ex);
            throw new BadRequestException("Failed to store file: " + ex.getMessage());
        }
    }

    /* ---------- Read ---------- */

    @Transactional(readOnly = true)
    public FileResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<FileResponse> list(Pageable pageable) {
        return PagedResponse.from(fileRepository.findAll(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<FileResponse> listByUploader(Long userId, Pageable pageable) {
        return PagedResponse.from(
                fileRepository.findByUploadedBy_Id(userId, pageable).map(this::toResponse));
    }

    /** Used by ContentService when attaching files to content. */
    @Transactional(readOnly = true)
    public FileAsset getAsset(Long id) {
        return getOrThrow(id);
    }

    @Transactional(readOnly = true)
    public Resource loadAsResource(Long id) {
        FileAsset asset = getOrThrow(id);
        try {
            Path uploadRoot = Paths.get(config.getUploadDir()).toAbsolutePath().normalize();
            Path filePath = uploadRoot.resolve(asset.getFilePath()).normalize();
            if (!filePath.startsWith(uploadRoot)) {
                throw new BadRequestException("Invalid file path");
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("File not found on disk: " + id);
            }
            return resource;
        } catch (IOException ex) {
            throw new ResourceNotFoundException("File not readable: " + id);
        }
    }

    /* ---------- Delete ---------- */

    @Transactional
    public void delete(Long id) {
        FileAsset asset = getOrThrow(id);

        // Delete from disk (best-effort)
        try {
            Path uploadRoot = Paths.get(config.getUploadDir()).toAbsolutePath().normalize();
            Path filePath = uploadRoot.resolve(asset.getFilePath()).normalize();
            if (filePath.startsWith(uploadRoot)) {
                Files.deleteIfExists(filePath);
            }
        } catch (IOException ex) {
            log.warn("Could not delete file from disk: {}", asset.getFilePath(), ex);
        }

        // content_files rows will be removed by ON DELETE CASCADE
        fileRepository.delete(asset);
    }

    /* ---------- Helpers ---------- */

    private FileAsset getOrThrow(Long id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + id));
    }

    private User currentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    public FileResponse toResponse(FileAsset f) {
        String url = config.getPublicBaseUrl() + "/" + f.getFilePath();
        return new FileResponse(
                f.getId(),
                f.getOriginalName(),
                f.getFilePath(),
                url,
                f.getMimeType(),
                f.getFileSize(),
                f.getUploadedBy() != null ? f.getUploadedBy().getId() : null,
                f.getUploadedBy() != null ? f.getUploadedBy().getName() : null,
                f.getCreatedAt()
        );
    }
}