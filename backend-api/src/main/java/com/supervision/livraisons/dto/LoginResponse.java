package com.supervision.livraisons.dto;

public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private Integer idpers;
    private String nomComplet;
    private String login;
    private String codeposte;
    private String role; // LIVREUR ou CONTROLEUR

    public LoginResponse() {}

    public LoginResponse(String token, Integer idpers, String nomComplet,
                         String login, String codeposte) {
        this.token = token;
        this.idpers = idpers;
        this.nomComplet = nomComplet;
        this.login = login;
        this.codeposte = codeposte;
        this.role = "P001".equals(codeposte) ? "LIVREUR" : "CONTROLEUR";
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public Integer getIdpers() { return idpers; }
    public void setIdpers(Integer idpers) { this.idpers = idpers; }
    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getCodeposte() { return codeposte; }
    public void setCodeposte(String codeposte) { this.codeposte = codeposte; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
