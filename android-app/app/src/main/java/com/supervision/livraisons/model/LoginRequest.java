package com.supervision.livraisons.model;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("login")
    private String login;

    @SerializedName("motPasse")
    private String motPasse;

    public LoginRequest(String login, String motPasse) {
        this.login = login;
        this.motPasse = motPasse;
    }

    public String getLogin() { return login; }
    public String getMotPasse() { return motPasse; }
}
