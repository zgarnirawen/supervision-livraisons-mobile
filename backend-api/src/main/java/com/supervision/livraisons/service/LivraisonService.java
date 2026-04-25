package com.supervision.livraisons.service;

import com.supervision.livraisons.dto.*;
import com.supervision.livraisons.model.*;
import com.supervision.livraisons.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class LivraisonService {

    private final LivraisonRepository livraisonRepo;
    private final ArticleCommandeRepository articleRepo;
    private final HistoriqueRepository historiqueRepo;

    public LivraisonService(LivraisonRepository livraisonRepo,
                            ArticleCommandeRepository articleRepo,
                            HistoriqueRepository historiqueRepo) {
        this.livraisonRepo = livraisonRepo;
        this.articleRepo = articleRepo;
        this.historiqueRepo = historiqueRepo;
    }

    // ── Livreur: Ses livraisons du jour ───────────────────────────────────
    @Transactional(readOnly = true)
    public List<LivraisonMobile> getLivraisonsLivreur(Integer livreurId) {
        return livraisonRepo.findByLivreurIdAndDateliv(livreurId, LocalDate.now());
    }

    // ── Contrôleur: Toutes les livraisons du jour ─────────────────────────
    @Transactional(readOnly = true)
    public List<LivraisonMobile> getAllLivraisons(String etatliv, String ville, Integer livreurId) {
        LocalDate today = LocalDate.now();

        if (etatliv != null && !etatliv.isEmpty()) {
            if (livreurId != null) {
                return livraisonRepo.findByLivreurIdAndDatelivAndEtatliv(livreurId, today, etatliv);
            }
            return livraisonRepo.findByDatelivAndEtatliv(today, etatliv);
        }
        if (ville != null && !ville.isEmpty()) {
            return livraisonRepo.findByDatelivAndClientVilleIgnoreCase(today, ville);
        }
        if (livreurId != null) {
            return livraisonRepo.findByLivreurIdAndDateliv(livreurId, today);
        }
        return livraisonRepo.findByDateliv(today);
    }

    // ── Détail d'une livraison (avec articles) ────────────────────────────
    @Transactional(readOnly = true)
    public LivraisonDetailDTO getDetail(Integer nocde) {
        LivraisonMobile livraison = livraisonRepo.findById(nocde)
                .orElseThrow(() -> new RuntimeException("Livraison introuvable: " + nocde));
        List<ArticleCommande> articles = articleRepo.findByNocde(nocde);
        return LivraisonDetailDTO.from(livraison, articles);
    }

    // ── Livreur: Changer statut (EC → LI ou AL) ──────────────────────────
    public LivraisonMobile changerStatut(Integer nocde, String nouveauStatut,
                                         String remarque, String causeAjournement,
                                         Integer idpersModificateur) {
        LivraisonMobile livraison = livraisonRepo.findById(nocde)
                .orElseThrow(() -> new RuntimeException("Livraison introuvable: " + nocde));

        // Validation de la transition d'état
        String ancienStatut = livraison.getEtatliv();
        if (!"EC".equals(ancienStatut)) {
            throw new IllegalStateException(
                "Impossible de modifier une livraison en état: " + ancienStatut
                + ". Seules les livraisons EC peuvent être modifiées.");
        }
        if (!"LI".equals(nouveauStatut) && !"AL".equals(nouveauStatut)) {
            throw new IllegalArgumentException(
                "Statut invalide: " + nouveauStatut + ". Valeurs autorisées: LI, AL");
        }

        // Mise à jour
        livraison.setEtatliv(nouveauStatut);
        if (remarque != null) livraison.setRemarque(remarque);
        if ("AL".equals(nouveauStatut) && causeAjournement != null) {
            livraison.setCauseAjournement(causeAjournement);
        }
        livraison.setSyncToOracle(false);

        // Historique
        HistoriqueLivraison historique = new HistoriqueLivraison(
                nocde, ancienStatut, nouveauStatut, idpersModificateur, remarque);
        historiqueRepo.save(historique);

        return livraisonRepo.save(livraison);
    }

    // ── Livreur: Ajouter/modifier remarque ──────────────────────────────
    public LivraisonMobile ajouterRemarque(Integer nocde, String remarque) {
        LivraisonMobile livraison = livraisonRepo.findById(nocde)
                .orElseThrow(() -> new RuntimeException("Livraison introuvable: " + nocde));
        livraison.setRemarque(remarque);
        livraison.setSyncToOracle(false);
        return livraisonRepo.save(livraison);
    }

    // ── Contrôleur: Enregistrer tentative de rappel ──────────────────────
    public LivraisonMobile enregistrerRappel(Integer nocde, LocalDateTime dateRappel,
                                              Integer idpersModificateur) {
        LivraisonMobile livraison = livraisonRepo.findById(nocde)
                .orElseThrow(() -> new RuntimeException("Livraison introuvable: " + nocde));

        if (!"AL".equals(livraison.getEtatliv())) {
            throw new IllegalStateException(
                "La tentative de rappel s'applique uniquement aux livraisons ajournées (AL).");
        }

        livraison.setDateTentativeRappel(dateRappel != null ? dateRappel : LocalDateTime.now());
        livraison.setSyncToOracle(false);

        HistoriqueLivraison historique = new HistoriqueLivraison(
                nocde, livraison.getEtatliv(), livraison.getEtatliv(),
                idpersModificateur, "Tentative de rappel enregistrée");
        historiqueRepo.save(historique);

        return livraisonRepo.save(livraison);
    }

    // ── Statistiques du jour (pour contrôleur) ───────────────────────────
    @Transactional(readOnly = true)
    public StatsDuJourDTO getStatsDuJour() {
        LocalDate today = LocalDate.now();
        StatsDuJourDTO stats = new StatsDuJourDTO();

        long total = livraisonRepo.countByDate(today);
        long livrees = livraisonRepo.countByDateAndEtat(today, "LI");
        long enCours = livraisonRepo.countByDateAndEtat(today, "EC");
        long ajournees = livraisonRepo.countByDateAndEtat(today, "AL");

        stats.setTotalLivraisons(total);
        stats.setLivrees(livrees);
        stats.setEnCours(enCours);
        stats.setAjournees(ajournees);
        stats.setTauxSucces(total > 0 ? Math.round((double) livrees / total * 1000.0) / 10.0 : 0.0);

        // Stats par livreur
        List<Object[]> rawStats = livraisonRepo.getStatsByLivreur(today);
        List<StatsDuJourDTO.StatsLivreurDTO> parLivreur = new ArrayList<>();
        for (Object[] row : rawStats) {
            Integer livreurId = (Integer) row[0];
            String nom = (String) row[1];
            String prenom = (String) row[2];
            long tot = ((Number) row[3]).longValue();
            long liv = ((Number) row[4]).longValue();
            long enc = ((Number) row[5]).longValue();
            long ajo = ((Number) row[6]).longValue();
            parLivreur.add(new StatsDuJourDTO.StatsLivreurDTO(
                    livreurId, nom + " " + prenom, tot, liv, enc, ajo));
        }
        stats.setParLivreur(parLivreur);
        return stats;
    }

    // ── Historique d'une livraison ────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<HistoriqueLivraison> getHistorique(Integer nocde) {
        return historiqueRepo.findByNocdeOrderByDateModificationDesc(nocde);
    }
}
