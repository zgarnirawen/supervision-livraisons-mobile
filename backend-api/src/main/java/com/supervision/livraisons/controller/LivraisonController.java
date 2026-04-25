package com.supervision.livraisons.controller;

import com.supervision.livraisons.dto.LivraisonDetailDTO;
import com.supervision.livraisons.dto.StatsDuJourDTO;
import com.supervision.livraisons.model.ChatMessage;
import com.supervision.livraisons.model.HistoriqueLivraison;
import com.supervision.livraisons.model.LivraisonGeopoint;
import com.supervision.livraisons.model.LivraisonMobile;
import com.supervision.livraisons.model.PodAsset;
import com.supervision.livraisons.security.JwtUtils;
import com.supervision.livraisons.service.LivraisonService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/livraisons")
@CrossOrigin(origins = "*")
public class LivraisonController {

    private final LivraisonService livraisonService;
    private final JwtUtils jwtUtils;

    public LivraisonController(LivraisonService livraisonService, JwtUtils jwtUtils) {
        this.livraisonService = livraisonService;
        this.jwtUtils = jwtUtils;
    }

    // ──────────────────────────────────────────────────────────────────────
    // GET /api/livraisons
    // Livreur → ses livraisons du jour
    // ──────────────────────────────────────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasRole('LIVREUR')")
    public ResponseEntity<List<LivraisonMobile>> getMesLivraisons(HttpServletRequest request) {
        Integer livreurId = extractIdpers(request);
        List<LivraisonMobile> livraisons = livraisonService.getLivraisonsLivreur(livreurId);
        return ResponseEntity.ok(livraisons);
    }

