package com.wagnerdf.backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.wagnerdf.backend.dto.LoginRequest;
import com.wagnerdf.backend.dto.SignupRequest;
import com.wagnerdf.backend.model.UserAccount;
import com.wagnerdf.backend.model.UserRole;
import com.wagnerdf.backend.repository.UserAccountRepository;
import com.wagnerdf.backend.repository.UserRoleRepository;
import com.wagnerdf.backend.util.JwtUtil;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ================================
    // SIGNUP
    // ================================
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {

        if (userAccountRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email já cadastrado.");
        }

        UserRole role = userRoleRepository.findByName(request.getRole())
                .orElseThrow(() -> new RuntimeException("Role não encontrada."));

        UserAccount user = UserAccount.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .active(true)
                .build();

        userAccountRepository.save(user);

        return ResponseEntity.ok("Usuário criado com sucesso!");
    }

    // ================================
    // LOGIN
    // ================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        try {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            UserAccount user = (UserAccount) authentication.getPrincipal();

            String token = jwtUtil.generateToken(user);

            return ResponseEntity.ok(Map.of("token", token));

        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body("Credenciais inválidas.");
        }
    }
}