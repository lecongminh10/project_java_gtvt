package com.example.project.controller.client;

import com.example.project.entity.User;
import com.example.project.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

@Controller
public class HomeController {

    private static final String DEFAULT_TEACHER_PASSWORD = "12345678";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public HomeController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String home(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if ("ROLE_ADMIN".equals(role)) {
                return "redirect:/admin/dashboard";
            }
            if ("ROLE_TEACHER".equals(role)) {
                return "redirect:" + resolveTeacherRedirect(authentication.getName());
            }
        }

        return "redirect:/login";
    }

    private String resolveTeacherRedirect(String username) {
        if (username == null || username.isBlank()) {
            return "/teacher/account";
        }
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return "/teacher/account";
        }
        User user = userOpt.get();
        if (passwordEncoder.matches(DEFAULT_TEACHER_PASSWORD, user.getPassword())) {
            return "/reset-password";
        }
        return "/teacher/account";
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();
                if ("ROLE_ADMIN".equals(role)) {
                    return "redirect:/admin/dashboard";
                }
                if ("ROLE_TEACHER".equals(role)) {
                    return "redirect:" + resolveTeacherRedirect(authentication.getName());
                }
            }
        }

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
