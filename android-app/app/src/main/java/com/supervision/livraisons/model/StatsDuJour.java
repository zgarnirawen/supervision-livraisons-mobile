package com.supervision.livraisons.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StatsDuJour {
    @SerializedName("totalLivraisons")
    private long totalLivraisons;

    @SerializedName("livrees")
    private long livrees;

    @SerializedName("enCours")
    private long enCours;

    @SerializedName("ajournees")
    private long ajournees;

    @SerializedName("tauxSucces")
    private double tauxSucces;

    @SerializedName("parLivreur")
    private List<StatsLivreur> parLivreur;

    public long getTotalLivraisons() { return totalLivraisons; }
    public long getLivrees() { return livrees; }
    public long getEnCours() { return enCours; }
    public long getAjournees() { return ajournees; }
    public double getTauxSucces() { return tauxSucces; }
    public List<StatsLivreur> getParLivreur() { return parLivreur; }

    public static class StatsLivreur {
        @SerializedName("livreurId")
        private int livreurId;

        @SerializedName("livreurNomComplet")
        private String livreurNomComplet;

        @SerializedName("total")
        private long total;

        @SerializedName("livrees")
        private long livrees;

        @SerializedName("enCours")
        private long enCours;

        @SerializedName("ajournees")
        private long ajournees;

        public int getLivreurId() { return livreurId; }
        public String getLivreurNomComplet() { return livreurNomComplet; }
        public long getTotal() { return total; }
        public long getLivrees() { return livrees; }
        public long getEnCours() { return enCours; }
        public long getAjournees() { return ajournees; }
    }
}
