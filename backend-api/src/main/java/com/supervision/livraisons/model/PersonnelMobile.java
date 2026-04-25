package com.supervision.livraisons.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "personnel_mobile")
public class PersonnelMobile {

    @Id
    @Column(name = "idpers")
    private Integer idpers;

    @Column(name = "nompers", nullable = false, length = 30)
    private String nompers;

    @Column(name = "prenompers", nullable = false, length = 30)
    private String prenompers;

    @Column(name = "telpers", nullable = false, length = 8)
    private String telpers;

    @Column(name = "login", nullable = false, unique = true, length = 30)
    private String login;

    @Column(name = "mot_passe", nullable = false)
    private String motPasse;

    @Column(name = "codeposte", nullable = false, length = 10)
    private String codeposte;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "actif")
    private Boolean actif = true;

    // Constructors
    public PersonnelMobile() {}

    // Getters & Setters
    public Integer getIdpers() { return idpers; }
    public void setIdpers(Integer idpers) { this.idpers = idpers; }

    public String getNompers() { return nompers; }
    public void setNompers(String nompers) { this.nompers = nompers; }

    public String getPrenompers() { return prenompers; }
    public void setPrenompers(String prenompers) { this.prenompers = prenompers; }

    public String getTelpers() { return telpers; }
    public void setTelpers(String telpers) { this.telpers = telpers; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getMotPasse() { return motPasse; }
    public void setMotPasse(String motPasse) { this.motPasse = motPasse; }

    public String getCodeposte() { return codeposte; }
    public void setCodeposte(String codeposte) { this.codeposte = codeposte; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public String getNomComplet() {
        return nompers + " " + prenompers;
    }

    public boolean isLivreur() {
        return "P001".equals(codeposte);
    }

    public boolean isControleur() {
        return "P003".equals(codeposte);
    }
}
