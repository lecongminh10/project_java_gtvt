package com.example.project.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.example.project.entity.User;
import com.example.project.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final String DEFAULT_TEACHER_PASSWORD = "12345678";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomAuthenticationSuccessHandler(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        // Kiểm tra role của user
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String redirectUrl = "/";

        if (shouldForcePasswordChange(authentication.getName())) {
            response.sendRedirect("/reset-password");
            return;
        }

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();

            if (role.equals("ROLE_ADMIN")) {
                redirectUrl = "/admin/dashboard";
                break;
            } else if (role.equals("ROLE_TEACHER")) {
                redirectUrl = resolveTeacherRedirect(authentication.getName());
                break;
            }
        }

        response.sendRedirect(redirectUrl);
    }

    private boolean shouldForcePasswordChange(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        Optional<User> userOpt = userRepository.findByUsername(username);
        return userOpt.isPresent()
                && passwordEncoder.matches(DEFAULT_TEACHER_PASSWORD, userOpt.get().getPassword());
    }

    private String resolveTeacherRedirect(String username) {
        if (username == null || username.isBlank()) {
            return "/teacher/dashboard";
        }
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return "/teacher/dashboard";
        }
        User user = userOpt.get();
        if (passwordEncoder.matches(DEFAULT_TEACHER_PASSWORD, user.getPassword())) {
            return "/reset-password";
        }
        return "/teacher/dashboard";
    }
}
