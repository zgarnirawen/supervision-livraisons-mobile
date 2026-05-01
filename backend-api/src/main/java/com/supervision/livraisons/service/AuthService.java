package com.supervision.livraisons.service;

import com.supervision.livraisons.dto.LoginRequest;
import com.supervision.livraisons.dto.LoginResponse;
import com.supervision.livraisons.model.PersonnelMobile;
import com.supervision.livraisons.repository.PersonnelRepository;
import com.supervision.livraisons.security.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final PersonnelRepository personnelRepo;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    public AuthService(PersonnelRepository personnelRepo,
                       JwtUtils jwtUtils,
                       PasswordEncoder passwordEncoder) {
        this.personnelRepo = personnelRepo;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {

        PersonnelMobile personnel = personnelRepo.findByLogin(request.getLogin())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (!passwordEncoder.matches(request.getMotPasse(), personnel.getMotPasse())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        String token = jwtUtils.generateToken(
                personnel.getIdpers(),
                personnel.getLogin(),
                personnel.getCodeposte()
        );

        return new LoginResponse(
                token,
                personnel.getIdpers(),
                personnel.getNomComplet(),
                personnel.getLogin(),
                personnel.getCodeposte()
        );
    }

    public void updateFcmToken(Integer idpers, String fcmToken) {
        PersonnelMobile personnel = personnelRepo.findById(idpers)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        personnel.setFcmToken(fcmToken);
        personnelRepo.save(personnel);
    }
}