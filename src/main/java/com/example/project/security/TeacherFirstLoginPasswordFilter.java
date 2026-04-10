package com.example.project.security;

import com.example.project.entity.User;
import com.example.project.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

public class TeacherFirstLoginPasswordFilter extends OncePerRequestFilter {

    private static final String DEFAULT_TEACHER_PASSWORD = "12345678";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TeacherFirstLoginPasswordFilter(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            boolean isTeacher = authentication.getAuthorities().stream()
                    .anyMatch(authority -> "ROLE_TEACHER".equals(authority.getAuthority()));
            if (isTeacher && shouldCheck(request)) {
                String username = authentication.getName();
                Optional<User> userOpt = userRepository.findByUsername(username);
                if (userOpt.isPresent()
                        && passwordEncoder.matches(DEFAULT_TEACHER_PASSWORD, userOpt.get().getPassword())) {
                    response.sendRedirect("/reset-password");
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean shouldCheck(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (!path.startsWith("/teacher")) {
            return false;
        }
        return true;
    }
}
