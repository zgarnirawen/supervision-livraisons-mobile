package com.supervision.livraisons.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "livraison_geopoints")
public class LivraisonGeopoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nocde", nullable = false)
    private Integer nocde;

    @Column(name = "livreur_id", nullable = false)
    private Integer livreurId;

    @Column(name = "captured_at")
    private LocalDateTime capturedAt;

    @Column(name = "latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "accuracy_m", precision = 6, scale = 2)
    private BigDecimal accuracyM;

    @Column(name = "speed_mps", precision = 6, scale = 2)
    private BigDecimal speedMps;

    @Column(name = "source", length = 16)
    private String source = "mobile";

    @PrePersist
    protected void onCreate() {
        capturedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getNocde() { return nocde; }
    public void setNocde(Integer nocde) { this.nocde = nocde; }
    public Integer getLivreurId() { return livreurId; }
    public void setLivreurId(Integer livreurId) { this.livreurId = livreurId; }
    public LocalDateTime getCapturedAt() { return capturedAt; }
    public void setCapturedAt(LocalDateTime capturedAt) { this.capturedAt = capturedAt; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getAccuracyM() { return accuracyM; }
    public void setAccuracyM(BigDecimal accuracyM) { this.accuracyM = accuracyM; }
    public BigDecimal getSpeedMps() { return speedMps; }
    public void setSpeedMps(BigDecimal speedMps) { this.speedMps = speedMps; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
