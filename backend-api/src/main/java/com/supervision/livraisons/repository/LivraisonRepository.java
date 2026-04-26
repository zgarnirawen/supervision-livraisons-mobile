package com.supervision.livraisons.repository;

import com.supervision.livraisons.model.LivraisonMobile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LivraisonRepository extends JpaRepository<LivraisonMobile, Integer> {

    // Livraisons d'un livreur pour aujourd'hui
    List<LivraisonMobile> findByLivreurIdAndDateliv(Integer livreurId, LocalDate dateliv);

    // Toutes les livraisons d'une date (pour le contrôleur)
    List<LivraisonMobile> findByDateliv(LocalDate dateliv);
    List<LivraisonMobile> findAllByDatelivOrderByDerniereModificationDesc(LocalDate dateliv);

    // Filtrer par état
    List<LivraisonMobile> findByDatelivAndEtatliv(LocalDate dateliv, String etatliv);

    // Filtrer par ville
    List<LivraisonMobile> findByDatelivAndClientVilleIgnoreCase(LocalDate dateliv, String ville);

    // Filtrer par livreur ET état
    List<LivraisonMobile> findByLivreurIdAndDatelivAndEtatliv(
            Integer livreurId, LocalDate dateliv, String etatliv);

    // Non synchronisées avec Oracle
    List<LivraisonMobile> findByDatelivAndSyncToOracleFalse(LocalDate dateliv);

    // Statistiques du jour
    @Query("SELECT COUNT(l) FROM LivraisonMobile l WHERE l.dateliv = :date")
    long countByDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(l) FROM LivraisonMobile l WHERE l.dateliv = :date AND l.etatliv = :etat")
    long countByDateAndEtat(@Param("date") LocalDate date, @Param("etat") String etat);

    // Résumé par livreur
    @Query("SELECT l.livreurId, l.livreurNom, l.livreurPrenom, " +
           "COUNT(l), " +
           "SUM(CASE WHEN l.etatliv = 'LI' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN l.etatliv = 'EC' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN l.etatliv = 'AL' THEN 1 ELSE 0 END), " +
           "MAX(COALESCE(l.livreurTel, '00000000')) " +
           "FROM LivraisonMobile l " +
           "WHERE l.dateliv = :date " +
           "GROUP BY l.livreurId, l.livreurNom, l.livreurPrenom")
    List<Object[]> getStatsByLivreur(@Param("date") LocalDate date);

    @Query("SELECT DISTINCT l FROM LivraisonMobile l " +
           "LEFT JOIN ChatMessage m ON l.nocde = m.nocde " +
           "WHERE l.dateliv = :date OR m.id IS NOT NULL " +
           "ORDER BY l.derniereModification DESC")
    List<LivraisonMobile> findActiveConversations(@Param("date") LocalDate date);

    @Query("SELECT DISTINCT l FROM LivraisonMobile l " +
           "LEFT JOIN ChatMessage m ON l.nocde = m.nocde " +
           "WHERE (l.dateliv = :date OR m.id IS NOT NULL) AND l.livreurId = :livreurId " +
           "ORDER BY l.derniereModification DESC")
    List<LivraisonMobile> findActiveConversationsLivreur(@Param("date") LocalDate date, @Param("livreurId") Integer livreurId);

    Optional<LivraisonMobile> findByNocdeAndLivreurId(Integer nocde, Integer livreurId);

    @Query("SELECT l.categorie, COUNT(l) FROM LivraisonMobile l " +
           "WHERE l.dateliv = :date GROUP BY l.categorie")
    List<Object[]> getStatsByCategorie(@Param("date") LocalDate date);
}
