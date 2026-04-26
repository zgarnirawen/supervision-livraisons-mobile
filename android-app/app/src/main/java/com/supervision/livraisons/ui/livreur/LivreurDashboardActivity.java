package com.supervision.livraisons.ui.livreur;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.supervision.livraisons.R;
import com.supervision.livraisons.api.ApiClient;
import com.supervision.livraisons.databinding.ActivityLivreurDashboardBinding;
import com.supervision.livraisons.model.ChatChannel;
import com.supervision.livraisons.model.LivraisonMobile;
import com.supervision.livraisons.ui.auth.LoginActivity;
import com.supervision.livraisons.ui.common.ChannelsAdapter;
import com.supervision.livraisons.ui.common.LivraisonChatActivity;
import com.supervision.livraisons.ui.common.LivraisonDetailActivity;
import com.supervision.livraisons.ui.common.LivraisonsAdapter;
import com.supervision.livraisons.ui.controleur.ConversationsAdapter;
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
    private String currentFilterStatut = null;
    private String currentFilterVille = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLivreurDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        ApiClient.init(sessionManager);

        setSupportActionBar(binding.toolbar);

        setupHeader();
        setupNavigation();
        setupRecyclerView();
        setupStatsFilterClicks();
        setupVilleFilter();
        setupSwipeRefresh();
        setupLocationSharing();
        
        loadLivraisons();
    }

    private void setupHeader() {
        binding.tvBonjour.setText(getString(R.string.label_bonjour, sessionManager.getNomComplet()));
        binding.tvRole.setText(getString(R.string.label_role_livreur));
        binding.tvDate.setText("Livraisons du " + UiUtils.getTodayFormatted());
    }

    private void setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_livraisons) {
                UiUtils.setVisible(binding.layoutLivraisons, true);
                UiUtils.setVisible(binding.layoutChats, false);
                UiUtils.setVisible(binding.layoutHeaderLivreur, true);
                UiUtils.setVisible(binding.layoutFilters, true);
                return true;
            } else if (item.getItemId() == R.id.nav_chats) {
                UiUtils.setVisible(binding.layoutLivraisons, false);
                UiUtils.setVisible(binding.layoutChats, true);
                UiUtils.setVisible(binding.layoutHeaderLivreur, false);
                UiUtils.setVisible(binding.layoutFilters, false);
                loadChats();
                return true;
            }
            return false;
        });

        binding.btnCallController.setOnClickListener(v -> {
            String tel = binding.tvControllerTel.getText().toString();
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(android.net.Uri.parse("tel:" + tel));
            startActivity(intent);
        });

        binding.btnSmsController.setOnClickListener(v -> {
            String tel = binding.tvControllerTel.getText().toString();
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(android.net.Uri.parse("smsto:" + tel));
            startActivity(intent);
        });

        binding.btnChatController.setOnClickListener(v -> {
            Intent intent = new Intent(this, LivraisonChatActivity.class);
            int privateChannelId = -sessionManager.getIdpers();
            intent.putExtra("nocde", privateChannelId); 
            startActivity(intent);
        });
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
    }

    private void setupSwipeRefresh() {
        binding.layoutLivraisons.setColorSchemeResources(R.color.primary, R.color.accent);
        binding.layoutLivraisons.setOnRefreshListener(this::loadLivraisons);
    }

    private void setupStatsFilterClicks() {
        binding.chipTotal.getRoot().setOnClickListener(v -> applyFilters(null, currentFilterVille));
        binding.chipLivrees.getRoot().setOnClickListener(v -> applyFilters("LI", currentFilterVille));
        binding.chipEnCours.getRoot().setOnClickListener(v -> applyFilters("EC", currentFilterVille));
        binding.chipAjournes.getRoot().setOnClickListener(v -> applyFilters("AL", currentFilterVille));
    }

    private void setupVilleFilter() {
        binding.btnFiltrerVille.setOnClickListener(v -> {
            String ville = binding.etFiltreVille.getText().toString().trim();
            applyFilters(currentFilterStatut, ville.isEmpty() ? null : ville);
        });
    }

    private void setupLocationSharing() {
        binding.switchLocation.setChecked(sessionManager.isOnline());
        
        binding.switchLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setOnlineStatus(isChecked);
            if (isChecked) {
                checkLocationPermissionAndStart();
            } else {
                stopLocationService();
            }
        });

        if (sessionManager.isOnline()) {
            checkLocationPermissionAndStart();
        }

        binding.tvLocationBanner.setOnClickListener(v -> {
            String text = binding.tvLocationBanner.getText().toString();
            if (text.contains(":") && !text.contains("--")) {
                String coords = text.split(":")[1].trim();
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + coords);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        // Broadcast receiver pour les mises à jour réelles
        android.content.BroadcastReceiver locationReceiver = new android.content.BroadcastReceiver() {
            @Override
            public void onReceive(android.content.Context context, Intent intent) {
                double lat = intent.getDoubleExtra("lat", 0);
                double lng = intent.getDoubleExtra("lng", 0);
                if (lat != 0 && lng != 0) {
                    binding.tvLocationBanner.setText(String.format("Position: %.5f, %.5f", lat, lng));
                }
            }
        };
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(locationReceiver, new android.content.IntentFilter("LOCATION_UPDATE"), android.content.Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(locationReceiver, new android.content.IntentFilter("LOCATION_UPDATE"));
        }

        // Periodic fetch from backend
        startLocationUpdateTimer();
    }

    private void startLocationUpdateTimer() {
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (sessionManager.isOnline()) {
                    fetchAndUpdateLocation();
                }
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this, 15000); // 15 seconds
            }
        }, 5000);
    }

    private void fetchAndUpdateLocation() {
        ApiClient.getApiService().getMyLatestLocation().enqueue(new Callback<com.supervision.livraisons.model.LivreurLocation>() {
            @Override
            public void onResponse(Call<com.supervision.livraisons.model.LivreurLocation> call, Response<com.supervision.livraisons.model.LivreurLocation> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.supervision.livraisons.model.LivreurLocation location = response.body();
                    if (location.getLatitude() != null && location.getLongitude() != null) {
                        binding.tvLocationBanner.setText(String.format("Position: %.5f, %.5f", location.getLatitude(), location.getLongitude()));
                    }
                }
            }
            @Override
            public void onFailure(Call<com.supervision.livraisons.model.LivreurLocation> call, Throwable t) {
                // Silently fail
            }
        });
    }

    private void checkLocationPermissionAndStart() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) 
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            androidx.core.app.ActivityCompat.requestPermissions(this, 
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            startLocationService();
        }
    }

    private void startLocationService() {
        Intent serviceIntent = new Intent(this, com.supervision.livraisons.service.LocationService.class);
        androidx.core.content.ContextCompat.startForegroundService(this, serviceIntent);
        Toast.makeText(this, "Partage de position activé (Réel)", Toast.LENGTH_SHORT).show();
    }

    private void stopLocationService() {
        Intent serviceIntent = new Intent(this, com.supervision.livraisons.service.LocationService.class);
        stopService(serviceIntent);
        binding.tvLocationBanner.setText("Position: --, --");
        Toast.makeText(this, "Partage de position désactivé", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            startLocationService();
        } else {
            binding.switchLocation.setChecked(false);
            sessionManager.setOnlineStatus(false);
        }
    }

    private void loadLivraisons() {
        binding.shimmerLayout.startShimmer();
        UiUtils.setVisible(binding.shimmerLayout, true);
        UiUtils.setVisible(binding.recyclerView, false);
        UiUtils.setVisible(binding.tvEmpty, false);

        ApiClient.getApiService().getMesLivraisons().enqueue(new Callback<List<LivraisonMobile>>() {
            @Override
            public void onResponse(Call<List<LivraisonMobile>> call, Response<List<LivraisonMobile>> response) {
                binding.layoutLivraisons.setRefreshing(false);
                binding.shimmerLayout.stopShimmer();
                UiUtils.setVisible(binding.shimmerLayout, false);

                if (response.isSuccessful() && response.body() != null) {
                    allLivraisons.clear();
                    allLivraisons.addAll(response.body());
                    updateStats();
                    applyFilters(currentFilterStatut, currentFilterVille);
                }
            }

            @Override
            public void onFailure(Call<List<LivraisonMobile>> call, Throwable t) {
                binding.layoutLivraisons.setRefreshing(false);
                binding.shimmerLayout.stopShimmer();
                UiUtils.setVisible(binding.shimmerLayout, false);
                UiUtils.setVisible(binding.tvEmpty, true);
            }
        });
    }

    private void loadChats() {
        // Charge toutes les conversations (commandes + conversations privées avec livreurs)
        ApiClient.getApiService().getChatChannels().enqueue(new Callback<List<ChatChannel>>() {
            @Override
            public void onResponse(Call<List<ChatChannel>> call, Response<List<ChatChannel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ChannelsAdapter chatAdapter = new ChannelsAdapter(response.body(), nocde -> {
                        Intent intent = new Intent(LivreurDashboardActivity.this, LivraisonChatActivity.class);
                        intent.putExtra("nocde", nocde);
                        startActivity(intent);
                    });
                    binding.recyclerChats.setAdapter(chatAdapter);
                }
            }
            @Override
            public void onFailure(Call<List<ChatChannel>> call, Throwable t) {}
        });

        binding.fabNewChat.setOnClickListener(v -> {
            if (allLivraisons.isEmpty()) {
                Toast.makeText(this, "Aucune livraison aujourd'hui", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String[] items = new String[allLivraisons.size()];
            for (int i = 0; i < allLivraisons.size(); i++) {
                LivraisonMobile l = allLivraisons.get(i);
                items[i] = "Livraison #" + l.getNocde() + " - " + l.getClientNom();
            }

            new AlertDialog.Builder(this)
                .setTitle("Choisir une livraison")
                .setItems(items, (dialog, which) -> {
                    Intent intent = new Intent(this, LivraisonChatActivity.class);
                    intent.putExtra("nocde", allLivraisons.get(which).getNocde());
                    startActivity(intent);
                })
                .show();
        });
    }

    private void applyFilters(String statut, String ville) {
        currentFilterStatut = statut;
        currentFilterVille = ville;
        livraisonsList.clear();
        for (LivraisonMobile item : allLivraisons) {
            boolean matchStatut = (statut == null || statut.equals(item.getEtatliv()));
            boolean matchVille = true;
            if (ville != null && item.getClientVille() != null) {
                matchVille = item.getClientVille().toLowerCase().contains(ville.toLowerCase());
            }
            if (matchStatut && matchVille) {
                livraisonsList.add(item);
            }
        }
        adapter.notifyDataSetChanged();
        boolean isEmpty = livraisonsList.isEmpty();
        UiUtils.setVisible(binding.recyclerView, !isEmpty);
        UiUtils.setVisible(binding.tvEmpty, isEmpty);
    }

    private void updateStats() {
        long total = allLivraisons.size();
        long livrees = allLivraisons.stream().filter(l -> "LI".equals(l.getEtatliv())).count();
        long enCours = allLivraisons.stream().filter(l -> "EC".equals(l.getEtatliv())).count();
        long ajournees = allLivraisons.stream().filter(l -> "AL".equals(l.getEtatliv())).count();

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
        loadLivraisons();
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
