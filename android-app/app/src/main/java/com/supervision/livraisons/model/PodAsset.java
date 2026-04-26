package com.supervision.livraisons.model;

import com.google.gson.annotations.SerializedName;

public class PodAsset {
    @SerializedName("id")
    private long id;
    @SerializedName("assetType")
    private String assetType;
    @SerializedName("storageProvider")
    private String storageProvider = "s3";
    @SerializedName("storageKey")
    private String storageKey;
    @SerializedName("mimeType")
    private String mimeType;
    @SerializedName("sizeBytes")
    private Long sizeBytes;
    @SerializedName("sha256")
    private String sha256;

    public PodAsset() {}

    public PodAsset(String assetType, String storageKey, String mimeType, Long sizeBytes) {
        this.assetType = assetType;
        this.storageKey = storageKey;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
    }

    public String getAssetType() { return assetType; }
    public String getStorageKey() { return storageKey; }
}
