package com.supervision.livraisons.ui.livreur;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.supervision.livraisons.R;
import com.supervision.livraisons.api.ApiClient;
import com.supervision.livraisons.databinding.ActivityChangerStatutBinding;
import com.supervision.livraisons.model.ChatMessage;
import com.supervision.livraisons.model.LivraisonMobile;
import com.supervision.livraisons.model.PodAsset;
import com.supervision.livraisons.utils.SessionManager;
import com.supervision.livraisons.utils.UiUtils;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangerStatutActivity extends AppCompatActivity {
    private static final int REQ_CAPTURE_PHOTO = 7001;
    private static final int PERMISSION_CAMERA = 100;

    private ActivityChangerStatutBinding binding;
    private SessionManager sessionManager;
    private int nocde;
    private String clientNom;
    private String selectedStatut = null;
    private Bitmap photoBitmap = null;
    private String selectedCause = "";
    private final String[] predefinedCauses = {
        "Client absent",
        "Adresse introuvable",
        "Colis endommagé",
        "Client a refusé la livraison",
        "Problème de paiement",
        "Autre"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangerStatutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        nocde = getIntent().getIntExtra("nocde", -1);
        clientNom = getIntent().getStringExtra("clientNom");
        sessionManager = new SessionManager(this);
        ApiClient.init(sessionManager);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Livraison N°" + nocde);
        }

        setupCausesDropdown();
        setupButtonListeners();
    }

    private void setupCausesDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, predefinedCauses);
        binding.etCauseAjournement.setAdapter(adapter);
        binding.etCauseAjournement.setOnItemClickListener((parent, view, position, id) -> {
            selectedCause = predefinedCauses[position];
            boolean isOther = "Autre".equals(selectedCause);
            UiUtils.setVisible(binding.tilCauseCustom, isOther);
            updateConfirmButtonState();
        });

        binding.etCauseCustom.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                updateConfirmButtonState();
            }
        });
    }

    private void setupButtonListeners() {
        // Bouton VERT - Livré (LI)
        binding.btnLivre.setOnClickListener(v -> {
            selectedStatut = "LI";
            photoBitmap = null;
            selectedCause = "";
            binding.etCauseAjournement.setText("");
            binding.etCauseCustom.setText("");
            UiUtils.setVisible(binding.cardFluxLivre, true);
            UiUtils.setVisible(binding.cardFluxAjourne, false);
            updateConfirmButtonState();
            resetPhotoUI();
        });

        // Bouton ROUGE - Ajourné (AL)
        binding.btnAjourne.setOnClickListener(v -> {
            selectedStatut = "AL";
            photoBitmap = null;
            selectedCause = "";
            binding.etCauseAjournement.setText("");
            binding.etCauseCustom.setText("");
            UiUtils.setVisible(binding.cardFluxAjourne, true);
            UiUtils.setVisible(binding.cardFluxLivre, false);
            updateConfirmButtonState();
        });

        // Bouton Prendre une Photo
        binding.btnCapturePhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.CAMERA}, PERMISSION_CAMERA);
            } else {
                launchCamera();
            }
        });

        // Bouton Effacer la Signature
        binding.btnClearSignature.setOnClickListener(v -> {
            binding.signatureView.clear();
            Toast.makeText(this, "Signature effacée", Toast.LENGTH_SHORT).show();
        });

        // Bouton CONFIRMER
        binding.btnConfirmer.setOnClickListener(v -> {
            if ("LI".equals(selectedStatut)) {
                confirmLivre();
            } else if ("AL".equals(selectedStatut)) {
                confirmAjourne();
            }
        });
    }

    private void resetPhotoUI() {
        binding.imgPreuvePhoto.setImageBitmap(null);
        UiUtils.setVisible(binding.imgPreuvePhoto, false);
        binding.tvPhotoStatus.setText("Aucune photo prise");
        binding.tvPhotoStatus.setTextColor(getResources().getColor(R.color.warning, getTheme()));
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQ_CAPTURE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CAPTURE_PHOTO && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null && extras.get("data") != null) {
                photoBitmap = (Bitmap) extras.get("data");
                binding.imgPreuvePhoto.setImageBitmap(photoBitmap);
                UiUtils.setVisible(binding.imgPreuvePhoto, true);
                binding.tvPhotoStatus.setText("✓ Photo capturée");
                binding.tvPhotoStatus.setTextColor(getResources().getColor(R.color.success, getTheme()));
                updateConfirmButtonState();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(this, "Permission caméra refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateConfirmButtonState() {
        boolean canConfirm = false;
        if ("LI".equals(selectedStatut)) {
            canConfirm = photoBitmap != null;
        } else if ("AL".equals(selectedStatut)) {
            String cause = binding.etCauseAjournement.getText().toString().trim();
            if ("Autre".equals(cause)) {
                String customCause = binding.etCauseCustom.getText().toString().trim();
                canConfirm = !customCause.isEmpty();
            } else {
                canConfirm = !cause.isEmpty();
            }
        }

        binding.btnConfirmer.setEnabled(canConfirm);
        binding.btnConfirmer.setAlpha(canConfirm ? 1.0f : 0.5f);
    }

    private void confirmLivre() {
        if (photoBitmap == null) {
            Toast.makeText(this, "Photo obligatoire", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.contentLayout.setVisibility(View.GONE);

        Map<String, String> body = new HashMap<>();
        body.put("nouveauStatut", "LI");

        ApiClient.getApiService().changerStatut(nocde, body).enqueue(new Callback<LivraisonMobile>() {
            @Override
            public void onResponse(Call<LivraisonMobile> call, Response<LivraisonMobile> response) {
                if (response.isSuccessful()) {
                    uploadProofsThenFinish();
                } else {
                    showErrorAndReset("Erreur lors du changement de statut");
                }
            }

            @Override
            public void onFailure(Call<LivraisonMobile> call, Throwable t) {
                showErrorAndReset("Erreur réseau: " + t.getMessage());
            }
        });
    }

    private String bitmapToBase64(Bitmap bitmap) {
        java.io.ByteArrayOutputStream byteArrayOutputStream = new java.io.ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return "data:image/jpeg;base64," + android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP);
    }

    private void uploadProofsThenFinish() {
        String base64Photo = bitmapToBase64(photoBitmap);
        PodAsset photoAsset = new PodAsset(
                "photo",
                base64Photo,
                "image/jpeg",
                (long) (base64Photo.length())
        );
        photoAsset.setStorageProvider("base64");

        ApiClient.getApiService().saveProof(nocde, photoAsset).enqueue(new Callback<PodAsset>() {
            @Override
            public void onResponse(Call<PodAsset> call, Response<PodAsset> response) {
                // Photo uploaded successfully
                if (!binding.signatureView.isEmpty()) {
                    uploadSignatureThenFinish();
                } else {
                    finishSuccessfully();
                }
            }

            @Override
            public void onFailure(Call<PodAsset> call, Throwable t) {
                Toast.makeText(ChangerStatutActivity.this,
                        "Statut changé mais photo non enregistrée", Toast.LENGTH_SHORT).show();
                finishSuccessfully();
            }
        });
    }

    private void uploadSignatureThenFinish() {
        Bitmap signatureBitmap = binding.signatureView.exportBitmap();
        String base64Sig = bitmapToBase64(signatureBitmap);
        PodAsset signatureAsset = new PodAsset(
                "signature",
                base64Sig,
                "image/png",
                (long) (base64Sig.length())
        );
        signatureAsset.setStorageProvider("base64");

        ApiClient.getApiService().saveProof(nocde, signatureAsset).enqueue(new Callback<PodAsset>() {
            @Override
            public void onResponse(Call<PodAsset> call, Response<PodAsset> response) {
                finishSuccessfully();
            }

            @Override
            public void onFailure(Call<PodAsset> call, Throwable t) {
                Toast.makeText(ChangerStatutActivity.this,
                        "Photo enregistrée mais signature non sauvegardée", Toast.LENGTH_SHORT).show();
                finishSuccessfully();
            }
        });
    }

    private void confirmAjourne() {
        String cause = binding.etCauseAjournement.getText().toString().trim();
        if (cause.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner une cause", Toast.LENGTH_SHORT).show();
            return;
        }

        String finalCause = cause;
        if ("Autre".equals(cause)) {
            finalCause = binding.etCauseCustom.getText().toString().trim();
            if (finalCause.isEmpty()) {
                Toast.makeText(this, "Veuillez décrire la cause personnalisée", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.contentLayout.setVisibility(View.GONE);

        Map<String, String> body = new HashMap<>();
        body.put("nouveauStatut", "AL");
        body.put("causeAjournement", finalCause);

        final String causeForMessage = finalCause;
        ApiClient.getApiService().changerStatut(nocde, body).enqueue(new Callback<LivraisonMobile>() {
            @Override
            public void onResponse(Call<LivraisonMobile> call, Response<LivraisonMobile> response) {
                if (response.isSuccessful()) {
                    sendAlertMessageThenFinish(causeForMessage);
                } else {
                    showErrorAndReset("Erreur lors du changement de statut");
                }
            }

            @Override
            public void onFailure(Call<LivraisonMobile> call, Throwable t) {
                showErrorAndReset("Erreur réseau: " + t.getMessage());
            }
        });
    }

    private void sendAlertMessageThenFinish(String cause) {
        String alertMessage = "🚨 ALERTE AJOURNEMENT - Commande N°" + nocde + "\n" +
                "Client: " + (clientNom != null ? clientNom : "N/A") + "\n" +
                "Cause: " + cause;

        ChatMessage message = new ChatMessage(alertMessage);

        ApiClient.getApiService().postChatMessage(nocde, message).enqueue(new Callback<ChatMessage>() {
            @Override
            public void onResponse(Call<ChatMessage> call, Response<ChatMessage> response) {
                finishSuccessfully();
            }

            @Override
            public void onFailure(Call<ChatMessage> call, Throwable t) {
                Toast.makeText(ChangerStatutActivity.this,
                        "Statut changé mais alerte non envoyée", Toast.LENGTH_SHORT).show();
                finishSuccessfully();
            }
        });
    }

    private void finishSuccessfully() {
        binding.progressBar.setVisibility(View.GONE);
        Toast.makeText(this, "Livraison finalisée avec succès", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void showErrorAndReset(String errorMessage) {
        binding.progressBar.setVisibility(View.GONE);
        binding.contentLayout.setVisibility(View.VISIBLE);
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
