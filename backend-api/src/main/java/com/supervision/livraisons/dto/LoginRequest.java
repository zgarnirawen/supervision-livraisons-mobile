package com.supervision.livraisons.dto;

// DTO de réponse login
public class LoginRequest {
    private String login;
    private String motPasse;

    public LoginRequest() {}
    public LoginRequest(String login, String motPasse) {
        this.login = login;
        this.motPasse = motPasse;
    }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getMotPasse() { return motPasse; }
    public void setMotPasse(String motPasse) { this.motPasse = motPasse; }
}
