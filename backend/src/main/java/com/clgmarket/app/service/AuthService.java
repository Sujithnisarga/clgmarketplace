package com.clgmarket.app.service;

import com.clgmarket.app.dto.*;
import com.clgmarket.app.entity.User;
import com.clgmarket.app.repository.UserRepository;
import com.clgmarket.app.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthDto.AuthResponse register(AuthDto.RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email already registered");

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .college(req.getCollege())
                .role(User.Role.USER)
                .build();
        userRepository.save(user);

        AuthDto.AuthResponse response = new AuthDto.AuthResponse();
        response.setToken(jwtUtil.generateToken(user));
        response.setUser(userService.toDto(user));
        return response;
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword()))
            throw new RuntimeException("Invalid credentials");

        AuthDto.AuthResponse response = new AuthDto.AuthResponse();
        response.setToken(jwtUtil.generateToken(user));
        response.setUser(userService.toDto(user));
        return response;
    }
}
