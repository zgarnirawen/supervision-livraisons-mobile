package com.supervision.livraisons.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.supervision.livraisons.R;
import com.supervision.livraisons.api.ApiClient;
import com.supervision.livraisons.databinding.ActivityLivraisonDetailBinding;
import com.supervision.livraisons.model.ArticleCommande;
import com.supervision.livraisons.model.LivraisonDetail;
import com.supervision.livraisons.ui.livreur.ChangerStatutActivity;
import com.supervision.livraisons.utils.SessionManager;
import com.supervision.livraisons.utils.UiUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LivraisonDetailActivity extends AppCompatActivity {

    private ActivityLivraisonDetailBinding binding;
    private SessionManager sessionManager;
    private int nocde;
    private boolean isLivreur;
    private LivraisonDetail currentLivraison;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLivraisonDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        ApiClient.init(sessionManager);

        nocde = getIntent().getIntExtra("nocde", -1);
        isLivreur = getIntent().getBooleanExtra("isLivreur", true);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Livraison N°" + nocde);
        }

        setupButtons();
        loadDetail();
    }

    private void setupButtons() {
        if (isLivreur) {
            UiUtils.setVisible(binding.btnChangerStatut, true);
            UiUtils.setVisible(binding.btnRappel, false);

            binding.btnChangerStatut.setOnClickListener(v -> {
                if (currentLivraison != null && "EC".equals(currentLivraison.getEtatliv())) {
                    Intent intent = new Intent(this, ChangerStatutActivity.class);
                    intent.putExtra("nocde", nocde);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Ce statut ne peut plus être modifié", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Contrôleur
            UiUtils.setVisible(binding.btnChangerStatut, false);
            UiUtils.setVisible(binding.btnRappel, true);

            binding.btnRappel.setOnClickListener(v -> {
                if (currentLivraison != null && "AL".equals(currentLivraison.getEtatliv())) {
                    enregistrerRappel();
                } else {
                    Toast.makeText(this,
                            "Le rappel s'applique uniquement aux livraisons ajournées",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadDetail() {
        UiUtils.setVisible(binding.progressBar, true);
        UiUtils.setVisible(binding.contentLayout, false);

        ApiClient.getApiService().getDetail(nocde).enqueue(new Callback<LivraisonDetail>() {
            @Override
            public void onResponse(Call<LivraisonDetail> call, Response<LivraisonDetail> response) {
                UiUtils.setVisible(binding.progressBar, false);
                if (response.isSuccessful() && response.body() != null) {
                    currentLivraison = response.body();
                    populateUI(currentLivraison);
                    UiUtils.setVisible(binding.contentLayout, true);
                }
            }

            @Override
            public void onFailure(Call<LivraisonDetail> call, Throwable t) {
                UiUtils.setVisible(binding.progressBar, false);
                Toast.makeText(LivraisonDetailActivity.this,
                        getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUI(LivraisonDetail l) {
        // Statut
        UiUtils.applyStatutStyle(this, binding.tvStatut, l.getEtatliv());

        // Client
        binding.tvClientNom.setText(l.getClientNomComplet());
        binding.tvClientTel.setText("📞 " + l.getClientTel());
        binding.tvClientAdresse.setText("📍 " + l.getClientAdresse() + ", " + l.getClientVille());

        // Livreur
        binding.tvLivreurNom.setText(l.getLivreurNomComplet());
        binding.tvLivreurTel.setText("📞 " + l.getLivreurTel());

        // Livraison
        binding.tvModePay.setText(UiUtils.formatModePay(l.getModepay()));
        binding.tvDate.setText("📅 " + UiUtils.formatDate(l.getDateliv() != null ? l.getDateliv().toString() : ""));

        // Remarque
        if (l.getRemarque() != null && !l.getRemarque().isEmpty()) {
            binding.tvRemarque.setText(l.getRemarque());
            UiUtils.setVisible(binding.cardRemarque, true);
        } else {
            UiUtils.setVisible(binding.cardRemarque, false);
        }

        // Cause ajournement
        if (l.getCauseAjournement() != null && !l.getCauseAjournement().isEmpty()) {
            binding.tvCauseAjournement.setText(l.getCauseAjournement());
            UiUtils.setVisible(binding.cardAjournement, true);
        } else {
            UiUtils.setVisible(binding.cardAjournement, false);
        }

        // Date rappel
        if (l.getDateTentativeRappel() != null) {
            binding.tvDateRappel.setText(UiUtils.formatDate(l.getDateTentativeRappel().toString()));
            UiUtils.setVisible(binding.cardRappel, true);
        }

        // Articles
        if (l.getArticles() != null && !l.getArticles().isEmpty()) {
            ArticlesAdapter articlesAdapter = new ArticlesAdapter(l.getArticles());
            binding.recyclerArticles.setLayoutManager(new LinearLayoutManager(this));
            binding.recyclerArticles.setAdapter(articlesAdapter);
            binding.recyclerArticles.setNestedScrollingEnabled(false);
            binding.tvMontantTotal.setText(
                    String.format("Total : %.2f DT", l.getMontantTotal()));
        }

        // Mise à jour du bouton selon statut
        if (isLivreur) {
            boolean canChange = "EC".equals(l.getEtatliv());
            binding.btnChangerStatut.setEnabled(canChange);
            binding.btnChangerStatut.setAlpha(canChange ? 1.0f : 0.5f);
        } else {
            boolean canRappel = "AL".equals(l.getEtatliv());
            binding.btnRappel.setEnabled(canRappel);
            binding.btnRappel.setAlpha(canRappel ? 1.0f : 0.5f);
        }
    }

    private void enregistrerRappel() {
        binding.btnRappel.setEnabled(false);

        java.util.Map<String, String> body = new java.util.HashMap<>();
        // Utiliser l'heure actuelle
        body.put("dateTentativeRappel", java.time.LocalDateTime.now().toString());

        ApiClient.getApiService().enregistrerRappel(nocde, body).enqueue(
                new Callback<com.supervision.livraisons.model.LivraisonMobile>() {
            @Override
            public void onResponse(Call<com.supervision.livraisons.model.LivraisonMobile> call,
                                   Response<com.supervision.livraisons.model.LivraisonMobile> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(LivraisonDetailActivity.this,
                            getString(R.string.success_rappel_saved), Toast.LENGTH_SHORT).show();
                    loadDetail(); // Rafraîchir
                } else {
                    binding.btnRappel.setEnabled(true);
                    Toast.makeText(LivraisonDetailActivity.this,
                            "Erreur lors de l'enregistrement", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.supervision.livraisons.model.LivraisonMobile> call, Throwable t) {
                binding.btnRappel.setEnabled(true);
                Toast.makeText(LivraisonDetailActivity.this,
                        getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
