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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private SecurityContextRepository securityContextRepository;
    @Mock private HttpServletRequest httpRequest;
    @Mock private HttpServletResponse httpResponse;
    @Mock private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("john@example.com")
                .password("secret123")
                .fullName("John Doe")
                .phoneNumber("9876543210")
                .build();

        loginRequest = LoginRequest.builder()
                .username("john@example.com")
                .password("secret123")
                .build();
    }

    @Test
    void register_success() {
        when(userRepository.existsByUsername("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed_secret");

        User savedUser = User.builder()
                .id(1L).username("john@example.com")
                .password("hashed_secret").fullName("John Doe")
                .phoneNumber("9876543210").build();
        when(userRepository.save(any())).thenReturn(savedUser);

        UserResponse response = authService.register(registerRequest);

        assertThat(response.getUsername()).isEqualTo("john@example.com");
        assertThat(response.getFullName()).isEqualTo("John Doe");
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void register_throwsWhenUsernameAlreadyExists() {
        when(userRepository.existsByUsername("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void login_success() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        User user = User.builder().id(1L).username("john@example.com")
                .fullName("John Doe").password("hashed").build();
        when(userRepository.findByUsername("john@example.com")).thenReturn(Optional.of(user));

        doNothing().when(securityContextRepository).saveContext(any(), any(), any());

        UserResponse response = authService.login(loginRequest, httpRequest, httpResponse);

        assertThat(response.getUsername()).isEqualTo("john@example.com");
    }

    @Test
    void login_throwsOnBadCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest, httpRequest, httpResponse))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid username or password");
    }
}
