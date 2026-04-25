package com.supervision.livraisons.service;

import com.supervision.livraisons.dto.*;
import com.supervision.livraisons.model.*;
import com.supervision.livraisons.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class LivraisonService {

    private final LivraisonRepository livraisonRepo;
    private final ArticleCommandeRepository articleRepo;
    private final HistoriqueRepository historiqueRepo;
    private final LivraisonGeopointRepository geopointRepo;
    private final PodAssetRepository podAssetRepo;
    private final ChatMessageRepository chatRepo;

    public LivraisonService(LivraisonRepository livraisonRepo,
                            ArticleCommandeRepository articleRepo,
                            HistoriqueRepository historiqueRepo,
                            LivraisonGeopointRepository geopointRepo,
                            PodAssetRepository podAssetRepo,
                            ChatMessageRepository chatRepo) {
        this.livraisonRepo = livraisonRepo;
        this.articleRepo = articleRepo;
        this.historiqueRepo = historiqueRepo;
        this.geopointRepo = geopointRepo;
        this.podAssetRepo = podAssetRepo;
        this.chatRepo = chatRepo;
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

    @Transactional(readOnly = true)
    public LivraisonDetailDTO getDetailForLivreur(Integer nocde, Integer livreurId) {
        LivraisonMobile livraison = livraisonRepo.findByNocdeAndLivreurId(nocde, livreurId)
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
        historique.setReasonCode("STATUT_CHANGE");
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
        historique.setReasonCode("RAPPEL");
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

        List<Object[]> rawCategories = livraisonRepo.getStatsByCategorie(today);
        List<StatsDuJourDTO.StatsCategorieDTO> parCategorie = new ArrayList<>();
        for (Object[] row : rawCategories) {
            String categorie = row[0] != null ? row[0].toString() : "Non classée";
            long count = ((Number) row[1]).longValue();
            parCategorie.add(new StatsDuJourDTO.StatsCategorieDTO(categorie, count));
        }
        stats.setParCategorie(parCategorie);
        return stats;
    }

    // ── Historique d'une livraison ────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<HistoriqueLivraison> getHistorique(Integer nocde) {
        return historiqueRepo.findByNocdeOrderByDateModificationDesc(nocde);
    }

    @Transactional(readOnly = true)
    public Map<String, List<String>> getAllowedTransitions(Integer nocde) {
        LivraisonMobile livraison = livraisonRepo.findById(nocde)
                .orElseThrow(() -> new RuntimeException("Livraison introuvable: " + nocde));
        if ("EC".equals(livraison.getEtatliv())) {
            return Collections.singletonMap("allowed", Arrays.asList("LI", "AL"));
        }
        if ("AL".equals(livraison.getEtatliv())) {
            return Collections.singletonMap("allowed", Collections.singletonList("EC"));
        }
        return Collections.singletonMap("allowed", Collections.emptyList());
    }

    public LivraisonGeopoint publishGeopoint(Integer nocde, Integer livreurId, LivraisonGeopoint point) {
        livraisonRepo.findByNocdeAndLivreurId(nocde, livreurId)
                .orElseThrow(() -> new RuntimeException("Livraison introuvable: " + nocde));
        point.setNocde(nocde);
        point.setLivreurId(livreurId);
        return geopointRepo.save(point);
    }

    @Transactional(readOnly = true)
    public LivraisonGeopoint getLatestGeopoint(Integer nocde) {
        return geopointRepo.findFirstByNocdeOrderByCapturedAtDesc(nocde)
                .orElseThrow(() -> new RuntimeException("Aucun point GPS pour livraison: " + nocde));
    }

    @Transactional(readOnly = true)
    public List<LivraisonGeopoint> getGeopointHistory(Integer nocde) {
        return geopointRepo.findByNocdeOrderByCapturedAtDesc(nocde);
    }

    public PodAsset saveProof(Integer nocde, Integer actorId, PodAsset asset) {
        livraisonRepo.findById(nocde)
                .orElseThrow(() -> new RuntimeException("Livraison introuvable: " + nocde));
        asset.setNocde(nocde);
        asset.setCapturedBy(actorId);
        return podAssetRepo.save(asset);
    }

    @Transactional(readOnly = true)
    public List<PodAsset> getProofs(Integer nocde) {
        return podAssetRepo.findByNocdeOrderByCapturedAtDesc(nocde);
    }

    public ChatMessage postChatMessage(Integer nocde, Integer senderId, ChatMessage message) {
        livraisonRepo.findById(nocde)
                .orElseThrow(() -> new RuntimeException("Livraison introuvable: " + nocde));
        message.setNocde(nocde);
        message.setSenderId(senderId);
        return chatRepo.save(message);
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getChatMessages(Integer nocde) {
        return chatRepo.findByNocdeOrderBySentAtAsc(nocde);
    }
}
