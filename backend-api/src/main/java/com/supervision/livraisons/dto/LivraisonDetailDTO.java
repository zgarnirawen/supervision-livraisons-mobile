package com.supervision.livraisons.dto;

import com.supervision.livraisons.model.ArticleCommande;
import com.supervision.livraisons.model.LivraisonMobile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class LivraisonDetailDTO {

    private Integer nocde;
    private LocalDate dateliv;

    // Livreur
    private Integer livreurId;
    private String livreurNom;
    private String livreurPrenom;
    private String livreurTel;
    private String livreurNomComplet;

    // Client
    private String clientNom;
    private String clientPrenom;
    private String clientNomComplet;
    private String clientTel;
    private String clientAdresse;
    private String clientVille;
    private String clientCodePostal;
    private java.math.BigDecimal clientLatitude;
    private java.math.BigDecimal clientLongitude;

    // Livraison
    private String etatliv;
    private String etatLibelle;
    private String modepay;
    private String categorie;
    private String remarque;
    private String causeAjournement;
    private LocalDateTime dateTentativeRappel;
    private LocalDateTime derniereModification;

    // Articles
    private List<ArticleCommande> articles;
    private BigDecimal montantTotal;

    public LivraisonDetailDTO() {}

    public static LivraisonDetailDTO from(LivraisonMobile l, List<ArticleCommande> articles) {
        LivraisonDetailDTO dto = new LivraisonDetailDTO();
        dto.nocde = l.getNocde();
        dto.dateliv = l.getDateliv();
        dto.livreurId = l.getLivreurId();
        dto.livreurNom = l.getLivreurNom();
        dto.livreurPrenom = l.getLivreurPrenom();
        dto.livreurTel = l.getLivreurTel();
        dto.livreurNomComplet = l.getLivreurNom() + " " + l.getLivreurPrenom();
        dto.clientNom = l.getClientNom();
        dto.clientPrenom = l.getClientPrenom();
        dto.clientNomComplet = l.getClientNom() + (l.getClientPrenom() != null ? " " + l.getClientPrenom() : "");
        dto.clientTel = l.getClientTel();
        dto.clientAdresse = l.getClientAdresse();
        dto.clientVille = l.getClientVille();
        dto.clientCodePostal = l.getClientCodePostal();
        dto.clientLatitude = l.getClientLatitude();
        dto.clientLongitude = l.getClientLongitude();
        dto.etatliv = l.getEtatliv();
        dto.etatLibelle = getEtatLibelle(l.getEtatliv());
        dto.modepay = l.getModepay();
        dto.categorie = l.getCategorie();
        dto.remarque = l.getRemarque();
        dto.causeAjournement = l.getCauseAjournement();
        dto.dateTentativeRappel = l.getDateTentativeRappel();
        dto.derniereModification = l.getDerniereModification();
        dto.articles = articles;
        dto.montantTotal = articles.stream()
                .map(ArticleCommande::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return dto;
    }

    private static String getEtatLibelle(String etat) {
        if (etat == null) return "Inconnu";
        switch (etat) {
            case "EC": return "En Cours";
            case "LI": return "Livré";
            case "AL": return "Ajourné";
            default: return etat;
        }
    }

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
    public String getLivreurNomComplet() { return livreurNomComplet; }
    public void setLivreurNomComplet(String livreurNomComplet) { this.livreurNomComplet = livreurNomComplet; }
    public String getClientNom() { return clientNom; }
    public void setClientNom(String clientNom) { this.clientNom = clientNom; }
    public String getClientPrenom() { return clientPrenom; }
    public void setClientPrenom(String clientPrenom) { this.clientPrenom = clientPrenom; }
    public String getClientNomComplet() { return clientNomComplet; }
    public void setClientNomComplet(String clientNomComplet) { this.clientNomComplet = clientNomComplet; }
    public String getClientTel() { return clientTel; }
    public void setClientTel(String clientTel) { this.clientTel = clientTel; }
    public String getClientAdresse() { return clientAdresse; }
    public void setClientAdresse(String clientAdresse) { this.clientAdresse = clientAdresse; }
    public String getClientVille() { return clientVille; }
    public void setClientVille(String clientVille) { this.clientVille = clientVille; }
    public String getClientCodePostal() { return clientCodePostal; }
    public void setClientCodePostal(String clientCodePostal) { this.clientCodePostal = clientCodePostal; }
    public java.math.BigDecimal getClientLatitude() { return clientLatitude; }
    public void setClientLatitude(java.math.BigDecimal clientLatitude) { this.clientLatitude = clientLatitude; }
    public java.math.BigDecimal getClientLongitude() { return clientLongitude; }
    public void setClientLongitude(java.math.BigDecimal clientLongitude) { this.clientLongitude = clientLongitude; }
    public String getEtatliv() { return etatliv; }
    public void setEtatliv(String etatliv) { this.etatliv = etatliv; }
    public String getEtatLibelle() { return etatLibelle; }
    public void setEtatLibelle(String etatLibelle) { this.etatLibelle = etatLibelle; }
    public String getModepay() { return modepay; }
    public void setModepay(String modepay) { this.modepay = modepay; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public String getRemarque() { return remarque; }
    public void setRemarque(String remarque) { this.remarque = remarque; }
    public String getCauseAjournement() { return causeAjournement; }
    public void setCauseAjournement(String causeAjournement) { this.causeAjournement = causeAjournement; }
    public LocalDateTime getDateTentativeRappel() { return dateTentativeRappel; }
    public void setDateTentativeRappel(LocalDateTime dateTentativeRappel) { this.dateTentativeRappel = dateTentativeRappel; }
    public LocalDateTime getDerniereModification() { return derniereModification; }
    public void setDerniereModification(LocalDateTime derniereModification) { this.derniereModification = derniereModification; }
    public List<ArticleCommande> getArticles() { return articles; }
    public void setArticles(List<ArticleCommande> articles) { this.articles = articles; }
    public BigDecimal getMontantTotal() { return montantTotal; }
    public void setMontantTotal(BigDecimal montantTotal) { this.montantTotal = montantTotal; }
}
