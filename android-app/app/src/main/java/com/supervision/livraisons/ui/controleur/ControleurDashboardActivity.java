package com.supervision.livraisons.ui.controleur;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.widget.Toast;
import com.supervision.livraisons.R;
import com.supervision.livraisons.api.ApiClient;
import com.supervision.livraisons.databinding.ActivityControleurDashboardBinding;
import com.supervision.livraisons.model.LivraisonMobile;
import com.supervision.livraisons.model.StatsDuJour;
import com.supervision.livraisons.ui.auth.LoginActivity;
import com.supervision.livraisons.ui.common.LivraisonChatActivity;
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
    private Integer filterLivreurId = null;

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
        
        binding.btnMap.setOnClickListener(v -> {
            startActivity(new Intent(this, ControleurMapActivity.class));
        });
        
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

        // Configuration Bottom Navigation
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_dashboard) {
                UiUtils.setVisible(binding.layoutLivraisons, true);
                UiUtils.setVisible(binding.layoutLivreurs, false);
                UiUtils.setVisible(binding.layoutChats, false);
                UiUtils.setVisible(binding.layoutHeaderStats, true);
                UiUtils.setVisible(binding.layoutFilters, true);
                return true;
            } else if (item.getItemId() == R.id.nav_livreurs) {
                UiUtils.setVisible(binding.layoutLivraisons, false);
                UiUtils.setVisible(binding.layoutLivreurs, true);
                UiUtils.setVisible(binding.layoutChats, false);
                UiUtils.setVisible(binding.layoutHeaderStats, false);
                UiUtils.setVisible(binding.layoutFilters, false);
                loadStats();
                return true;
            } else if (item.getItemId() == R.id.nav_chats) {
                UiUtils.setVisible(binding.layoutLivraisons, false);
                UiUtils.setVisible(binding.layoutLivreurs, false);
                UiUtils.setVisible(binding.layoutChats, true);
                UiUtils.setVisible(binding.layoutHeaderStats, false);
                UiUtils.setVisible(binding.layoutFilters, false);
                loadChats();
                return true;
            }
            return false;
        });
    }

    private void setupFilters() {
        // Filtre par état
        binding.chipTous.setOnClickListener(v -> { filterEtat = null; loadAll(); });
        binding.chipEnCours.setOnClickListener(v -> { filterEtat = "EC"; loadAll(); });
        binding.chipLivres.setOnClickListener(v -> { filterEtat = "LI"; loadAll(); });
        binding.chipAjournes.setOnClickListener(v -> { filterEtat = "AL"; loadAll(); });
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
                    binding.tvTauxSucces.setText(s.getTauxSucces() + "%");
                    binding.progressTaux.setProgress((int) s.getTauxSucces());
                    // Liste des livreurs
                    if (s.getParLivreur() != null && !s.getParLivreur().isEmpty()) {
                        StatsLivreursAdapter statsAdapter = new StatsLivreursAdapter(s.getParLivreur(), livreurId -> {
                            // Trouver le livreur
                            StatsDuJour.StatsLivreur clicked = null;
                            for (StatsDuJour.StatsLivreur sl : s.getParLivreur()) {
                                if (sl.getLivreurId() == livreurId) {
                                    clicked = sl;
                                    break;
                                }
                            }
                            if (clicked != null) {
                                StatsDuJour.StatsLivreur finalClicked = clicked;
                                android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_livreur_info, null);
                                AlertDialog dialog = new AlertDialog.Builder(ControleurDashboardActivity.this)
                                        .setView(dialogView)
                                        .create();

                                // Bind views
                                ((android.widget.TextView) dialogView.findViewById(R.id.tv_dialog_nom)).setText(clicked.getLivreurNomComplet());
                                ((android.widget.TextView) dialogView.findViewById(R.id.tv_dialog_livreur_id)).setText("ID: #" + String.format("%04d", livreurId));
                                ((android.widget.TextView) dialogView.findViewById(R.id.tv_dialog_tel)).setText(clicked.getLivreurTel());
                                ((android.widget.TextView) dialogView.findViewById(R.id.tv_dialog_total)).setText(String.valueOf(clicked.getTotal()));
                                ((android.widget.TextView) dialogView.findViewById(R.id.tv_dialog_livrees)).setText(String.valueOf(clicked.getLivrees()));
                                ((android.widget.TextView) dialogView.findViewById(R.id.tv_dialog_encours)).setText(String.valueOf(clicked.getEnCours()));
                                ((android.widget.TextView) dialogView.findViewById(R.id.tv_dialog_ajournees)).setText(String.valueOf(clicked.getAjournees()));

                                android.widget.TextView tvPos = dialogView.findViewById(R.id.tv_dialog_pos);
                                final double[] currentCoords = new double[2]; // [lat, lng]

                                ApiClient.getApiService().getLivreurLocation(livreurId).enqueue(new Callback<com.supervision.livraisons.model.LivreurLocation>() {
                                    @Override
                                    public void onResponse(Call<com.supervision.livraisons.model.LivreurLocation> call, Response<com.supervision.livraisons.model.LivreurLocation> response) {
                                        if (response.isSuccessful() && response.body() != null) {
                                            com.supervision.livraisons.model.LivreurLocation loc = response.body();
                                            currentCoords[0] = loc.getLatitude();
                                            currentCoords[1] = loc.getLongitude();
                                            String time = (loc.getCapturedAt() != null && loc.getCapturedAt().length() >= 16) 
                                                ? loc.getCapturedAt().substring(11, 16) : "--:--";
                                            tvPos.setText(String.format("Position: %.5f, %.5f (le %s)", 
                                                currentCoords[0], currentCoords[1], time));
                                        }
                                    }
                                    @Override public void onFailure(Call<com.supervision.livraisons.model.LivreurLocation> call, Throwable t) {}
                                });

                                tvPos.setOnClickListener(v -> {
                                    if (currentCoords[0] != 0) {
                                        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + currentCoords[0] + "," + currentCoords[1]);
                                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                        mapIntent.setPackage("com.google.android.apps.maps");
                                        startActivity(mapIntent);
                                    }
                                });

                                // Bind actions
                                dialogView.findViewById(R.id.btn_dialog_call).setOnClickListener(v -> {
                                    appelerLivreur(finalClicked.getLivreurTel());
                                    dialog.dismiss();
                                });

                                dialogView.findViewById(R.id.btn_dialog_sms).setOnClickListener(v -> {
                                    String tel = finalClicked.getLivreurTel();
                                    if (tel != null && !tel.isEmpty() && !"00000000".equals(tel)) {
                                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                                        intent.setData(android.net.Uri.parse("smsto:" + tel));
                                        startActivity(intent);
                                    }
                                    dialog.dismiss();
                                });

                                dialogView.findViewById(R.id.btn_dialog_support).setOnClickListener(v -> {
                                    Intent intent = new Intent(ControleurDashboardActivity.this, LivraisonChatActivity.class);
                                    intent.putExtra("nocde", -livreurId); // Support privé hors commande
                                    startActivity(intent);
                                    dialog.dismiss();
                                });

                                dialogView.findViewById(R.id.btn_dialog_close).setOnClickListener(v -> dialog.dismiss());

                                if (dialog.getWindow() != null) {
                                    dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                                }
                                dialog.show();
                            }
                        });
                        binding.recyclerStatsLivreurs.setAdapter(statsAdapter);
                        UiUtils.setVisible(binding.recyclerStatsLivreurs, true);
                    } else {
                        UiUtils.setVisible(binding.recyclerStatsLivreurs, false);
                    }
                }
            }
            @Override
            public void onFailure(Call<StatsDuJour> call, Throwable t) {}
        });
    }

    private void appelerLivreur(String tel) {
        if (tel == null || tel.isEmpty() || "00000000".equals(tel)) {
            android.widget.Toast.makeText(this, "Numéro indisponible pour ce livreur (vérifiez la base)", android.widget.Toast.LENGTH_LONG).show();
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:" + tel));
            startActivity(intent);
        } catch (Exception e) {
            android.widget.Toast.makeText(this, "Erreur lors de l'appel : " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void envoyerSMSLivreur(String tel) {
        if (tel == null || tel.isEmpty() || "00000000".equals(tel)) {
            android.widget.Toast.makeText(this, "Numéro indisponible pour ce livreur (vérifiez la base)", android.widget.Toast.LENGTH_LONG).show();
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO, android.net.Uri.parse("smsto:" + tel));
            startActivity(intent);
        } catch (Exception e) {
            android.widget.Toast.makeText(this, "Erreur lors de l'envoi du message : " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void loadChats() {
        // Chargement des conversations (Livraisons + Support)
        ApiClient.getApiService().getChatChannels().enqueue(new retrofit2.Callback<List<com.supervision.livraisons.model.ChatChannel>>() {
            @Override
            public void onResponse(retrofit2.Call<List<com.supervision.livraisons.model.ChatChannel>> call, retrofit2.Response<List<com.supervision.livraisons.model.ChatChannel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.supervision.livraisons.ui.common.ChannelsAdapter chatAdapter = new com.supervision.livraisons.ui.common.ChannelsAdapter(response.body(), nocde -> {
                        Intent intent = new Intent(ControleurDashboardActivity.this, com.supervision.livraisons.ui.common.LivraisonChatActivity.class);
                        intent.putExtra("nocde", nocde);
                        startActivity(intent);
                    });
                    binding.recyclerChats.setLayoutManager(new LinearLayoutManager(ControleurDashboardActivity.this));
                    binding.recyclerChats.setAdapter(chatAdapter);
                }
            }
            @Override
            public void onFailure(retrofit2.Call<List<com.supervision.livraisons.model.ChatChannel>> call, Throwable t) {}
        });
    }

    private void loadAll() {
        binding.shimmerLayout.startShimmer();
        UiUtils.setVisible(binding.shimmerLayout, true);
        UiUtils.setVisible(binding.recyclerView, false);

        ApiClient.getApiService().getAllLivraisons(filterEtat, null, filterLivreurId)
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
