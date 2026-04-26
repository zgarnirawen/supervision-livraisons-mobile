package com.supervision.livraisons.ui.common;

import android.content.Intent;
import android.net.Uri;
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
import com.supervision.livraisons.model.PodAsset;
import com.bumptech.glide.Glide;

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
        String clientName = l.getClientNomComplet();
        if (l.getClientId() != null) {
            clientName += " (#" + l.getClientId() + ")";
        }
        binding.tvClientNom.setText(clientName);
        binding.tvClientTel.setText(l.getClientTel());
        binding.tvClientAdresse.setText("📍 " + l.getClientAdresse() + ", " + l.getClientVille());

        if (l.getClientCin() != null && !l.getClientCin().isEmpty()) {
            binding.tvClientCin.setText("🪪 CIN: " + l.getClientCin());
            UiUtils.setVisible(binding.tvClientCin, true);
        } else {
            UiUtils.setVisible(binding.tvClientCin, false);
        }

        if (l.getClientEmail() != null && !l.getClientEmail().isEmpty()) {
            binding.tvClientEmail.setText(l.getClientEmail());
            UiUtils.setVisible(binding.tvClientEmail, true);
            UiUtils.setVisible(binding.btnEmailClient, true);
        } else {
            UiUtils.setVisible(binding.tvClientEmail, false);
            UiUtils.setVisible(binding.btnEmailClient, false);
        }

        // Clic pour appeler (demande permission) ou chatter
        binding.tvClientTel.setOnClickListener(v -> appelerNumero(l.getClientTel()));
        binding.btnSmsClient.setOnClickListener(v -> envoyerSMS(l.getClientTel()));
        binding.btnEmailClient.setOnClickListener(v -> envoyerEmail(l.getClientEmail()));
        binding.tvClientAdresse.setOnClickListener(v -> ouvrirCarte());

        // Livreur
        binding.tvLivreurNom.setText(l.getLivreurNomComplet());
        binding.tvLivreurTel.setText(l.getLivreurTel());

        binding.tvLivreurTel.setOnClickListener(v -> appelerNumero(l.getLivreurTel()));
        binding.btnChatLivreur.setOnClickListener(v -> ouvrirChat());

        // Position Livreur (Contrôleur only)
        if (!isLivreur && l.getLivreurId() > 0) {
            loadLivreurLocation(l.getLivreurId());
        }

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

        // Preuves (Photo / Signature)
        if (l.getProofs() != null && !l.getProofs().isEmpty()) {
            UiUtils.setVisible(binding.cardProofs, true);
            for (PodAsset proof : l.getProofs()) {
                if ("photo".equals(proof.getAssetType())) {
                    loadProofImage(proof.getStorageKey(), binding.imgProofPhoto);
                } else if ("signature".equals(proof.getAssetType())) {
                    loadProofImage(proof.getStorageKey(), binding.imgProofSignature);
                }
            }
        } else {
            UiUtils.setVisible(binding.cardProofs, false);
        }

        // Mise à jour du bouton selon statut
        if (isLivreur) {
            boolean canChange = "EC".equals(l.getEtatliv());
            binding.btnChangerStatut.setEnabled(canChange);
            binding.btnChangerStatut.setAlpha(canChange ? 1.0f : 0.5f);
            binding.btnChangerStatut.setText(canChange
                    ? "✏️ Choisir un nouveau statut"
                    : "✅ Livraison finalisée");
        } else {
            boolean canRappel = "AL".equals(l.getEtatliv());
            binding.btnRappel.setEnabled(canRappel);
            binding.btnRappel.setAlpha(canRappel ? 1.0f : 0.5f);
            binding.btnRappel.setText(canRappel
                    ? "📞 Enregistrer une tentative de rappel"
                    : "📞 Rappel non applicable");
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

    private String numeroToCall = null;
    private static final int REQ_CALL_PHONE = 101;

    private void appelerNumero(String tel) {
        if (tel == null || tel.isEmpty()) {
            Toast.makeText(this, "Numéro indisponible", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tel));
        startActivity(intent);
    }



    private void envoyerSMS(String tel) {
        if (tel == null || tel.isEmpty()) {
            Toast.makeText(this, "Numéro indisponible", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + tel));
        startActivity(intent);
    }

    private void envoyerEmail(String email) {
        if (email == null || email.isEmpty()) return;
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Livraison N°" + nocde);
        startActivity(Intent.createChooser(intent, "Envoyer un email..."));
    }

    private void ouvrirChat() {
        Intent intent = new Intent(this, LivraisonChatActivity.class);
        intent.putExtra("nocde", nocde);
        startActivity(intent);
    }

    private void ouvrirCarte() {
        if (currentLivraison == null) return;
        String query = currentLivraison.getClientAdresse() + ", " + currentLivraison.getClientVille();
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(query));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(query))));
        }
    }

    private void loadProofImage(String storageKey, android.widget.ImageView imageView) {
        if (storageKey == null) return;
        
        // Simulation: Si c'est une URL S3 fictive, on affiche une image par défaut/mockup
        if (storageKey.startsWith("s3://")) {
            if (storageKey.contains("photo")) {
                Glide.with(this)
                    .load("https://images.unsplash.com/photo-1586769852836-bc069f19e1b6?q=80&w=400") // Mock delivery photo
                    .placeholder(R.color.background)
                    .into(imageView);
            } else {
                Glide.with(this)
                    .load("https://www.pngarts.com/files/10/Signature-PNG-Transparent-Image.png") // Mock signature
                    .placeholder(R.color.background)
                    .into(imageView);
            }
        } else {
            Glide.with(this).load(storageKey).into(imageView);
        }
    }

    private void loadLivreurLocation(Integer livreurId) {
        ApiClient.getApiService().getLivreurLocation(livreurId).enqueue(new Callback<com.supervision.livraisons.model.LivreurLocation>() {
            @Override
            public void onResponse(Call<com.supervision.livraisons.model.LivreurLocation> call, Response<com.supervision.livraisons.model.LivreurLocation> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.supervision.livraisons.model.LivreurLocation location = response.body();
                    if (location.getLatitude() != null && location.getLongitude() != null) {
                        String locationText = String.format("%.5f, %.5f", location.getLatitude(), location.getLongitude());
                        binding.tvLivreurLocation.setText(locationText);
                        UiUtils.setVisible(binding.cardLivreurLocation, true);

                        binding.tvLivreurLocation.setOnClickListener(v -> {
                            Uri gmmIntentUri = Uri.parse("geo:" + location.getLatitude() + "," + location.getLongitude());
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                                startActivity(mapIntent);
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<com.supervision.livraisons.model.LivreurLocation> call, Throwable t) {
                UiUtils.setVisible(binding.cardLivreurLocation, false);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
