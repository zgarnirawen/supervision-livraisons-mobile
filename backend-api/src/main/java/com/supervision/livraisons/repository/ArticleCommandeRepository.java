package com.supervision.livraisons.repository;

import com.supervision.livraisons.model.ArticleCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleCommandeRepository extends JpaRepository<ArticleCommande, Integer> {
    List<ArticleCommande> findByNocde(Integer nocde);
    void deleteByNocde(Integer nocde);
}
