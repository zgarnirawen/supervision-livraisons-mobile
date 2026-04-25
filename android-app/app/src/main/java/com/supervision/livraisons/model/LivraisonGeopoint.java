package com.supervision.livraisons.model;

import com.google.gson.annotations.SerializedName;

public class LivraisonGeopoint {
    @SerializedName("latitude")
    private double latitude;
    @SerializedName("longitude")
    private double longitude;
    @SerializedName("accuracyM")
    private Double accuracyM;
    @SerializedName("source")
    private String source = "mobile";
    @SerializedName("capturedAt")
    private String capturedAt;

    public LivraisonGeopoint() {}

    public LivraisonGeopoint(double latitude, double longitude, Double accuracyM) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracyM = accuracyM;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public Double getAccuracyM() { return accuracyM; }
    public String getCapturedAt() { return capturedAt; }
}
