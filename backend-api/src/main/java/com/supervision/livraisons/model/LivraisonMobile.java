package com.supervision.livraisons.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "livraisons_mobile")
public class LivraisonMobile {

    @Id
    @Column(name = "nocde")
    private Integer nocde;

    @Column(name = "dateliv", nullable = false)
    private LocalDate dateliv;

    // Livreur (dénormalisé)
    @Column(name = "livreur_id", nullable = false)
    private Integer livreurId;

    @Column(name = "livreur_nom", length = 30)
    private String livreurNom;

    @Column(name = "livreur_prenom", length = 30)
    private String livreurPrenom;

    @Column(name = "livreur_tel", length = 8)
    private String livreurTel;

    // Client (dénormalisé)
    @Column(name = "client_nom", length = 60)
    private String clientNom;

    @Column(name = "client_prenom", length = 30)
    private String clientPrenom;

    @Column(name = "client_tel", length = 8)
    private String clientTel;

    @Column(name = "client_adresse", length = 60)
    private String clientAdresse;

    @Column(name = "client_ville", length = 30)
    private String clientVille;

    @Column(name = "client_code_postal", length = 5)
    private String clientCodePostal;

    // État livraison
    @Column(name = "etatliv", length = 2)
    private String etatliv = "EC";

    @Column(name = "modepay", length = 20)
    private String modepay;

    // Champs mobiles enrichis
    @Column(name = "remarque", columnDefinition = "TEXT")
    private String remarque;

    @Column(name = "cause_ajournement", length = 100)
    private String causeAjournement;

    @Column(name = "date_tentative_rappel")
    private LocalDateTime dateTentativeRappel;

    // Métadonnées
    @Column(name = "date_chargement")
    private LocalDateTime dateChargement;

    @Column(name = "derniere_modification")
    private LocalDateTime derniereModification;

    @Column(name = "sync_to_oracle")
    private Boolean syncToOracle = false;

    @PrePersist
    protected void onCreate() {
        dateChargement = LocalDateTime.now();
        derniereModification = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        derniereModification = LocalDateTime.now();
    }

    // Constructors
    public LivraisonMobile() {}

    // Getters & Setters
    public Integer getNocde() { return nocde; }
    public void setNocde(Integer nocde) { this.nocde = nocde; }

    public LocalDate getDateliv() { return dateliv; }
    public void setDateliv(LocalDate dateliv) { this.dateliv = dateliv; }

    public Integer getLivreurId() { return livreurId; }
    public void setLivreurId(Integer livreurId) { this.livreurId = livreurId; }

    public String getLivreurNom() { return livreurNom; }
    public void setLivreurNom(String livreurNom) { this.livreurNom = livreurNom; }

    public String getLivreurPrenom() { return livreurPrenom; }
    public void setLivreurPrenom(String livreurPrenom) { this.livreurPrenom = livreurPrenom; }

    public String getLivreurTel() { return livreurTel; }
    public void setLivreurTel(String livreurTel) { this.livreurTel = livreurTel; }

    public String getClientNom() { return clientNom; }
    public void setClientNom(String clientNom) { this.clientNom = clientNom; }

    public String getClientPrenom() { return clientPrenom; }
    public void setClientPrenom(String clientPrenom) { this.clientPrenom = clientPrenom; }

    public String getClientTel() { return clientTel; }
    public void setClientTel(String clientTel) { this.clientTel = clientTel; }

    public String getClientAdresse() { return clientAdresse; }
    public void setClientAdresse(String clientAdresse) { this.clientAdresse = clientAdresse; }

    public String getClientVille() { return clientVille; }
    public void setClientVille(String clientVille) { this.clientVille = clientVille; }

    public String getClientCodePostal() { return clientCodePostal; }
    public void setClientCodePostal(String clientCodePostal) { this.clientCodePostal = clientCodePostal; }

    public String getEtatliv() { return etatliv; }
    public void setEtatliv(String etatliv) { this.etatliv = etatliv; }

    public String getModepay() { return modepay; }
    public void setModepay(String modepay) { this.modepay = modepay; }

    public String getRemarque() { return remarque; }
    public void setRemarque(String remarque) { this.remarque = remarque; }

    public String getCauseAjournement() { return causeAjournement; }
    public void setCauseAjournement(String causeAjournement) { this.causeAjournement = causeAjournement; }

    public LocalDateTime getDateTentativeRappel() { return dateTentativeRappel; }
    public void setDateTentativeRappel(LocalDateTime dateTentativeRappel) { this.dateTentativeRappel = dateTentativeRappel; }

    public LocalDateTime getDateChargement() { return dateChargement; }
    public void setDateChargement(LocalDateTime dateChargement) { this.dateChargement = dateChargement; }

    public LocalDateTime getDerniereModification() { return derniereModification; }
    public void setDerniereModification(LocalDateTime derniereModification) { this.derniereModification = derniereModification; }

    public Boolean getSyncToOracle() { return syncToOracle; }
    public void setSyncToOracle(Boolean syncToOracle) { this.syncToOracle = syncToOracle; }
}
