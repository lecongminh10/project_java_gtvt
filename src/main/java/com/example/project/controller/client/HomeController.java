package com.example.project.controller.client;

import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "Home");
        return "client/home";
    }

    @GetMapping("/product")
    public String product(Model model) {
        model.addAttribute("pageTitle", "Products");
        return "client/product";
    }

    @GetMapping("/cart")
    public String cart(Model model) {
        model.addAttribute("pageTitle", "Shopping Cart");
        return "client/cart";
    }

    @GetMapping("/login")
    public String login(
            Model model,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "expired", required = false) String expired) {
        model.addAttribute("pageTitle", "Login");

        if (error != null) {
            model.addAttribute("loginError", "Invalid username or password");
        }
        if (expired != null) {
            model.addAttribute("sessionExpired", "Your session has expired. Please login again.");
        }

        return "client/login";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, null);
        return "redirect:/?logout=success";
    }
}
