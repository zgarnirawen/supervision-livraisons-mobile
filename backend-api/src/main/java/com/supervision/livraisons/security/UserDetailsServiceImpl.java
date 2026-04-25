package com.supervision.livraisons.security;

import com.supervision.livraisons.model.PersonnelMobile;
import com.supervision.livraisons.repository.PersonnelRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final PersonnelRepository personnelRepository;

    public UserDetailsServiceImpl(PersonnelRepository personnelRepository) {
        this.personnelRepository = personnelRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        PersonnelMobile personnel = personnelRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable: " + login));

        String role = "P001".equals(personnel.getCodeposte()) ? "ROLE_LIVREUR" : "ROLE_CONTROLEUR";

        return User.builder()
                .username(personnel.getLogin())
                .password(personnel.getMotPasse())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(role)))
                .disabled(!Boolean.TRUE.equals(personnel.getActif()))
                .build();
    }
}
