package com.supervision.livraisons.ui.controleur;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.supervision.livraisons.R;
import com.supervision.livraisons.api.ApiClient;
import com.supervision.livraisons.databinding.ActivityControleurDashboardBinding;
import com.supervision.livraisons.model.LivraisonMobile;
import com.supervision.livraisons.model.StatsDuJour;
import com.supervision.livraisons.ui.auth.LoginActivity;
import com.supervision.livraisons.ui.common.LivraisonDetailActivity;
import com.supervision.livraisons.ui.common.LivraisonsAdapter;
import com.supervision.livraisons.utils.SessionManager;
import com.supervision.livraisons.utils.UiUtils;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ControleurDashboardActivity extends AppCompatActivity {

    private ActivityControleurDashboardBinding binding;
    private SessionManager sessionManager;
    private LivraisonsAdapter adapter;
    private final List<LivraisonMobile> livraisonsList = new ArrayList<>();
    private String filterEtat = null;
    private String filterVille = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityControleurDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        ApiClient.init(sessionManager);

        setSupportActionBar(binding.toolbar);
        binding.tvBonjour.setText(getString(R.string.label_bonjour, sessionManager.getNomComplet()));
        binding.tvRole.setText(getString(R.string.label_role_controleur));
        binding.tvDate.setText("Livraisons du " + UiUtils.getTodayFormatted());

        setupRecyclerView();
        setupFilters();
        setupSwipeRefresh();
        loadStats();
        loadAll();
    }

    private void setupRecyclerView() {
        adapter = new LivraisonsAdapter(livraisonsList, nocde -> {
            Intent intent = new Intent(this, LivraisonDetailActivity.class);
            intent.putExtra("nocde", nocde);
            intent.putExtra("isLivreur", false);
            startActivity(intent);
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupFilters() {
        // Filtre par état
        binding.chipTous.setOnClickListener(v -> { filterEtat = null; loadAll(); });
        binding.chipEnCours.setOnClickListener(v -> { filterEtat = "EC"; loadAll(); });
        binding.chipLivres.setOnClickListener(v -> { filterEtat = "LI"; loadAll(); });
        binding.chipAjournes.setOnClickListener(v -> { filterEtat = "AL"; loadAll(); });

        // Filtre par ville
        binding.btnFiltrerVille.setOnClickListener(v -> {
            String ville = binding.etFiltreVille.getText().toString().trim();
            filterVille = ville.isEmpty() ? null : ville;
            loadAll();
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.role_controleur, R.color.accent);
        binding.swipeRefresh.setOnRefreshListener(() -> { loadStats(); loadAll(); });
    }

    private void loadStats() {
        ApiClient.getApiService().getStats().enqueue(new Callback<StatsDuJour>() {
            @Override
            public void onResponse(Call<StatsDuJour> call, Response<StatsDuJour> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StatsDuJour s = response.body();
                    binding.tvStatTotal.setText(String.valueOf(s.getTotalLivraisons()));
                    binding.tvStatLivrees.setText(String.valueOf(s.getLivrees()));
                    binding.tvStatEnCours.setText(String.valueOf(s.getEnCours()));
                    binding.tvStatAjournees.setText(String.valueOf(s.getAjournees()));
                    binding.tvTauxSucces.setText(s.getTauxSucces() + "%");
                    // Barre de progression
                    binding.progressTaux.setProgress((int) s.getTauxSucces());
                }
            }
            @Override
            public void onFailure(Call<StatsDuJour> call, Throwable t) {}
        });
    }

    private void loadAll() {
        binding.shimmerLayout.startShimmer();
        UiUtils.setVisible(binding.shimmerLayout, true);
        UiUtils.setVisible(binding.recyclerView, false);

        ApiClient.getApiService().getAllLivraisons(filterEtat, filterVille, null)
                .enqueue(new Callback<List<LivraisonMobile>>() {
            @Override
            public void onResponse(Call<List<LivraisonMobile>> call,
                                   Response<List<LivraisonMobile>> response) {
                binding.swipeRefresh.setRefreshing(false);
                binding.shimmerLayout.stopShimmer();
                UiUtils.setVisible(binding.shimmerLayout, false);
                if (response.isSuccessful() && response.body() != null) {
                    livraisonsList.clear();
                    livraisonsList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    binding.tvCountResults.setText(livraisonsList.size() + " livraison(s)");
                    UiUtils.setVisible(binding.recyclerView, true);
                    UiUtils.setVisible(binding.tvEmpty, livraisonsList.isEmpty());
                }
            }
            @Override
            public void onFailure(Call<List<LivraisonMobile>> call, Throwable t) {
                binding.swipeRefresh.setRefreshing(false);
                binding.shimmerLayout.stopShimmer();
                UiUtils.setVisible(binding.shimmerLayout, false);
                UiUtils.setVisible(binding.tvEmpty, true);
            }
        });
    }

    @Override
    protected void onResume() { super.onResume(); loadStats(); loadAll(); }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dialog_logout_title))
                    .setMessage(getString(R.string.dialog_logout_message))
                    .setPositiveButton(getString(R.string.nav_logout), (d, w) -> {
                        sessionManager.clearSession();
                        ApiClient.reset();
                        startActivity(new Intent(this, LoginActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        finish();
                    })
                    .setNegativeButton(getString(R.string.btn_annuler), null).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
