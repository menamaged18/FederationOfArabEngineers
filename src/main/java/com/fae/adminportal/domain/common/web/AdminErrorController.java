package com.fae.adminportal.domain.common.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Renders the custom admin error views.
 * Registered URLs are referenced by SecurityConfig (403) and by
 * Spring Boot error routing for 404/500 within the admin chain.
 */
@Controller
public class AdminErrorController {

    @GetMapping("/admin/error/403")
    public String forbidden()  { return "admin/error/403"; }

    @GetMapping("/admin/error/404")
    public String notFound()   { return "admin/error/404"; }

    @GetMapping("/admin/error/500")
    public String serverError(){ return "admin/error/500"; }
}