package com.supervision.livraisons.repository;

import com.supervision.livraisons.model.HistoriqueLivraison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueRepository extends JpaRepository<HistoriqueLivraison, Integer> {
    List<HistoriqueLivraison> findByNocdeOrderByDateModificationDesc(Integer nocde);
}
