package com.fae.adminportal.domain.content.controller.web;

import com.fae.adminportal.domain.content.dto.ContentTypeRequest;
import com.fae.adminportal.domain.content.dto.ContentTypeResponse;
import com.fae.adminportal.domain.content.service.ContentTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Admin MVC endpoints for content-type management (Thymeleaf views).
 * Base path: /admin/content-types
 */
@Controller
@RequestMapping("/admin/content-types")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_super_admin')")
public class AdminContentTypeController {

    private static final String VIEW_LIST = "admin/content-types/list";
    private static final String VIEW_FORM = "admin/content-types/form";

    private final ContentTypeService contentTypeService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("contentTypes", contentTypeService.findAll());
        model.addAttribute("activeNav", "content-types");
        return VIEW_LIST;
    }

    /* ------------------------------------------------------------------ */
    /*  CREATE                                                             */
    /* ------------------------------------------------------------------ */

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        ContentTypeRequest blank = new ContentTypeRequest(null, null);
        model.addAttribute("contentType", blank);
        model.addAttribute("mode", "create");
        model.addAttribute("activeNav", "content-types");
        return VIEW_FORM;
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("contentType") ContentTypeRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttrs) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("activeNav", "content-types");
            return VIEW_FORM;
        }

        try {
            contentTypeService.create(request);
            redirectAttrs.addFlashAttribute("successMessage", "Content type created.");
        } catch (RuntimeException ex) {
            redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/content-types";
    }

    /* ------------------------------------------------------------------ */
    /*  EDIT / UPDATE                                                      */
    /* ------------------------------------------------------------------ */

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Integer id, Model model) {
        ContentTypeResponse existing = contentTypeService.findById(id);
        ContentTypeRequest form = new ContentTypeRequest(existing.name(), existing.slug());

        model.addAttribute("contentType", form);
        model.addAttribute("contentTypeId", id);
        model.addAttribute("mode", "edit");
        model.addAttribute("activeNav", "content-types");
        return VIEW_FORM;
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Integer id,
            @Valid @ModelAttribute("contentType") ContentTypeRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttrs) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("contentTypeId", id);
            model.addAttribute("mode", "edit");
            model.addAttribute("activeNav", "content-types");
            return VIEW_FORM;
        }

        try {
            contentTypeService.update(id, request);
            redirectAttrs.addFlashAttribute("successMessage", "Content type updated.");
        } catch (RuntimeException ex) {
            redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/content-types";
    }

    /* ------------------------------------------------------------------ */
    /*  DELETE                                                             */
    /* ------------------------------------------------------------------ */

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttrs) {
        try {
            contentTypeService.delete(id);
            redirectAttrs.addFlashAttribute("successMessage", "Content type deleted.");
        } catch (RuntimeException ex) {
            redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/content-types";
    }
}