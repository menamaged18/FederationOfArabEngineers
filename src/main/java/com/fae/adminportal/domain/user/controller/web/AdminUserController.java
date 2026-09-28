package com.fae.adminportal.domain.user.controller.web;

import com.fae.adminportal.common.dto.PagedResponse;
import com.fae.adminportal.domain.user.dto.CreateUserRequest;
import com.fae.adminportal.domain.user.dto.UpdateUserRequest;
import com.fae.adminportal.domain.user.dto.UserResponse;
import com.fae.adminportal.domain.user.service.RoleService;
import com.fae.adminportal.domain.user.service.UserService;
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
 * Admin MVC endpoints for user management (Thymeleaf views).
 * Base path: /admin/users
 * <p>
 * Authenticated via form login (see {@code SecurityConfig} admin chain).
 * <p>
 * DTOs are Java records → forms use Spring's constructor binding
 * (Spring Framework 6.1+ / Boot 3.2+). Ensure the build keeps
 * {@code -parameters} (Spring Boot Maven/Gradle plugins do this by default).
 */
@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_super_admin')")
public class AdminUserController {

    private static final String VIEW_LIST = "admin/users/list";
    private static final String VIEW_FORM = "admin/users/form";

    private final UserService userService;
    private final RoleService roleService;

    /* ------------------------------------------------------------------ */
    /*  LIST                                                               */
    /* ------------------------------------------------------------------ */

    @GetMapping
    public String list(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            Model model) {

        PagedResponse<UserResponse> users = userService.list(
                q,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        model.addAttribute("users", users);
        model.addAttribute("q", q);
        model.addAttribute("activeNav", "users");
        return VIEW_LIST;
    }

    /* ------------------------------------------------------------------ */
    /*  CREATE                                                             */
    /* ------------------------------------------------------------------ */

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        // Records have no no-arg ctor → build an "empty" instance explicitly.
        // `active` defaults to true so the checkbox starts checked.
        CreateUserRequest blank = new CreateUserRequest(
                null,   // name
                null,   // email
                null,   // phone
                null,   // password
                null,   // roleId
                true    // active
        );

        model.addAttribute("user", blank);
        model.addAttribute("roles", roleService.listAll());
        model.addAttribute("mode", "create");
        model.addAttribute("activeNav", "users");
        return VIEW_FORM;
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("user") CreateUserRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttrs) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", roleService.listAll());
            model.addAttribute("mode", "create");
            model.addAttribute("activeNav", "users");
            return VIEW_FORM;
        }

        userService.create(request);
        redirectAttrs.addFlashAttribute("successMessage", "User created successfully.");
        return "redirect:/admin/users";
    }

    /* ------------------------------------------------------------------ */
    /*  EDIT / UPDATE                                                      */
    /* ------------------------------------------------------------------ */

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        UserResponse existing = userService.getById(id);

        // Records use accessor methods without the "get" prefix: name(), email(), role()...
        UpdateUserRequest form = new UpdateUserRequest(
                existing.name(),
                existing.email(),
                existing.phone(),
                existing.role() != null ? existing.role().id() : null
        );

        model.addAttribute("user", form);
        model.addAttribute("userId", id);
        model.addAttribute("roles", roleService.listAll());
        model.addAttribute("mode", "edit");
        model.addAttribute("activeNav", "users");
        return VIEW_FORM;
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("user") UpdateUserRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttrs) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("userId", id);
            model.addAttribute("roles", roleService.listAll());
            model.addAttribute("mode", "edit");
            model.addAttribute("activeNav", "users");
            return VIEW_FORM;
        }

        userService.update(id, request);
        redirectAttrs.addFlashAttribute("successMessage", "User updated successfully.");
        return "redirect:/admin/users";
    }

    /* ------------------------------------------------------------------ */
    /*  ACTIVATE / DEACTIVATE                                              */
    /* ------------------------------------------------------------------ */

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        userService.setActive(id, true);
        redirectAttrs.addFlashAttribute("successMessage", "User activated.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        userService.setActive(id, false);
        redirectAttrs.addFlashAttribute("successMessage", "User deactivated.");
        return "redirect:/admin/users";
    }

    /* ------------------------------------------------------------------ */
    /*  DELETE                                                             */
    /* ------------------------------------------------------------------ */

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        userService.delete(id);
        redirectAttrs.addFlashAttribute("successMessage", "User deleted.");
        return "redirect:/admin/users";
    }
}