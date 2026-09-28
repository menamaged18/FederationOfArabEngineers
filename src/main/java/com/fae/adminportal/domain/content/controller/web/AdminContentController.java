package com.fae.adminportal.domain.content.controller.web;

import com.fae.adminportal.common.dto.PagedResponse;
import com.fae.adminportal.domain.content.dto.ContentCreateRequest;
import com.fae.adminportal.domain.content.dto.ContentResponse;
import com.fae.adminportal.domain.content.dto.ContentUpdateRequest;
import com.fae.adminportal.domain.content.service.ContentService;
import com.fae.adminportal.domain.content.service.ContentTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Admin MVC endpoints for content management (Thymeleaf views).
 * Base path: /admin/contents
 */
@Controller
@RequestMapping("/admin/contents")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_super_admin')")
public class AdminContentController {

    private static final String VIEW_LIST = "admin/content/list";
    private static final String VIEW_FORM = "admin/content/form";

    private final ContentService contentService;
    private final ContentTypeService contentTypeService;

    /* ------------------------------------------------------------------ */
    /*  LIST                                                               */
    /* ------------------------------------------------------------------ */

    @GetMapping
    public String list(
            @RequestParam(required = false) Integer contentTypeId,
            @RequestParam(required = false) String typeSlug,
            @RequestParam(required = false) Boolean isPublished,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        PagedResponse<ContentResponse> contents = contentService.search(
                contentTypeId, typeSlug, isPublished, search,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        model.addAttribute("contents", contents);
        model.addAttribute("contentTypes", contentTypeService.findAll());
        model.addAttribute("contentTypeId", contentTypeId);
        model.addAttribute("typeSlug", typeSlug);
        model.addAttribute("isPublished", isPublished);
        model.addAttribute("search", search);
        model.addAttribute("activeNav", "content");
        return VIEW_LIST;
    }

    /* ------------------------------------------------------------------ */
    /*  CREATE                                                             */
    /* ------------------------------------------------------------------ */

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        // Build an "empty" record instance (records have no no-arg ctor).
        ContentCreateRequest blank = new ContentCreateRequest(
                null,   // contentTypeId
                null,   // title
                null,   // slug
                null,   // description
                null,   // imagePath
                null,   // eventDate
                true,   // isPublished (default)
                null    // fileIds
        );

        model.addAttribute("content", blank);
        model.addAttribute("contentTypes", contentTypeService.findAll());
        model.addAttribute("mode", "create");
        model.addAttribute("activeNav", "content");
        return VIEW_FORM;
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("content") ContentCreateRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttrs) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("contentTypes", contentTypeService.findAll());
            model.addAttribute("mode", "create");
            model.addAttribute("activeNav", "content");
            return VIEW_FORM;
        }

        try {
            contentService.create(request);
            redirectAttrs.addFlashAttribute("successMessage", "Content created successfully.");
        } catch (RuntimeException ex) {
            redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/contents";
    }

    /* ------------------------------------------------------------------ */
    /*  EDIT / UPDATE                                                      */
    /* ------------------------------------------------------------------ */

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        ContentResponse existing = contentService.findById(id);

        ContentUpdateRequest form = new ContentUpdateRequest(
                existing.contentTypeId(),   // ← see Fix 2 below
                existing.title(),
                existing.slug(),
                existing.description(),
                existing.imagePath(),
                existing.eventDate(),
                existing.isPublished(),
                null                        // fileIds — see note below
        );

        model.addAttribute("content", form);
        model.addAttribute("contentId", id);
        model.addAttribute("contentTypes", contentTypeService.findAll());
        model.addAttribute("mode", "edit");
        model.addAttribute("activeNav", "content");
        return VIEW_FORM;
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("content") ContentUpdateRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttrs) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("contentId", id);
            model.addAttribute("contentTypes", contentTypeService.findAll());
            model.addAttribute("mode", "edit");
            model.addAttribute("activeNav", "content");
            return VIEW_FORM;
        }

        try {
            contentService.update(id, request);
            redirectAttrs.addFlashAttribute("successMessage", "Content updated successfully.");
        } catch (RuntimeException ex) {
            redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/contents";
    }

    /* ------------------------------------------------------------------ */
    /*  PUBLISH TOGGLE                                                     */
    /* ------------------------------------------------------------------ */

    @PostMapping("/{id}/publish")
    public String publish(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        contentService.setPublished(id, true);
        redirectAttrs.addFlashAttribute("successMessage", "Content published.");
        return "redirect:/admin/contents";
    }

    @PostMapping("/{id}/unpublish")
    public String unpublish(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        contentService.setPublished(id, false);
        redirectAttrs.addFlashAttribute("successMessage", "Content unpublished.");
        return "redirect:/admin/contents";
    }

    /* ------------------------------------------------------------------ */
    /*  DELETE                                                             */
    /* ------------------------------------------------------------------ */

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            contentService.delete(id);
            redirectAttrs.addFlashAttribute("successMessage", "Content deleted.");
        } catch (RuntimeException ex) {
            redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/contents";
    }
}