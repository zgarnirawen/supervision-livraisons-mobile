package com.supervision.livraisons.service;

import com.supervision.livraisons.dto.LoginRequest;
import com.supervision.livraisons.dto.LoginResponse;
import com.supervision.livraisons.model.PersonnelMobile;
import com.supervision.livraisons.repository.PersonnelRepository;
import com.supervision.livraisons.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final PersonnelRepository personnelRepo;
    private final JwtUtils jwtUtils;

    public AuthService(AuthenticationManager authManager,
                       PersonnelRepository personnelRepo,
                       JwtUtils jwtUtils) {
        this.authManager = authManager;
        this.personnelRepo = personnelRepo;
        this.jwtUtils = jwtUtils;
    }

    public LoginResponse login(LoginRequest request) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getLogin(), request.getMotPasse()));
        SecurityContextHolder.getContext().setAuthentication(auth);

        PersonnelMobile personnel = personnelRepo.findByLogin(request.getLogin())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        String token = jwtUtils.generateToken(
                personnel.getIdpers(), personnel.getLogin(), personnel.getCodeposte());

        return new LoginResponse(
                token,
                personnel.getIdpers(),
                personnel.getNomComplet(),
                personnel.getLogin(),
                personnel.getCodeposte());
    }
}
