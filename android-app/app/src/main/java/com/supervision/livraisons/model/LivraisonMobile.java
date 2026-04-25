package com.supervision.livraisons.model;

import com.google.gson.annotations.SerializedName;

public class LivraisonMobile {
    @SerializedName("nocde")
    private int nocde;

    @SerializedName("dateliv")
    private String dateliv;

    @SerializedName("livreurId")
    private int livreurId;

    @SerializedName("livreurNom")
    private String livreurNom;

    @SerializedName("livreurPrenom")
    private String livreurPrenom;

    @SerializedName("livreurTel")
    private String livreurTel;

    @SerializedName("clientNom")
    private String clientNom;

    @SerializedName("clientPrenom")
    private String clientPrenom;

    @SerializedName("clientTel")
    private String clientTel;

    @SerializedName("clientAdresse")
    private String clientAdresse;

    @SerializedName("clientVille")
    private String clientVille;

    @SerializedName("clientCodePostal")
    private String clientCodePostal;

    @SerializedName("etatliv")
    private String etatliv;

    @SerializedName("modepay")
    private String modepay;

    @SerializedName("remarque")
    private String remarque;

    @SerializedName("causeAjournement")
    private String causeAjournement;

    @SerializedName("dateTentativeRappel")
    private String dateTentativeRappel;

    @SerializedName("derniereModification")
    private String derniereModification;

    // Getters
    public int getNocde() { return nocde; }
    public String getDateliv() { return dateliv; }
    public int getLivreurId() { return livreurId; }
    public String getLivreurNom() { return livreurNom; }
    public String getLivreurPrenom() { return livreurPrenom; }
    public String getLivreurTel() { return livreurTel; }
    public String getLivreurNomComplet() { return livreurNom + " " + livreurPrenom; }
    public String getClientNom() { return clientNom; }
    public String getClientPrenom() { return clientPrenom; }
    public String getClientTel() { return clientTel; }
    public String getClientAdresse() { return clientAdresse; }
    public String getClientVille() { return clientVille; }
    public String getClientCodePostal() { return clientCodePostal; }
    public String getEtatliv() { return etatliv; }
    public String getModepay() { return modepay; }
    public String getRemarque() { return remarque; }
    public String getCauseAjournement() { return causeAjournement; }
    public String getDateTentativeRappel() { return dateTentativeRappel; }
    public String getDerniereModification() { return derniereModification; }

    public String getClientNomComplet() {
        if (clientPrenom != null && !clientPrenom.isEmpty()) {
            return clientNom + " " + clientPrenom;
        }
        return clientNom;
    }

    public String getEtatLibelle() {
        if (etatliv == null) return "Inconnu";
        switch (etatliv) {
            case "EC": return "En Cours";
            case "LI": return "Livré";
            case "AL": return "Ajourné";
            default: return etatliv;
        }
    }
}
