package com.fae.adminportal.domain.file.controller.web;

import com.fae.adminportal.common.dto.PagedResponse;
import com.fae.adminportal.domain.file.dto.FileResponse;
import com.fae.adminportal.domain.file.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

/**
 * Admin MVC endpoints for file asset management (Thymeleaf views).
 * Base path: /admin/files
 */
@Controller
@RequestMapping("/admin/files")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_super_admin')")
public class AdminFileController {

    private static final String VIEW_LIST = "admin/files/list";

    private final FileStorageService fileStorageService;

    /* ------------------------------------------------------------------ */
    /*  LIST                                                               */
    /* ------------------------------------------------------------------ */

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        PagedResponse<FileResponse> files = fileStorageService.list(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        model.addAttribute("files", files);
        model.addAttribute("activeNav", "files");
        return VIEW_LIST;
    }

    /* ------------------------------------------------------------------ */
    /*  UPLOAD                                                             */
    /* ------------------------------------------------------------------ */

    @PostMapping(value = "/upload")
    public String upload(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttrs) {

        if (file == null || file.isEmpty()) {
            redirectAttrs.addFlashAttribute("errorMessage", "Please select a file to upload.");
            return "redirect:/admin/files";
        }

        try {
            FileResponse created = fileStorageService.store(file);
            redirectAttrs.addFlashAttribute("successMessage",
                    "Uploaded: " + safeName(created));
        } catch (RuntimeException ex) {
            redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/files";
    }

    @PostMapping(value = "/upload-multiple")
    public String uploadMultiple(
            @RequestParam("files") MultipartFile[] files,
            RedirectAttributes redirectAttrs) {

        if (files == null || files.length == 0) {
            redirectAttrs.addFlashAttribute("errorMessage", "Please select at least one file.");
            return "redirect:/admin/files";
        }

        try {
            long ok = Arrays.stream(files)
                    .filter(f -> !f.isEmpty())
                    .peek(fileStorageService::store)
                    .count();
            redirectAttrs.addFlashAttribute("successMessage",
                    ok + " file" + (ok == 1 ? "" : "s") + " uploaded.");
        } catch (RuntimeException ex) {
            redirectAttrs.addFlashAttribute("errorMessage",
                    "Upload partially failed: " + ex.getMessage());
        }
        return "redirect:/admin/files";
    }

    /* ------------------------------------------------------------------ */
    /*  DELETE                                                             */
    /* ------------------------------------------------------------------ */

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            fileStorageService.delete(id);
            redirectAttrs.addFlashAttribute("successMessage", "File deleted.");
        } catch (RuntimeException ex) {
            redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/files";
    }

    /* ------------------------------------------------------------------ */
    /*  Helpers                                                            */
    /* ------------------------------------------------------------------ */

    /**
     * Defensive accessor — returns the file's display name, whatever
     * {@code FileResponse} actually calls it. Adjust once FileResponse is finalized.
     */
    private String safeName(FileResponse f) {
        // Adjust to whatever the record accessor is: originalName(), name(), filename()...
        return f.originalName();
    }
}