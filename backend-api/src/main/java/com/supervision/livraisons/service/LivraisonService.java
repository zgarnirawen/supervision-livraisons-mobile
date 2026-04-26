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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class LivraisonService {

    private final LivraisonRepository livraisonRepo;
    private final ArticleCommandeRepository articleRepo;
    private final HistoriqueRepository historiqueRepo;
    private final LivraisonGeopointRepository geopointRepo;
    private final PodAssetRepository podAssetRepo;
    private final ChatMessageRepository chatRepo;
    private final PersonnelRepository personnelRepo;
    private final FirebaseService firebaseService;

    public LivraisonService(LivraisonRepository livraisonRepo,
                            ArticleCommandeRepository articleRepo,
                            HistoriqueRepository historiqueRepo,
                            LivraisonGeopointRepository geopointRepo,
                            PodAssetRepository podAssetRepo,
                            ChatMessageRepository chatRepo,
                            PersonnelRepository personnelRepo,
                            FirebaseService firebaseService) {
        this.livraisonRepo = livraisonRepo;
        this.articleRepo = articleRepo;
        this.historiqueRepo = historiqueRepo;
        this.geopointRepo = geopointRepo;
        this.podAssetRepo = podAssetRepo;
        this.chatRepo = chatRepo;
        this.personnelRepo = personnelRepo;
        this.firebaseService = firebaseService;
    }

    private LocalDate getEffectiveDate() {
        LocalDateTime now = LocalDateTime.now();
        // Si on est entre minuit et 4h du matin, on considère encore la journée d'hier
        if (now.getHour() < 4) {
            return now.toLocalDate().minusDays(1);
        }
        return now.toLocalDate();
    }

    // ── Livreur: Ses livraisons du jour ───────────────────────────────────
    @Transactional(readOnly = true)
    public List<LivraisonMobile> getLivraisonsLivreur(Integer livreurId) {
        return livraisonRepo.findByLivreurIdAndDateliv(livreurId, getEffectiveDate());
    }

    // ── Contrôleur: Toutes les livraisons du jour ─────────────────────────
    @Transactional(readOnly = true)
    public List<LivraisonMobile> getAllLivraisons(String etatliv, String ville, Integer livreurId) {
        LocalDate today = getEffectiveDate();

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

    @Transactional(readOnly = true)
    public List<LivraisonMobile> getActiveConversations() {
        // Retourne les livraisons d'aujourd'hui + celles ayant des messages (persistentes)
        return livraisonRepo.findActiveConversations(getEffectiveDate());
    }

    @Transactional(readOnly = true)
    public List<ClientStatsDTO> getClientsStats() {
        LocalDate today = getEffectiveDate();
        List<LivraisonMobile> all = livraisonRepo.findByDateliv(today);
        Map<String, ClientStatsDTO> map = new java.util.HashMap<>();

        for (LivraisonMobile l : all) {
            String key = l.getClientTel() != null ? l.getClientTel() : l.getClientNom();
            if (key == null) key = "Inconnu";
            
            ClientStatsDTO stats = map.computeIfAbsent(key, k -> {
                ClientStatsDTO dto = new ClientStatsDTO();
                dto.setNom(l.getClientNom());
                dto.setPrenom(l.getClientPrenom());
                dto.setTel(l.getClientTel());
                dto.setAdresse(l.getClientAdresse());
                dto.setVille(l.getClientVille());
                dto.setLatitude(l.getClientLatitude() != null ? l.getClientLatitude().doubleValue() : null);
                dto.setLongitude(l.getClientLongitude() != null ? l.getClientLongitude().doubleValue() : null);
                dto.setCategorie(l.getCategorie());
                return dto;
            });
            
            stats.setTotalLivraisons(stats.getTotalLivraisons() + 1);
            if ("LI".equals(l.getEtatliv())) stats.setLivrees(stats.getLivrees() + 1);
            else if ("AL".equals(l.getEtatliv())) stats.setAjournees(stats.getAjournees() + 1);
            else stats.setEnCours(stats.getEnCours() + 1);
        }
        
        return new ArrayList<>(map.values());
    }

    @Transactional(readOnly = true)
    public List<LivraisonMobile> getActiveConversationsForLivreur(Integer livreurId) {
        return livraisonRepo.findActiveConversationsLivreur(getEffectiveDate(), livreurId);
    }

    // ── Détail d'une livraison (avec articles) ────────────────────────────
    @Transactional(readOnly = true)
    public LivraisonDetailDTO getDetail(Integer nocde) {
        LivraisonMobile livraison = livraisonRepo.findById(nocde)
                .orElseThrow(() -> new RuntimeException("Livraison introuvable: " + nocde));
        List<ArticleCommande> articles = articleRepo.findByNocde(nocde);
        List<PodAsset> proofs = podAssetRepo.findByNocdeOrderByCapturedAtDesc(nocde);
        return LivraisonDetailDTO.from(livraison, articles, proofs);
    }

    @Transactional(readOnly = true)
    public LivraisonDetailDTO getDetailForLivreur(Integer nocde, Integer livreurId) {
        LivraisonMobile livraison = livraisonRepo.findByNocdeAndLivreurId(nocde, livreurId)
                .orElseThrow(() -> new RuntimeException("Livraison introuvable: " + nocde));
        List<ArticleCommande> articles = articleRepo.findByNocde(nocde);
        List<PodAsset> proofs = podAssetRepo.findByNocdeOrderByCapturedAtDesc(nocde);
        return LivraisonDetailDTO.from(livraison, articles, proofs);
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
        if ("AL".equals(nouveauStatut) && (causeAjournement == null || causeAjournement.trim().isEmpty())) {
            throw new IllegalArgumentException("La cause d'ajournement est obligatoire.");
        }

        // Mise à jour
        livraison.setEtatliv(nouveauStatut);
        if (remarque != null) livraison.setRemarque(remarque);
        if ("AL".equals(nouveauStatut)) {
            livraison.setCauseAjournement(causeAjournement.trim());
        }
        livraison.setSyncToOracle(false);

        // Historique
        HistoriqueLivraison historique = new HistoriqueLivraison(
                nocde, ancienStatut, nouveauStatut, idpersModificateur, remarque);
        historique.setReasonCode("STATUT_CHANGE");
        historiqueRepo.save(historique);

        // En cas d'ajournement: push urgent + message d'urgence automatique dans le chat commande
        if ("AL".equals(nouveauStatut)) {
            notifyAjournementAndCreateEmergencyMessage(livraison, causeAjournement.trim(), idpersModificateur);
        }

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
        LocalDate today = getEffectiveDate();
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
            String tel = row[7] != null ? row[7].toString() : "";
            parLivreur.add(new StatsDuJourDTO.StatsLivreurDTO(
                    livreurId, nom + " " + prenom, tot, liv, enc, ajo, tel));
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

    @Transactional(readOnly = true)
    public List<LivreurLocationDTO> getAllLivreurLocations() {
        List<LivraisonGeopoint> latestPoints = geopointRepo.findAllLatestPerLivreur();
        List<LivreurLocationDTO> result = new ArrayList<>();
        
        for (LivraisonGeopoint p : latestPoints) {
            String nom = personnelRepo.findById(p.getLivreurId())
                    .map(pers -> pers.getNompers() + " " + pers.getPrenompers())
                    .orElse("Livreur #" + p.getLivreurId());
                    
            result.add(new LivreurLocationDTO(
                    p.getLivreurId(), nom, p.getLatitude().doubleValue(), p.getLongitude().doubleValue(), p.getCapturedAt()
            ));
        }
        return result;
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

    public ChatMessage postChatMessage(Integer nocde, Integer senderId, String senderCodeposte, ChatMessage message) {
        if (message == null || message.getMessageText() == null || message.getMessageText().trim().isEmpty()) {
            throw new IllegalArgumentException("Le message ne peut pas être vide.");
        }

        ChatContext context = resolveChatContext(nocde, senderId, senderCodeposte);

        message.setNocde(nocde);
        message.setSenderId(senderId);
        message.setRecipientId(context.recipientId);
        message.setMessageText(message.getMessageText().trim());

        ChatMessage saved = chatRepo.save(message);
        sendPushForNewMessage(saved, context, senderId, senderCodeposte);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getChatMessages(Integer nocde, Integer actorId, String actorCodeposte) {
        resolveChatContext(nocde, actorId, actorCodeposte);
        return chatRepo.findByNocdeOrderBySentAtAsc(nocde);
    }

    public void updateLivreurLocation(Integer livreurId, Double latitude, Double longitude) {
        // Récupère une livraison active pour ce livreur aujourd'hui pour avoir un nocde de référence
        List<LivraisonMobile> active = getLivraisonsLivreur(livreurId);
        Integer nocde = active.isEmpty() ? 0 : active.get(0).getNocde();

        LivraisonGeopoint point = new LivraisonGeopoint();
        point.setLivreurId(livreurId);
        point.setNocde(nocde);
        point.setLatitude(new java.math.BigDecimal(latitude));
        point.setLongitude(new java.math.BigDecimal(longitude));
        geopointRepo.save(point);
    }

    @Transactional(readOnly = true)
    public LivreurLocationDTO getLatestLivreurLocation(Integer livreurId) {
        List<LivraisonGeopoint> all = geopointRepo.findByLivreurIdOrderByCapturedAtDesc(livreurId);
        if (all.isEmpty()) {
            return null;
        }
        LivraisonGeopoint latest = all.get(0);
        String nom = personnelRepo.findById(livreurId)
                .map(pers -> pers.getNompers() + " " + pers.getPrenompers())
                .orElse("Livreur #" + livreurId);
        return new LivreurLocationDTO(
                livreurId, nom, latest.getLatitude().doubleValue(),
                latest.getLongitude().doubleValue(), latest.getCapturedAt()
        );
    }
    @Transactional(readOnly = true)
    public List<com.supervision.livraisons.dto.ChatChannelDTO> getChatChannels(Integer userId, String userCodeposte) {
        Set<Integer> channelIds = new LinkedHashSet<>();
        
        // Livreur: voir ses propres livraisons du jour + sa conversation privée
        if ("P001".equals(userCodeposte)) {
            livraisonRepo.findByLivreurIdAndDateliv(userId, getEffectiveDate()).forEach(l -> channelIds.add(l.getNocde()));
            channelIds.add(-userId); // Ajouter sa conversation privée
        }
        // Contrôleur: voir toutes les livraisons du jour + toutes les conversations de support
        else if ("P003".equals(userCodeposte)) {
            livraisonRepo.findByDateliv(getEffectiveDate()).forEach(l -> channelIds.add(l.getNocde()));
            personnelRepo.findByCodeposte("P001").forEach(p -> channelIds.add(-p.getIdpers()));
        }
        
        // Inclure également les chans avec messages liés à cet utilisateur
        if ("P001".equals(userCodeposte)) {
            channelIds.addAll(chatRepo.findDistinctNocdeByUserId(userId));
        } else {
            // Le contrôleur voit tout l'historique
            chatRepo.findAll().forEach(m -> channelIds.add(m.getNocde()));
        }

        List<com.supervision.livraisons.dto.ChatChannelDTO> channels = new ArrayList<>();

        for (Integer nocde : channelIds) {
            String title;
            boolean isSupport = false;
            if (nocde > 0) {
                title = livraisonRepo.findById(nocde)
                        .map(l -> "Livraison #" + l.getNocde() + " - " + l.getClientNom())
                        .orElse("Livraison #" + nocde);
            } else {
                int livreurId = -nocde;
                title = personnelRepo.findById(livreurId)
                        .map(p -> "Support: " + p.getNompers() + " " + p.getPrenompers())
                        .orElse("Support Livreur #" + livreurId);
                isSupport = true;
            }
            
            String lastMessage = "Ouvrir la conversation";
            LocalDateTime lastDate = null;
            
            List<ChatMessage> msgs = chatRepo.findByNocdeOrderBySentAtAsc(nocde);
            if (!msgs.isEmpty()) {
                ChatMessage last = msgs.get(msgs.size() - 1);
                lastMessage = last.getMessageText();
                lastDate = last.getSentAt();
            }

            channels.add(new com.supervision.livraisons.dto.ChatChannelDTO(nocde, title, lastMessage, lastDate, isSupport));
        }
        return channels;
    }

    private void notifyAjournementAndCreateEmergencyMessage(LivraisonMobile livraison, String causeAjournement, Integer senderId) {
        String title = "Alerte Ajournement";
        String body = "Commande #" + livraison.getNocde() + " ajournée: " + causeAjournement;

        List<PersonnelMobile> controleurs = personnelRepo.findByCodeposte("P003");
        for (PersonnelMobile c : controleurs) {
            if (c.getFcmToken() != null && !c.getFcmToken().isEmpty()) {
                firebaseService.sendNotification(c.getFcmToken(), title, body);
            }
        }

        ChatMessage emergency = new ChatMessage();
        emergency.setNocde(livraison.getNocde());
        emergency.setSenderId(senderId);
        emergency.setMessageText("[URGENT] Livraison ajournée. Cause: " + causeAjournement);
        emergency.setRecipientId(controleurs.isEmpty() ? null : controleurs.get(0).getIdpers());
        chatRepo.save(emergency);
    }

    private void sendPushForNewMessage(ChatMessage msg, ChatContext context, Integer senderId, String senderCodeposte) {
        String title = context.supportChannel
                ? "Nouveau message (Conversation générale)"
                : "Nouveau message (Commande #" + msg.getNocde() + ")";
        String body = msg.getMessageText();

        if ("P001".equals(senderCodeposte)) {
            personnelRepo.findByCodeposte("P003").stream()
                    .filter(p -> !p.getIdpers().equals(senderId))
                    .filter(p -> p.getFcmToken() != null && !p.getFcmToken().isEmpty())
                    .forEach(p -> firebaseService.sendNotification(p.getFcmToken(), title, body));
            return;
        }

        if (context.recipientId != null) {
            personnelRepo.findById(context.recipientId)
                    .filter(p -> p.getFcmToken() != null && !p.getFcmToken().isEmpty())
                    .ifPresent(p -> firebaseService.sendNotification(p.getFcmToken(), title, body));
        }
    }

    private ChatContext resolveChatContext(Integer nocde, Integer actorId, String actorCodeposte) {
        if (nocde == null || nocde == 0) {
            throw new IllegalArgumentException("Canal de chat invalide.");
        }

        if (nocde < 0) {
            Integer livreurId = -nocde;
            if (!"P003".equals(actorCodeposte) && !("P001".equals(actorCodeposte) && actorId.equals(livreurId))) {
                throw new IllegalStateException("Accès non autorisé à cette conversation générale.");
            }
            Integer recipientId = "P003".equals(actorCodeposte) ? livreurId : firstControleurId();
            return new ChatContext(true, recipientId);
        }

        LivraisonMobile livraison = livraisonRepo.findById(nocde)
                .orElseThrow(() -> new RuntimeException("Livraison introuvable: " + nocde));

        if ("P001".equals(actorCodeposte) && !actorId.equals(livraison.getLivreurId())) {
            throw new IllegalStateException("Le livreur ne peut accéder qu'à ses propres commandes.");
        }
        if (!"P001".equals(actorCodeposte) && !"P003".equals(actorCodeposte)) {
            throw new IllegalStateException("Rôle non autorisé pour le chat.");
        }

        Integer recipientId = "P003".equals(actorCodeposte) ? livraison.getLivreurId() : firstControleurId();
        return new ChatContext(false, recipientId);
    }

    private Integer firstControleurId() {
        return personnelRepo.findByCodeposte("P003").stream()
                .map(PersonnelMobile::getIdpers)
                .findFirst()
                .orElse(null);
    }

    private static class ChatContext {
        private final boolean supportChannel;
        private final Integer recipientId;

        private ChatContext(boolean supportChannel, Integer recipientId) {
            this.supportChannel = supportChannel;
            this.recipientId = recipientId;
        }
    }
}
