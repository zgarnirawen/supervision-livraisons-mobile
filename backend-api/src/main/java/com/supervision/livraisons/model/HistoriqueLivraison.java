package com.supervision.livraisons.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historique_livraisons")
public class HistoriqueLivraison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nocde", nullable = false)
    private Integer nocde;

    @Column(name = "ancien_statut", length = 2)
    private String ancienStatut;

    @Column(name = "nouveau_statut", length = 2)
    private String nouveauStatut;

    @Column(name = "modifie_par")
    private Integer modifiePar;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @Column(name = "remarque", columnDefinition = "TEXT")
    private String remarque;

    @Column(name = "transition_source", length = 20)
    private String transitionSource = "mobile";

    @Column(name = "reason_code", length = 40)
    private String reasonCode;

    @Column(name = "request_id")
    private java.util.UUID requestId;

    @PrePersist
    protected void onCreate() {
        dateModification = LocalDateTime.now();
    }

    // Constructors
    public HistoriqueLivraison() {}

    public HistoriqueLivraison(Integer nocde, String ancienStatut, String nouveauStatut,
                                Integer modifiePar, String remarque) {
        this.nocde = nocde;
        this.ancienStatut = ancienStatut;
        this.nouveauStatut = nouveauStatut;
        this.modifiePar = modifiePar;
        this.remarque = remarque;
    }

    // Getters & Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getNocde() { return nocde; }
    public void setNocde(Integer nocde) { this.nocde = nocde; }

    public String getAncienStatut() { return ancienStatut; }
    public void setAncienStatut(String ancienStatut) { this.ancienStatut = ancienStatut; }

    public String getNouveauStatut() { return nouveauStatut; }
    public void setNouveauStatut(String nouveauStatut) { this.nouveauStatut = nouveauStatut; }

    public Integer getModifiePar() { return modifiePar; }
    public void setModifiePar(Integer modifiePar) { this.modifiePar = modifiePar; }

    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

    public String getRemarque() { return remarque; }
    public void setRemarque(String remarque) { this.remarque = remarque; }
    public String getTransitionSource() { return transitionSource; }
    public void setTransitionSource(String transitionSource) { this.transitionSource = transitionSource; }
    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }
    public java.util.UUID getRequestId() { return requestId; }
    public void setRequestId(java.util.UUID requestId) { this.requestId = requestId; }
}
