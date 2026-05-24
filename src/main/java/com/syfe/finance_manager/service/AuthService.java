package com.syfe.finance_manager.service;

import com.syfe.finance_manager.dto.auth.LoginRequest;
import com.syfe.finance_manager.dto.auth.RegisterRequest;
import com.syfe.finance_manager.dto.auth.UserResponse;
import com.syfe.finance_manager.entity.User;
import com.syfe.finance_manager.exception.InvalidCredentialsException;
import com.syfe.finance_manager.exception.UsernameAlreadyExistsException;
import com.syfe.finance_manager.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    // Constructor injection for dependencies
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       SecurityContextRepository securityContextRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Username (email) '" + request.getUsername() + "' is already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .build();

        User savedUser = userRepository.save(user);

        return mapToUserResponse(savedUser);
    }

    public UserResponse login(LoginRequest request, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        try {
            // Perform programmatic authentication using standard Spring Security UsernamePasswordAuthenticationToken
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
            );

            Authentication authentication = authenticationManager.authenticate(authToken);

            // Set the security context in SecurityContextHolder
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            // Persist the security context into the HTTP Session so it remains active
            securityContextRepository.saveContext(context, servletRequest, servletResponse);

            // Retrieve the authenticated user from the database to return their profile details
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new InvalidCredentialsException("User details not found"));

            return mapToUserResponse(user);

        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
