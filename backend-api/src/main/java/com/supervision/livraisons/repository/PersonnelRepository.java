package com.supervision.livraisons.repository;

import com.supervision.livraisons.model.PersonnelMobile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonnelRepository extends JpaRepository<PersonnelMobile, Integer> {
    Optional<PersonnelMobile> findByLogin(String login);
    boolean existsByLogin(String login);
    java.util.List<PersonnelMobile> findByCodeposte(String codeposte);
}
