package com.fae.adminportal.domain.user.service;

import com.fae.adminportal.domain.user.entity.User;
import com.fae.adminportal.domain.user.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String DEFAULT_AUTHORITY = ROLE_PREFIX + "member";

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isActive(),   // enabled
                true,              // accountNonExpired
                true,              // credentialsNonExpired
                true,              // accountNonLocked
                List.of(new SimpleGrantedAuthority(authorityFor(user)))
        );
    }

    private String authorityFor(User user) {
        if (user.getRole() == null || user.getRole().getSlug() == null) {
            return DEFAULT_AUTHORITY;
        }
        String slug = user.getRole().getSlug();     // already a String
        return slug.startsWith(ROLE_PREFIX) ? slug : ROLE_PREFIX + slug;
    }
}