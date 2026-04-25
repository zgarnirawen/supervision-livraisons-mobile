package com.supervision.livraisons.ui.livreur;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.supervision.livraisons.R;
import com.supervision.livraisons.api.ApiClient;
import com.supervision.livraisons.databinding.ActivityChangerStatutBinding;
import com.supervision.livraisons.model.LivraisonMobile;
import com.supervision.livraisons.utils.SessionManager;
import com.supervision.livraisons.utils.UiUtils;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangerStatutActivity extends AppCompatActivity {

    private ActivityChangerStatutBinding binding;
    private int nocde;
    private String selectedStatut = null;

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
                    Toast.makeText(ChangerStatutActivity.this,
                            getString(R.string.success_statut_changed), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
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

    @Override
    public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}
