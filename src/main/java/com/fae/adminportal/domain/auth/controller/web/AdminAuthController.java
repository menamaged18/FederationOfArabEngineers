package com.fae.adminportal.domain.auth.controller.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Admin form-login page controller.
 * <p>
 * Only renders the login view (GET). The actual credential submission is handled
 * by Spring Security's {@code UsernamePasswordAuthenticationFilter} at
 * {@code POST /admin/login} — the {@code SecurityFilterChain} intercepts the
 * request before it ever reaches this controller.
 * <p>
 * Supports the two standard redirect markers:
 * <ul>
 *   <li>{@code ?error}  — authentication failed</li>
 *   <li>{@code ?logout} — user explicitly logged out</li>
 * </ul>
 * plus {@code ?deactivated} which we use in a custom
 * {@code AuthenticationFailureHandler} when the account is disabled.
 */
@Controller
@RequestMapping("/admin")
public class AdminAuthController {

    private static final String VIEW_LOGIN = "admin/auth/login";

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error",     required = false) String error,
            @RequestParam(value = "logout",    required = false) String logout,
            @RequestParam(value = "deactivated", required = false) String deactivated,
            Model model) {

        // Already authenticated? Bounce straight to the dashboard.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/admin/dashboard";
        }

        if (error != null)       model.addAttribute("errorMessage",       "Invalid email or password.");
        if (deactivated != null) model.addAttribute("errorMessage",
                "Account is deactivated. Contact an administrator.");
        if (logout != null)      model.addAttribute("successMessage",     "You have been signed out.");

        return VIEW_LOGIN;
    }

    // Note: no POST handler here on purpose.
    // POST /admin/login  → handled by Spring Security (UsernamePasswordAuthenticationFilter).
    // POST /admin/logout → handled by Spring Security (LogoutFilter).
}