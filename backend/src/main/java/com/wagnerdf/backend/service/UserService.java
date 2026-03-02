package com.wagnerdf.backend.service;

import com.wagnerdf.backend.model.UserAccount;
import com.wagnerdf.backend.repository.UserAccountRepository;
import com.wagnerdf.backend.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserAccountRepository userAccountRepository;

    public UserAccount getLoggedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // pega o username/email do token
        return userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário logado não encontrado", HttpStatus.NOT_FOUND));
    }
}
