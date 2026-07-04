package com.kushagragupta.bankledger.service;

import com.kushagragupta.bankledger.dto.AuthResponse;
import com.kushagragupta.bankledger.dto.LoginRequest;
import com.kushagragupta.bankledger.dto.RegisterRequest;
import com.kushagragupta.bankledger.entity.Customer;
import com.kushagragupta.bankledger.entity.Role;
import com.kushagragupta.bankledger.entity.RoleName;
import com.kushagragupta.bankledger.exception.DuplicateResourceException;
import com.kushagragupta.bankledger.repository.CustomerRepository;
import com.kushagragupta.bankledger.repository.RoleRepository;
import com.kushagragupta.bankledger.security.CustomUserDetails;
import com.kushagragupta.bankledger.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public void register(RegisterRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already taken");
        }
        if (customerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("Phone number is already taken");
        }

        Role userRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("CUSTOMER role not found"));

        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Collections.singleton(userRole))
                .build();

        customerRepository.save(customer);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow();
        
        CustomUserDetails userDetails = new CustomUserDetails(customer);
        var jwtToken = jwtService.generateToken(userDetails);
        
        return AuthResponse.builder()
                .accessToken(jwtToken)
                .tokenType("Bearer")
                .build();
    }
}
