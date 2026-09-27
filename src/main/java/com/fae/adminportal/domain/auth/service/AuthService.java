package com.fae.adminportal.domain.auth.service;

import com.fae.adminportal.common.exception.BadRequestException;
import com.fae.adminportal.domain.auth.dto.JwtAuthResponse;
import com.fae.adminportal.domain.auth.dto.LoginRequest;
import com.fae.adminportal.domain.auth.dto.SignupRequest;
import com.fae.adminportal.domain.user.dto.RoleSummary;
import com.fae.adminportal.domain.user.entity.Role;
import com.fae.adminportal.domain.user.entity.RoleSlug;
import com.fae.adminportal.domain.user.entity.User;
import com.fae.adminportal.domain.user.repository.RoleRepository;
import com.fae.adminportal.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public JwtAuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (DisabledException ex) {
            throw new BadRequestException("Account is deactivated. Contact an administrator.");
        } catch (AuthenticationException ex) {
            throw new BadRequestException("Invalid email or password");
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        String token = jwtService.generateToken(user);
        return new JwtAuthResponse(
                token, "Bearer", jwtService.getExpirationSeconds(), toUserSummary(user));
    }

    @Transactional
    public JwtAuthResponse signup(SignupRequest req) {
        String email = req.email().trim().toLowerCase();
        
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("Email already in use: " + email);
        }

        Role memberRole = roleRepository.findBySlug(RoleSlug.MEMBER)
                .orElseThrow(() -> new BadRequestException(
                        "Default role '" + RoleSlug.MEMBER + "' is missing — check your seed data"));

        User user = User.builder()
                .name(req.name().trim())
                .email(email)
                .phone(req.phone() == null || req.phone().isBlank() ? null : req.phone().trim())
                .password(passwordEncoder.encode(req.password()))
                .role(memberRole)
                .active(true)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new JwtAuthResponse(
                token, "Bearer", jwtService.getExpirationSeconds(), toUserSummary(user));
    }

    private JwtAuthResponse.UserSummary toUserSummary(User user) {
        Role r = user.getRole();
        return new JwtAuthResponse.UserSummary(
                user.getId(),
                user.getName(),
                user.getEmail(),
                r == null ? null : new RoleSummary(r.getId(), r.getName(), r.getSlug())
        );
    }
}