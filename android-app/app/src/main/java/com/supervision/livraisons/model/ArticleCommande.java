package com.supervision.livraisons.model;

import com.google.gson.annotations.SerializedName;

public class ArticleCommande {
    @SerializedName("id")
    private int id;

    @SerializedName("refart")
    private String refart;

    @SerializedName("designation")
    private String designation;

    @SerializedName("quantite")
    private int quantite;

    @SerializedName("prixUnitaire")
    private double prixUnitaire;

    @SerializedName("montantTotal")
    private double montantTotal;

    public int getId() { return id; }
    public String getRefart() { return refart; }
    public String getDesignation() { return designation; }
    public int getQuantite() { return quantite; }
    public double getPrixUnitaire() { return prixUnitaire; }
    public double getMontantTotal() { return montantTotal; }
}
