package com.kushagragupta.bankledger.service;

import com.kushagragupta.bankledger.security.JwtService;

import com.kushagragupta.bankledger.dto.AuthResponse;
import com.kushagragupta.bankledger.dto.LoginRequest;
import com.kushagragupta.bankledger.dto.RegisterRequest;
import com.kushagragupta.bankledger.entity.Customer;
import com.kushagragupta.bankledger.entity.Role;
import com.kushagragupta.bankledger.entity.RoleName;
import com.kushagragupta.bankledger.exception.DuplicateResourceException;
import com.kushagragupta.bankledger.repository.CustomerRepository;
import com.kushagragupta.bankledger.repository.RoleRepository;
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

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private Customer customer;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("John", "Doe", "john@example.com", "password", "1234567890");
        loginRequest = new LoginRequest("1234567890", "password");
        customerRole = new Role(UUID.randomUUID(), RoleName.CUSTOMER);
        customer = Customer.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("encoded_password")
                .phoneNumber("1234567890")
                .roles(Set.of(customerRole))
                .build();
    }

    @Test
    void register_Success() {
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(roleRepository.findByName(RoleName.CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(jwtService.generateToken(any())).thenReturn("jwt_token");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("jwt_token", response.getToken());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        when(customerRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(registerRequest));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void register_DuplicatePhone_ThrowsException() {
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.existsByPhoneNumber(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(registerRequest));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void login_Success() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(customerRepository.findByPhoneNumber(anyString())).thenReturn(Optional.of(customer));
        when(jwtService.generateToken(any())).thenReturn("jwt_token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt_token", response.getToken());
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }
}
