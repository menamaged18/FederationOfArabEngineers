package com.fae.adminportal.domain.user.controller.web;

import com.fae.adminportal.common.dto.PagedResponse;
import com.fae.adminportal.domain.user.dto.CreateRoleRequest;
import com.fae.adminportal.domain.user.dto.RoleResponse;
import com.fae.adminportal.domain.user.dto.UpdateRoleRequest;
import com.fae.adminportal.domain.user.service.RoleService;
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
 * Admin MVC endpoints for role management (Thymeleaf views).
 * Base path: /admin/roles
 * <p>
 * Authenticated via form login (see {@code SecurityConfig} admin chain).
 * DTOs are Java records → constructor binding (Spring 6.1+ / Boot 3.2+).
 */
@Controller
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_super_admin')")
public class AdminRoleController {

    private static final String VIEW_LIST = "admin/roles/list";
    private static final String VIEW_FORM = "admin/roles/form";

    private final RoleService roleService;

    /* ------------------------------------------------------------------ */
    /*  LIST                                                               */
    /* ------------------------------------------------------------------ */

    @GetMapping
    public String list(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            Model model) {

        PagedResponse<RoleResponse> roles = roleService.list(
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"))
        );

        model.addAttribute("roles", roles);
        model.addAttribute("activeNav", "roles");
        return VIEW_LIST;
    }

    /* ------------------------------------------------------------------ */
    /*  CREATE                                                             */
    /* ------------------------------------------------------------------ */

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        // Records have no no-arg ctor → build an "empty" instance explicitly.
        CreateRoleRequest blank = new CreateRoleRequest(
                null,   // name
                null,   // slug
                null    // description
        );

        model.addAttribute("role", blank);
        model.addAttribute("mode", "create");
        model.addAttribute("activeNav", "roles");
        return VIEW_FORM;
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("role") CreateRoleRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttrs) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("activeNav", "roles");
            return VIEW_FORM;
        }

        try {
            roleService.create(request);
        } catch (RuntimeException ex) {
            // e.g. duplicate slug/name — surface as a form-level error
            bindingResult.reject("createFailed", ex.getMessage());
            model.addAttribute("mode", "create");
            model.addAttribute("activeNav", "roles");
            return VIEW_FORM;
        }

        redirectAttrs.addFlashAttribute("successMessage", "Role created successfully.");
        return "redirect:/admin/roles";
    }

    /* ------------------------------------------------------------------ */
    /*  EDIT / UPDATE                                                      */
    /* ------------------------------------------------------------------ */

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Integer id, Model model) {
        RoleResponse existing = roleService.getById(id);

        // Records: accessors without "get" prefix; slug is immutable → not carried in the form.
        UpdateRoleRequest form = new UpdateRoleRequest(
                existing.name(),
                existing.description()
        );

        model.addAttribute("role", form);
        model.addAttribute("roleId", id);
        model.addAttribute("roleSlug", existing.slug());
        model.addAttribute("mode", "edit");
        model.addAttribute("activeNav", "roles");
        return VIEW_FORM;
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Integer id,
            @Valid @ModelAttribute("role") UpdateRoleRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttrs) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("roleId", id);
            model.addAttribute("mode", "edit");
            model.addAttribute("activeNav", "roles");
            return VIEW_FORM;
        }

        try {
            roleService.update(id, request);
        } catch (RuntimeException ex) {
            bindingResult.reject("updateFailed", ex.getMessage());
            model.addAttribute("roleId", id);
            model.addAttribute("mode", "edit");
            model.addAttribute("activeNav", "roles");
            return VIEW_FORM;
        }

        redirectAttrs.addFlashAttribute("successMessage", "Role updated successfully.");
        return "redirect:/admin/roles";
    }

    /* ------------------------------------------------------------------ */
    /*  DELETE                                                             */
    /* ------------------------------------------------------------------ */

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttrs) {
        try {
            roleService.delete(id);
            redirectAttrs.addFlashAttribute("successMessage", "Role deleted.");
        } catch (RuntimeException ex) {
            // e.g. RoleService throws BadRequestException when users are still assigned
            redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/roles";
    }
}