package com.example.project.security;

import com.example.project.entity.User;
import com.example.project.entity.UserStatus;
import com.example.project.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

public class InactiveUserLogoutFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public InactiveUserLogoutFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            if (username != null) {
                Optional<User> userOpt = userRepository.findByUsername(username);
                if (userOpt.isPresent() && userOpt.get().getStatus() == UserStatus.INACTIVE) {
                    new SecurityContextLogoutHandler().logout(request, response, authentication);
                    response.sendRedirect(request.getContextPath() + "/login?inactive=true");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
