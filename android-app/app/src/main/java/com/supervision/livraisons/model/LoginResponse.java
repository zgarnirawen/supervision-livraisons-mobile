package com.supervision.livraisons.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("token")
    private String token;

    @SerializedName("tokenType")
    private String tokenType;

    @SerializedName("idpers")
    private int idpers;

    @SerializedName("nomComplet")
    private String nomComplet;

    @SerializedName("login")
    private String login;

    @SerializedName("codeposte")
    private String codeposte;

    @SerializedName("role")
    private String role; // "LIVREUR" or "CONTROLEUR"

    public String getToken() { return token; }
    public int getIdpers() { return idpers; }
    public String getNomComplet() { return nomComplet; }
    public String getLogin() { return login; }
    public String getCodeposte() { return codeposte; }
    public String getRole() { return role; }

    public boolean isLivreur() { return "LIVREUR".equals(role); }
    public boolean isControleur() { return "CONTROLEUR".equals(role); }
}
