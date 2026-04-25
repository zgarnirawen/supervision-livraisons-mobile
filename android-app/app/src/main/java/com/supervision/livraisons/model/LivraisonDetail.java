package com.supervision.livraisons.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LivraisonDetail extends LivraisonMobile {

    @SerializedName("etatLibelle")
    private String etatLibelle;

    @SerializedName("articles")
    private List<ArticleCommande> articles;

    @SerializedName("montantTotal")
    private double montantTotal;

    public String getEtatLibelle() { return etatLibelle; }
    public List<ArticleCommande> getArticles() { return articles; }
    public double getMontantTotal() { return montantTotal; }
}
