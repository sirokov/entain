package com.entain.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller to serve the frontend dashboard page.
 *
 * <p>
 * This controller maps the "/dashboard" URL to the static resource "dashboard.html",
 * allowing users to access the dashboard via a clean and user-friendly URL.
 * </p>
 */
@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "forward:/dashboard.html";
    }
}