    // ──────────────────────────────────────────────────────────────────────
    // GET /api/livraisons/all?etatliv=EC&ville=Tunis&livreurId=1
    // Contrôleur → toutes les livraisons du jour avec filtres optionnels
    // ──────────────────────────────────────────────────────────────────────
    @GetMapping("/all")
    @PreAuthorize("hasRole('CONTROLEUR')")
    public ResponseEntity<List<LivraisonMobile>> getAllLivraisons(
            @RequestParam(required = false) String etatliv,
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) Integer livreurId) {
        List<LivraisonMobile> livraisons = livraisonService.getAllLivraisons(etatliv, ville, livreurId);
        return ResponseEntity.ok(livraisons);
    }

    // ──────────────────────────────────────────────────────────────────────
    // GET /api/livraisons/{nocde}
    // Détail complet avec articles de la commande
    // ──────────────────────────────────────────────────────────────────────
    @GetMapping("/{nocde}")
    @PreAuthorize("hasAnyRole('LIVREUR','CONTROLEUR')")
    public ResponseEntity<LivraisonDetailDTO> getDetail(@PathVariable Integer nocde) {
        LivraisonDetailDTO detail = livraisonService.getDetail(nocde);
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/{nocde}/mine")
    @PreAuthorize("hasRole('LIVREUR')")
    public ResponseEntity<LivraisonDetailDTO> getDetailMine(
            @PathVariable Integer nocde,
            HttpServletRequest request) {
        Integer livreurId = extractIdpers(request);
        return ResponseEntity.ok(livraisonService.getDetailForLivreur(nocde, livreurId));
    }

    // ──────────────────────────────────────────────────────────────────────
    // PUT /api/livraisons/{nocde}/statut
    // Livreur → changer statut EC → LI ou AL
    // Body: { "nouveauStatut": "LI", "remarque": "...", "causeAjournement": "..." }
    // ──────────────────────────────────────────────────────────────────────
    @PutMapping("/{nocde}/statut")
    @PreAuthorize("hasRole('LIVREUR')")
    public ResponseEntity<LivraisonMobile> changerStatut(
            @PathVariable Integer nocde,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        String nouveauStatut = body.get("nouveauStatut");
        String remarque = body.get("remarque");
        String causeAjournement = body.get("causeAjournement");
        Integer idpers = extractIdpers(request);

        LivraisonMobile updated = livraisonService.changerStatut(
                nocde, nouveauStatut, remarque, causeAjournement, idpers);
        return ResponseEntity.ok(updated);
    }

    // ──────────────────────────────────────────────────────────────────────
    // PUT /api/livraisons/{nocde}/remarque
    // Livreur → ajouter/modifier une remarque
    // Body: { "remarque": "Client absent, boîte aux lettres" }
    // ──────────────────────────────────────────────────────────────────────
    @PutMapping("/{nocde}/remarque")
    @PreAuthorize("hasRole('LIVREUR')")
    public ResponseEntity<LivraisonMobile> ajouterRemarque(
            @PathVariable Integer nocde,
            @RequestBody Map<String, String> body) {
        String remarque = body.get("remarque");
        LivraisonMobile updated = livraisonService.ajouterRemarque(nocde, remarque);
        return ResponseEntity.ok(updated);
    }

    // ──────────────────────────────────────────────────────────────────────
    // PUT /api/livraisons/{nocde}/rappel
    // Contrôleur → enregistrer une tentative de rappel
    // Body: { "dateTentativeRappel": "2026-04-25T14:30:00" }
    // ──────────────────────────────────────────────────────────────────────
    @PutMapping("/{nocde}/rappel")
    @PreAuthorize("hasRole('CONTROLEUR')")
    public ResponseEntity<LivraisonMobile> enregistrerRappel(
            @PathVariable Integer nocde,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        Integer idpers = extractIdpers(request);
        LocalDateTime dateRappel = null;
        String dateStr = body.get("dateTentativeRappel");
        if (StringUtils.hasText(dateStr)) {
            dateRappel = LocalDateTime.parse(dateStr);
        }

        LivraisonMobile updated = livraisonService.enregistrerRappel(nocde, dateRappel, idpers);
        return ResponseEntity.ok(updated);
    }

    // ──────────────────────────────────────────────────────────────────────
    // GET /api/livraisons/stats
    // Contrôleur → statistiques du jour
    // ──────────────────────────────────────────────────────────────────────
    @GetMapping("/stats")
    @PreAuthorize("hasRole('CONTROLEUR')")
    public ResponseEntity<StatsDuJourDTO> getStats() {
        StatsDuJourDTO stats = livraisonService.getStatsDuJour();
        return ResponseEntity.ok(stats);
    }

    // ──────────────────────────────────────────────────────────────────────
    // GET /api/livraisons/{nocde}/historique
    // Audit trail de la livraison
    // ──────────────────────────────────────────────────────────────────────
    @GetMapping("/{nocde}/historique")
    @PreAuthorize("hasAnyRole('LIVREUR','CONTROLEUR')")
    public ResponseEntity<List<HistoriqueLivraison>> getHistorique(@PathVariable Integer nocde) {
        return ResponseEntity.ok(livraisonService.getHistorique(nocde));
    }

    @GetMapping("/{nocde}/transitions")
    @PreAuthorize("hasAnyRole('LIVREUR','CONTROLEUR')")
    public ResponseEntity<Map<String, List<String>>> getAllowedTransitions(@PathVariable Integer nocde) {
        return ResponseEntity.ok(livraisonService.getAllowedTransitions(nocde));
    }

    @PostMapping("/{nocde}/location")
    @PreAuthorize("hasRole('LIVREUR')")
    public ResponseEntity<LivraisonGeopoint> publishLocation(
            @PathVariable Integer nocde,
            @RequestBody LivraisonGeopoint point,
            HttpServletRequest request) {
        Integer livreurId = extractIdpers(request);
        return ResponseEntity.ok(livraisonService.publishGeopoint(nocde, livreurId, point));
    }

    @GetMapping("/{nocde}/location/latest")
    @PreAuthorize("hasAnyRole('LIVREUR','CONTROLEUR')")
    public ResponseEntity<LivraisonGeopoint> getLatestLocation(@PathVariable Integer nocde) {
        return ResponseEntity.ok(livraisonService.getLatestGeopoint(nocde));
    }

    @GetMapping("/{nocde}/location/history")
    @PreAuthorize("hasAnyRole('LIVREUR','CONTROLEUR')")
    public ResponseEntity<List<LivraisonGeopoint>> getLocationHistory(@PathVariable Integer nocde) {
        return ResponseEntity.ok(livraisonService.getGeopointHistory(nocde));
    }

    @PostMapping("/{nocde}/proof")
    @PreAuthorize("hasAnyRole('LIVREUR','CONTROLEUR')")
    public ResponseEntity<PodAsset> saveProof(
            @PathVariable Integer nocde,
            @RequestBody PodAsset asset,
            HttpServletRequest request) {
        Integer idpers = extractIdpers(request);
        return ResponseEntity.ok(livraisonService.saveProof(nocde, idpers, asset));
    }

    @GetMapping("/{nocde}/proof")
    @PreAuthorize("hasAnyRole('LIVREUR','CONTROLEUR')")
    public ResponseEntity<List<PodAsset>> getProofs(@PathVariable Integer nocde) {
        return ResponseEntity.ok(livraisonService.getProofs(nocde));
    }

    @GetMapping("/{nocde}/chat")
    @PreAuthorize("hasAnyRole('LIVREUR','CONTROLEUR')")
    public ResponseEntity<List<ChatMessage>> getChatMessages(@PathVariable Integer nocde) {
        return ResponseEntity.ok(livraisonService.getChatMessages(nocde));
    }

    @PostMapping("/{nocde}/chat")
    @PreAuthorize("hasAnyRole('LIVREUR','CONTROLEUR')")
    public ResponseEntity<ChatMessage> postChatMessage(
            @PathVariable Integer nocde,
            @RequestBody ChatMessage message,
            HttpServletRequest request) {
        Integer idpers = extractIdpers(request);
        ChatMessage created = livraisonService.postChatMessage(nocde, idpers, message);
        return ResponseEntity.ok(created);
    }

    // ── Helper: extraire idpers depuis JWT ─────────────────────────────
    private Integer extractIdpers(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return jwtUtils.getIdpersFromToken(header.substring(7));
        }
        throw new RuntimeException("Token JWT manquant");
    }
}
