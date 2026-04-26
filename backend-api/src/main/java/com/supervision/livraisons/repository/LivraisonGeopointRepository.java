package com.supervision.livraisons.repository;

import com.supervision.livraisons.model.LivraisonGeopoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LivraisonGeopointRepository extends JpaRepository<LivraisonGeopoint, Long> {
    List<LivraisonGeopoint> findByNocdeOrderByCapturedAtDesc(Integer nocde);
    Optional<LivraisonGeopoint> findFirstByNocdeOrderByCapturedAtDesc(Integer nocde);
    List<LivraisonGeopoint> findByLivreurIdOrderByCapturedAtDesc(Integer livreurId);

    @org.springframework.data.jpa.repository.Query(value = 
        "SELECT DISTINCT ON (livreur_id) * FROM livraison_geopoints " +
        "ORDER BY livreur_id, captured_at DESC", nativeQuery = true)
    List<LivraisonGeopoint> findAllLatestPerLivreur();
}
