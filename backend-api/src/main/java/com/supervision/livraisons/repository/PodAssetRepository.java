package com.supervision.livraisons.repository;

import com.supervision.livraisons.model.PodAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PodAssetRepository extends JpaRepository<PodAsset, Long> {
    List<PodAsset> findByNocdeOrderByCapturedAtDesc(Integer nocde);
}
