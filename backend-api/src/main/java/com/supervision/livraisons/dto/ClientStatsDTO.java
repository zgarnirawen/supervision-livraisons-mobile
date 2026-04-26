package com.supervision.livraisons.dto;

public class ClientStatsDTO {
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

    // Getters and Setters
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public int getTotalLivraisons() { return totalLivraisons; }
    public void setTotalLivraisons(int totalLivraisons) { this.totalLivraisons = totalLivraisons; }

    public int getLivrees() { return livrees; }
    public void setLivrees(int livrees) { this.livrees = livrees; }

    public int getEnCours() { return enCours; }
    public void setEnCours(int enCours) { this.enCours = enCours; }

    public int getAjournees() { return ajournees; }
    public void setAjournees(int ajournees) { this.ajournees = ajournees; }
}
