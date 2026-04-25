package com.supervision.livraisons.ui.livreur;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.supervision.livraisons.R;
import com.supervision.livraisons.api.ApiClient;
import com.supervision.livraisons.api.ApiService;
import com.supervision.livraisons.databinding.ActivityLivreurDashboardBinding;
import com.supervision.livraisons.model.LivraisonMobile;
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

public class LivreurDashboardActivity extends AppCompatActivity {

    private ActivityLivreurDashboardBinding binding;
    private SessionManager sessionManager;
    private LivraisonsAdapter adapter;
    private final List<LivraisonMobile> livraisonsList = new ArrayList<>();
    private final List<LivraisonMobile> allLivraisons = new ArrayList<>();
    private String currentFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLivreurDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        ApiClient.init(sessionManager);

        setSupportActionBar(binding.toolbar);

        setupHeader();
        setupRecyclerView();
        setupStatsFilterClicks();
        setupSwipeRefresh();
        loadLivraisons();
    }

    private void setupHeader() {
        binding.tvBonjour.setText(getString(R.string.label_bonjour, sessionManager.getNomComplet()));
        binding.tvRole.setText(getString(R.string.label_role_livreur));
        binding.tvDate.setText("Livraisons du " + UiUtils.getTodayFormatted());
    }

    private void setupRecyclerView() {
        adapter = new LivraisonsAdapter(livraisonsList, nocde -> {
            Intent intent = new Intent(this, LivraisonDetailActivity.class);
            intent.putExtra("nocde", nocde);
            intent.putExtra("isLivreur", true);
            startActivity(intent);
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setHasFixedSize(false);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent);
        binding.swipeRefresh.setOnRefreshListener(this::loadLivraisons);
    }

    private void setupStatsFilterClicks() {
        binding.chipTotal.getRoot().setOnClickListener(v -> applyStatutFilter(null));
        binding.chipLivrees.getRoot().setOnClickListener(v -> applyStatutFilter("LI"));
        binding.chipEnCours.getRoot().setOnClickListener(v -> applyStatutFilter("EC"));
        binding.chipAjournes.getRoot().setOnClickListener(v -> applyStatutFilter("AL"));
    }

    private void loadLivraisons() {
        binding.shimmerLayout.startShimmer();
        UiUtils.setVisible(binding.shimmerLayout, true);
        UiUtils.setVisible(binding.recyclerView, false);
        UiUtils.setVisible(binding.tvEmpty, false);

        ApiClient.getApiService().getMesLivraisons().enqueue(new Callback<List<LivraisonMobile>>() {
            @Override
            public void onResponse(Call<List<LivraisonMobile>> call, Response<List<LivraisonMobile>> response) {
                binding.swipeRefresh.setRefreshing(false);
                binding.shimmerLayout.stopShimmer();
                UiUtils.setVisible(binding.shimmerLayout, false);

                if (response.isSuccessful() && response.body() != null) {
                    allLivraisons.clear();
                    allLivraisons.addAll(response.body());
                    updateStats();
                    applyStatutFilter(currentFilter);
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

    private void applyStatutFilter(String statut) {
        currentFilter = statut;
        livraisonsList.clear();
        for (LivraisonMobile item : allLivraisons) {
            if (statut == null || statut.equals(item.getEtatliv())) {
                livraisonsList.add(item);
            }
        }
        adapter.notifyDataSetChanged();
        boolean isEmpty = livraisonsList.isEmpty();
        UiUtils.setVisible(binding.recyclerView, !isEmpty);
        UiUtils.setVisible(binding.tvEmpty, isEmpty);
    }

    private void updateStats() {
        long total = livraisonsList.size();
        long livrees = livraisonsList.stream().filter(l -> "LI".equals(l.getEtatliv())).count();
        long enCours = livraisonsList.stream().filter(l -> "EC".equals(l.getEtatliv())).count();
        long ajournees = livraisonsList.stream().filter(l -> "AL".equals(l.getEtatliv())).count();

        binding.chipTotal.tvStatValue.setText(String.valueOf(total));
        binding.chipTotal.tvStatLabel.setText("Total");

        binding.chipLivrees.tvStatValue.setText(String.valueOf(livrees));
        binding.chipLivrees.tvStatLabel.setText("Livrées");

        binding.chipEnCours.tvStatValue.setText(String.valueOf(enCours));
        binding.chipEnCours.tvStatLabel.setText("En cours");

        binding.chipAjournes.tvStatValue.setText(String.valueOf(ajournees));
        binding.chipAjournes.tvStatLabel.setText("Ajournées");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLivraisons(); // Rafraîchir au retour du détail
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            showLogoutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_logout_title))
                .setMessage(getString(R.string.dialog_logout_message))
                .setPositiveButton(getString(R.string.nav_logout), (d, w) -> logout())
                .setNegativeButton(getString(R.string.btn_annuler), null)
                .show();
    }

    private void logout() {
        sessionManager.clearSession();
        ApiClient.reset();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
