package com.supervision.livraisons.ui.livreur;

import android.graphics.Bitmap;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.supervision.livraisons.R;
import com.supervision.livraisons.api.ApiClient;
import com.supervision.livraisons.databinding.ActivityChangerStatutBinding;
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

    private ActivityChangerStatutBinding binding;
    private int nocde;
    private String selectedStatut = null;
    private Bitmap proofPhotoBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangerStatutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        nocde = getIntent().getIntExtra("nocde", -1);
        SessionManager session = new SessionManager(this);
        ApiClient.init(session);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Livraison N°" + nocde);
        }
        setupButtons();
    }

    private void setupButtons() {
        binding.btnCapturePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQ_CAPTURE_PHOTO);
        });
        binding.btnClearSignature.setOnClickListener(v -> binding.signatureView.clear());

        binding.btnLivre.setOnClickListener(v -> {
            selectedStatut = "LI";
            binding.btnLivre.setAlpha(1.0f);
            binding.btnAjourne.setAlpha(0.4f);
            UiUtils.setVisible(binding.tilCauseAjournement, false);
            binding.btnConfirmer.setEnabled(true);
        });

        binding.btnAjourne.setOnClickListener(v -> {
            selectedStatut = "AL";
            binding.btnAjourne.setAlpha(1.0f);
            binding.btnLivre.setAlpha(0.4f);
            UiUtils.setVisible(binding.tilCauseAjournement, true);
            binding.btnConfirmer.setEnabled(true);
        });

        binding.btnConfirmer.setEnabled(false);
        binding.btnConfirmer.setOnClickListener(v -> {
            if (selectedStatut == null) return;
            String cause = binding.etCauseAjournement.getText().toString().trim();
            if ("LI".equals(selectedStatut) && (proofPhotoBitmap == null || binding.signatureView.isEmpty())) {
                Toast.makeText(this, "Photo et signature sont obligatoires pour finaliser en Livré", Toast.LENGTH_SHORT).show();
                return;
            }
            if ("AL".equals(selectedStatut) && cause.isEmpty()) {
                binding.tilCauseAjournement.setError(getString(R.string.error_cause_required));
                return;
            }
            binding.tilCauseAjournement.setError(null);
            submitStatut(selectedStatut, cause);
        });
    }

    private void submitStatut(String statut, String cause) {
        binding.btnConfirmer.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        Map<String, String> body = new HashMap<>();
        body.put("nouveauStatut", statut);
        String remarque = binding.etRemarque.getText().toString().trim();
        if (!remarque.isEmpty()) body.put("remarque", remarque);
        if ("AL".equals(statut) && !cause.isEmpty()) body.put("causeAjournement", cause);

        ApiClient.getApiService().changerStatut(nocde, body).enqueue(new Callback<LivraisonMobile>() {
            @Override
            public void onResponse(Call<LivraisonMobile> call, Response<LivraisonMobile> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    if ("LI".equals(statut) && proofPhotoBitmap != null && !binding.signatureView.isEmpty()) {
                        uploadProofsThenFinish();
                    } else {
                        Toast.makeText(ChangerStatutActivity.this,
                                getString(R.string.success_statut_changed), Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                } else {
                    binding.btnConfirmer.setEnabled(true);
                    Toast.makeText(ChangerStatutActivity.this,
                            "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<LivraisonMobile> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnConfirmer.setEnabled(true);
                Toast.makeText(ChangerStatutActivity.this,
                        getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadProofsThenFinish() {
        PodAsset photo = new PodAsset(
                "photo",
                "s3://livraisons-proof/" + nocde + "/photo_" + System.currentTimeMillis() + ".jpg",
                "image/jpeg",
                (long) (proofPhotoBitmap.getByteCount()));
        PodAsset signature = new PodAsset(
                "signature",
                "s3://livraisons-proof/" + nocde + "/signature_" + System.currentTimeMillis() + ".png",
                "image/png",
                (long) (binding.signatureView.exportBitmap().getByteCount()));

        ApiClient.getApiService().saveProof(nocde, photo).enqueue(new Callback<PodAsset>() {
            @Override
            public void onResponse(Call<PodAsset> call, Response<PodAsset> response) {
                ApiClient.getApiService().saveProof(nocde, signature).enqueue(new Callback<PodAsset>() {
                    @Override
                    public void onResponse(Call<PodAsset> call2, Response<PodAsset> response2) {
                        Toast.makeText(ChangerStatutActivity.this,
                                getString(R.string.success_statut_changed), Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onFailure(Call<PodAsset> call2, Throwable t) {
                        Toast.makeText(ChangerStatutActivity.this,
                                "Statut changé mais signature non enregistrée", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                });
            }

            @Override
            public void onFailure(Call<PodAsset> call, Throwable t) {
                Toast.makeText(ChangerStatutActivity.this,
                        "Statut changé mais photo non enregistrée", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CAPTURE_PHOTO && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null && extras.get("data") instanceof Bitmap) {
                proofPhotoBitmap = (Bitmap) extras.get("data");
                binding.imgPreuvePhoto.setImageBitmap(proofPhotoBitmap);
                UiUtils.setVisible(binding.imgPreuvePhoto, true);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}
