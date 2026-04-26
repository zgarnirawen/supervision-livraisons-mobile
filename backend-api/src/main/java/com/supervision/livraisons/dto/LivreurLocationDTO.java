package com.supervision.livraisons.dto;

import java.time.LocalDateTime;

public class LivreurLocationDTO {
    private Integer livreurId;
    private String livreurNom;
    private Double latitude;
    private Double longitude;
    private LocalDateTime capturedAt;

    public LivreurLocationDTO() {}

    public LivreurLocationDTO(Integer livreurId, String livreurNom, Double latitude, Double longitude, LocalDateTime capturedAt) {
        this.livreurId = livreurId;
        this.livreurNom = livreurNom;
        this.latitude = latitude;
        this.longitude = longitude;
        this.capturedAt = capturedAt;
    }

    // Getters and Setters
    public Integer getLivreurId() { return livreurId; }
    public void setLivreurId(Integer livreurId) { this.livreurId = livreurId; }
    public String getLivreurNom() { return livreurNom; }
    public void setLivreurNom(String livreurNom) { this.livreurNom = livreurNom; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public LocalDateTime getCapturedAt() { return capturedAt; }
    public void setCapturedAt(LocalDateTime capturedAt) { this.capturedAt = capturedAt; }
}
