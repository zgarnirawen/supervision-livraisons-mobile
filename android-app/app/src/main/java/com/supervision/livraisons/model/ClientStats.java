package com.supervision.livraisons.model;

import java.io.Serializable;

public class ClientStats implements Serializable {
    private String nom;
    private String prenom;
    private String tel;
    private String adresse;
    private String ville;
    private Double latitude;
    private Double longitude;
    private String categorie;
    
    private int totalLivraisons;
    private int livrees;
    private int enCours;
    private int ajournees;

    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getTel() { return tel; }
    public String getAdresse() { return adresse; }
    public String getVille() { return ville; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getCategorie() { return categorie; }
    
    public int getTotalLivraisons() { return totalLivraisons; }
    public int getLivrees() { return livrees; }
    public int getEnCours() { return enCours; }
    public int getAjournees() { return ajournees; }
}
