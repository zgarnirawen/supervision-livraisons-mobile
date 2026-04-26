package com.supervision.livraisons.ui.controleur;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.supervision.livraisons.api.ApiClient;
import com.supervision.livraisons.databinding.ActivityControleurMapBinding;
import com.supervision.livraisons.model.LivreurLocation;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ControleurMapActivity extends AppCompatActivity {

    private ActivityControleurMapBinding binding;
    private android.os.Handler refreshHandler;
    private static final long REFRESH_INTERVAL = 15000; // 15 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityControleurMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Carte des Livreurs");
        }

        refreshHandler = new android.os.Handler(android.os.Looper.getMainLooper());

        setupWebView();
        startLocationRefresh();
    }

    private void setupWebView() {
        WebSettings settings = binding.webView.getSettings();
        settings.setJavaScriptEnabled(true);
        binding.webView.loadUrl("file:///android_asset/map.html");
    }

    private void startLocationRefresh() {
        // Load immediately
        loadLocations();
        
        // Then refresh periodically
        refreshHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadLocations();
                refreshHandler.postDelayed(this, REFRESH_INTERVAL);
            }
        }, REFRESH_INTERVAL);
    }

    private void loadLocations() {
        ApiClient.getApiService().getAllLocations().enqueue(new Callback<List<LivreurLocation>>() {
            @Override
            public void onResponse(Call<List<LivreurLocation>> call, Response<List<LivreurLocation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String json = new Gson().toJson(response.body());
                    binding.webView.evaluateJavascript("updateMarkers('" + json.replace("'", "\\'") + "')", null);
                }
            }

            @Override
            public void onFailure(Call<List<LivreurLocation>> call, Throwable t) {
                Toast.makeText(ControleurMapActivity.this, "Erreur chargement positions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop refresh when activity is destroyed
        if (refreshHandler != null) {
            refreshHandler.removeCallbacksAndMessages(null);
        }
    }
}
