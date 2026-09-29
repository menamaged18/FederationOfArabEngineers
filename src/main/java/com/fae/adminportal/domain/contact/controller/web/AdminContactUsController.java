package com.fae.adminportal.domain.contact.controller.web;

import com.fae.adminportal.common.dto.PagedResponse;
import com.fae.adminportal.domain.contact.dto.ContactUsResponse;
import com.fae.adminportal.domain.contact.dto.ReviewContactRequest;
import com.fae.adminportal.domain.contact.entity.ContactUs;
import com.fae.adminportal.domain.contact.service.ContactUsService;
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
 * Admin MVC endpoints for reviewing Contact Us inquiries (Thymeleaf views).
 * Base path: /admin/contact-us
 */
@Controller
@RequestMapping("/admin/contact-us")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_super_admin')")
public class AdminContactUsController {

    private static final String VIEW_LIST   = "admin/contact-us/list";
    private static final String VIEW_DETAIL = "admin/contact-us/detail";

    private final ContactUsService contactUsService;

    /* ------------------------------------------------------------------ */
    /*  LIST                                                               */
    /* ------------------------------------------------------------------ */

    @GetMapping
    public String list(
            @RequestParam(required = false) ContactUs.Status status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        PagedResponse<ContactUsResponse> messages = contactUsService.search(
                status, search,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        model.addAttribute("messages", messages);
        model.addAttribute("stats", contactUsService.countByStatus());
        model.addAttribute("statuses", ContactUs.Status.values());
        model.addAttribute("status", status);
        model.addAttribute("search", search);
        model.addAttribute("activeNav", "contact");
        return VIEW_LIST;
    }

    /* ------------------------------------------------------------------ */
    /*  DETAIL                                                             */
    /* ------------------------------------------------------------------ */

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        ContactUsResponse message = contactUsService.findById(id);

        // Records have no no-arg ctor; pre-fill the review form with the current status.
        ReviewContactRequest reviewForm = new ReviewContactRequest(message.status());

        model.addAttribute("message", message);
        model.addAttribute("reviewForm", reviewForm);
        model.addAttribute("statuses", ContactUs.Status.values());
        model.addAttribute("activeNav", "contact");
        return VIEW_DETAIL;
    }

    /* ------------------------------------------------------------------ */
    /*  REVIEW (change status)                                             */
    /* ------------------------------------------------------------------ */

    @PostMapping("/{id}/review")
    public String review(
            @PathVariable Long id,
            @Valid @ModelAttribute("reviewForm") ReviewContactRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttrs) {

        if (bindingResult.hasErrors()) {
            redirectAttrs.addFlashAttribute("errorMessage", "Invalid status value.");
            return "redirect:/admin/contact-us/" + id;
        }

        try {
            contactUsService.review(id, request);
            redirectAttrs.addFlashAttribute("successMessage", "Message marked as " + request.status() + ".");
        } catch (RuntimeException ex) {
            redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/contact-us/" + id;
    }

    /* ------------------------------------------------------------------ */
    /*  DELETE                                                             */
    /* ------------------------------------------------------------------ */

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            contactUsService.delete(id);
            redirectAttrs.addFlashAttribute("successMessage", "Message deleted.");
            return "redirect:/admin/contact-us";
        } catch (RuntimeException ex) {
            redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/contact-us/" + id;
        }
    }
}