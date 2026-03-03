package com.wagnerdf.backend.service;

import com.wagnerdf.backend.model.UserAccount;
import com.wagnerdf.backend.model.UserRole;
import com.wagnerdf.backend.repository.UserAccountRepository;
import com.wagnerdf.backend.repository.UserRoleRepository;
import com.wagnerdf.backend.dto.SignupRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.wagnerdf.backend.exception.BusinessException;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    
	private final UserRoleRepository userRoleRepository;
	private final UserAccountRepository userAccountRepository;
	private final PasswordEncoder passwordEncoder;

    public UserAccount getLoggedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // pega o username/email do token
        return userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário logado não encontrado", HttpStatus.NOT_FOUND));
    }
    
    public void registerUser(SignupRequest request) {

        String email = request.getEmail().toLowerCase();

        if (userAccountRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email já cadastrado.");
        }

        UserRole role = userRoleRepository.findByName(request.getRole())
                .orElseThrow(() -> new RuntimeException("Role não encontrada."));

        UserAccount user = UserAccount.builder()
                .fullName(request.getFullName())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .active(true)
                .build();

        userAccountRepository.save(user);
    }
}
