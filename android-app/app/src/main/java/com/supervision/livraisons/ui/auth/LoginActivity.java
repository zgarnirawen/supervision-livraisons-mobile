package com.supervision.livraisons.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.supervision.livraisons.R;
import com.supervision.livraisons.api.ApiClient;
import com.supervision.livraisons.api.ApiService;
import com.supervision.livraisons.databinding.ActivityLoginBinding;
import com.supervision.livraisons.model.LoginRequest;
import com.supervision.livraisons.model.LoginResponse;
import com.supervision.livraisons.ui.controleur.ControleurDashboardActivity;
import com.supervision.livraisons.ui.livreur.LivreurDashboardActivity;
import com.supervision.livraisons.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        ApiClient.init(sessionManager);

        // Rediriger si déjà connecté
        if (sessionManager.isLoggedIn()) {
            navigateToDashboard();
            return;
        }

        setupAnimations();
        setupClickListeners();
    }

    private void setupAnimations() {
        // Animation d'entrée pour le logo et le formulaire
        binding.imgLogo.setAlpha(0f);
        binding.imgLogo.animate().alpha(1f).setDuration(800).setStartDelay(200).start();

        binding.cardLogin.setTranslationY(100f);
        binding.cardLogin.setAlpha(0f);
        binding.cardLogin.animate()
                .translationY(0f).alpha(1f)
                .setDuration(600).setStartDelay(400).start();
    }

    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String login = binding.etLogin.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (login.isEmpty() || password.isEmpty()) {
                showError(getString(R.string.error_login_empty));
                return;
            }

            performLogin(login, password);
        });
    }

    private void performLogin(String login, String password) {
        setLoading(true);

        ApiService api = ApiClient.getApiService();
        api.login(new LoginRequest(login, password)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse data = response.body();
                    sessionManager.saveSession(
                            data.getToken(),
                            data.getIdpers(),
                            data.getNomComplet(),
                            data.getLogin(),
                            data.getCodeposte(),
                            data.getRole()
                    );
                    // Reinitialiser le client avec le token
                    ApiClient.reset();
                    ApiClient.init(sessionManager);

                    navigateToDashboard();

                    // Mise à jour du token FCM
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    String fcmToken = task.getResult();
                                    ApiClient.getApiService().updateFcmToken(data.getIdpers(), fcmToken).enqueue(new Callback<Void>() {
                                        @Override
                                        public void onResponse(Call<Void> call, Response<Void> response) {}
                                        @Override
                                        public void onFailure(Call<Void> call, Throwable t) {}
                                    });
                                }
                            });
                } else {
                    showError(getString(R.string.error_login_failed));
                    shakeButton();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                String details = t != null && t.getMessage() != null ? t.getMessage() : "";
                if (!details.isEmpty()) {
                    showError(getString(R.string.error_network) + " (" + details + ")");
                } else {
                    showError(getString(R.string.error_network));
                }
            }
        });
    }

    private void navigateToDashboard() {
        Intent intent;
        if (sessionManager.isLivreur()) {
            intent = new Intent(this, LivreurDashboardActivity.class);
        } else {
            intent = new Intent(this, ControleurDashboardActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void setLoading(boolean loading) {
        binding.btnLogin.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setText(loading ? "" : getString(R.string.btn_login));
    }

    private void showError(String message) {
        binding.tvError.setText(message);
        binding.tvError.setVisibility(View.VISIBLE);
        binding.tvError.animate().alpha(1f).setDuration(300).start();
    }

    private void shakeButton() {
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        binding.btnLogin.startAnimation(shake);
    }
}
