package com.supervision.livraisons.model;

import com.google.gson.annotations.SerializedName;

public class LivreurLocation {
    @SerializedName("livreurId")
    private Integer livreurId;
    @SerializedName("livreurNom")
    private String livreurNom;
    @SerializedName("latitude")
    private Double latitude;
    @SerializedName("longitude")
    private Double longitude;
    @SerializedName("capturedAt")
    private String capturedAt;

    public Integer getLivreurId() { return livreurId; }
    public String getLivreurNom() { return livreurNom; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getCapturedAt() { return capturedAt; }
}
