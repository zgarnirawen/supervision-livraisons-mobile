package com.supervision.livraisons.model;

import com.google.gson.annotations.SerializedName;

public class HistoriqueLivraison {
    @SerializedName("id")
    private int id;

    @SerializedName("nocde")
    private int nocde;

    @SerializedName("ancienStatut")
    private String ancienStatut;

    @SerializedName("nouveauStatut")
    private String nouveauStatut;

    @SerializedName("modifiePar")
    private int modifiePar;

    @SerializedName("dateModification")
    private String dateModification;

    @SerializedName("remarque")
    private String remarque;

    public int getId() { return id; }
    public int getNocde() { return nocde; }
    public String getAncienStatut() { return ancienStatut; }
    public String getNouveauStatut() { return nouveauStatut; }
    public int getModifiePar() { return modifiePar; }
    public String getDateModification() { return dateModification; }
    public String getRemarque() { return remarque; }
}
