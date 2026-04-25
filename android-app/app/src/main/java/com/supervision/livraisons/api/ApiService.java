package com.supervision.livraisons.api;

import com.supervision.livraisons.model.LoginRequest;
import com.supervision.livraisons.model.LoginResponse;
import com.supervision.livraisons.model.LivraisonDetail;
import com.supervision.livraisons.model.LivraisonMobile;
import com.supervision.livraisons.model.StatsDuJour;
import com.supervision.livraisons.model.HistoriqueLivraison;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // ── Authentification ──────────────────────────────────────────────────
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // ── Livreur : ses livraisons ──────────────────────────────────────────
    @GET("api/livraisons")
    Call<List<LivraisonMobile>> getMesLivraisons();

    // ── Contrôleur : toutes les livraisons ───────────────────────────────
    @GET("api/livraisons/all")
    Call<List<LivraisonMobile>> getAllLivraisons(
            @Query("etatliv") String etatliv,
            @Query("ville") String ville,
            @Query("livreurId") Integer livreurId
    );

    // ── Détail livraison ──────────────────────────────────────────────────
    @GET("api/livraisons/{nocde}")
    Call<LivraisonDetail> getDetail(@Path("nocde") int nocde);

    // ── Changer statut (Livreur) ──────────────────────────────────────────
    @PUT("api/livraisons/{nocde}/statut")
    Call<LivraisonMobile> changerStatut(
            @Path("nocde") int nocde,
            @Body Map<String, String> body
    );

    // ── Ajouter remarque (Livreur) ────────────────────────────────────────
    @PUT("api/livraisons/{nocde}/remarque")
    Call<LivraisonMobile> ajouterRemarque(
            @Path("nocde") int nocde,
            @Body Map<String, String> body
    );

    // ── Enregistrer rappel (Contrôleur) ──────────────────────────────────
    @PUT("api/livraisons/{nocde}/rappel")
    Call<LivraisonMobile> enregistrerRappel(
            @Path("nocde") int nocde,
            @Body Map<String, String> body
    );

    // ── Stats du jour (Contrôleur) ────────────────────────────────────────
    @GET("api/livraisons/stats")
    Call<StatsDuJour> getStats();

    // ── Historique ────────────────────────────────────────────────────────
    @GET("api/livraisons/{nocde}/historique")
    Call<List<HistoriqueLivraison>> getHistorique(@Path("nocde") int nocde);
}
