package com.dropout.prediction.service;

import com.dropout.prediction.dto.AuthRequest;
import com.dropout.prediction.dto.AuthResponse;
import com.dropout.prediction.model.User;
import com.dropout.prediction.repository.UserRepository;
import com.dropout.prediction.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private AuthenticationManager authenticationManager;

    public AuthResponse signup(AuthRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(token, user.getUsername(), "Signup successful");
    }

    public AuthResponse login(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid username or password");
        }
        String token = jwtUtil.generateToken(request.getUsername());
        return new AuthResponse(token, request.getUsername(), "Login successful");
    }
}
