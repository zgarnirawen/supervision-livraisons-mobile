package com.supervision.livraisons.repository;

import com.supervision.livraisons.model.LivraisonGeopoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LivraisonGeopointRepository extends JpaRepository<LivraisonGeopoint, Long> {
    List<LivraisonGeopoint> findByNocdeOrderByCapturedAtDesc(Integer nocde);
    Optional<LivraisonGeopoint> findFirstByNocdeOrderByCapturedAtDesc(Integer nocde);
}
