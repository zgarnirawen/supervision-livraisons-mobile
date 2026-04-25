package com.supervision.livraisons.model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "articles_commande")
public class ArticleCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nocde", nullable = false)
    private Integer nocde;

    @Column(name = "refart", length = 4)
    private String refart;

    @Column(name = "designation", length = 50)
    private String designation;

    @Column(name = "quantite")
    private Integer quantite;

    @Column(name = "prix_unitaire", precision = 8, scale = 2)
    private BigDecimal prixUnitaire;

    // Constructors
    public ArticleCommande() {}

    // Getters & Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getNocde() { return nocde; }
    public void setNocde(Integer nocde) { this.nocde = nocde; }

    public String getRefart() { return refart; }
    public void setRefart(String refart) { this.refart = refart; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }

    public BigDecimal getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(BigDecimal prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    public BigDecimal getMontantTotal() {
        if (prixUnitaire != null && quantite != null) {
            return prixUnitaire.multiply(BigDecimal.valueOf(quantite));
        }
        return BigDecimal.ZERO;
    }
}
