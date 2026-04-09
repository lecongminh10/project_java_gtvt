package com.example.project.security;

import com.example.project.entity.User;
import com.example.project.entity.UserStatus;
import com.example.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);

        if (!user.isPresent()) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        User foundUser = user.get();
        boolean enabled = foundUser.getStatus() != UserStatus.INACTIVE;
        return new org.springframework.security.core.userdetails.User(
            foundUser.getUsername(),
            foundUser.getPassword(),
            enabled,
            true,
            true,
            true,
            buildAuthorities(foundUser));
    }

    private Collection<? extends GrantedAuthority> buildAuthorities(User user) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        // Add role prefixed with ROLE_ for Spring Security
        if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().toString()));
        }

        return authorities;
    }
}
