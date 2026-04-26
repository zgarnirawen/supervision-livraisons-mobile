package com.supervision.livraisons.ui.common;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.supervision.livraisons.R;
import com.supervision.livraisons.api.ApiClient;
import com.supervision.livraisons.databinding.ActivityLivraisonChatBinding;
import com.supervision.livraisons.model.ChatMessage;
import com.supervision.livraisons.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LivraisonChatActivity extends AppCompatActivity {
    private ActivityLivraisonChatBinding binding;
    private final List<ChatMessage> messages = new ArrayList<>();
    private ChatMessagesAdapter adapter;
    private int nocde;
    private SessionManager sessionManager;
    private android.os.Handler pollHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            loadMessagesSilent();
            pollHandler.postDelayed(this, 3000); // 3 seconds
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLivraisonChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        nocde = getIntent().getIntExtra("nocde", -1);
        sessionManager = new SessionManager(this);
        ApiClient.init(sessionManager);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (nocde > 0) {
                getSupportActionBar().setTitle("Chat Livraison #" + nocde);
            } else {
                getSupportActionBar().setTitle("Support Général (Privé)");
            }
        }

        adapter = new ChatMessagesAdapter(messages, sessionManager.getIdpers());
        binding.recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerMessages.setAdapter(adapter);

        binding.btnSend.setOnClickListener(v -> sendMessage());
        loadMessages();
        pollHandler.postDelayed(pollRunnable, 3000);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        if (nocde > 0) {
            getMenuInflater().inflate(R.menu.menu_chat, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_details) {
            android.content.Intent intent = new android.content.Intent(this, LivraisonDetailActivity.class);
            intent.putExtra("nocde", nocde);
            intent.putExtra("isLivreur", sessionManager.isLivreur());
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pollHandler.removeCallbacks(pollRunnable);
    }

    private void loadMessages() {
        ApiClient.getApiService().getChatMessages(nocde).enqueue(new Callback<List<ChatMessage>>() {
            @Override
            public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    messages.clear();
                    messages.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    if (!messages.isEmpty()) {
                        binding.recyclerMessages.scrollToPosition(messages.size() - 1);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
                Toast.makeText(LivraisonChatActivity.this, "Erreur chargement chat", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMessagesSilent() {
        ApiClient.getApiService().getChatMessages(nocde).enqueue(new Callback<List<ChatMessage>>() {
            @Override
            public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ChatMessage> newMessages = response.body();
                    if (newMessages.size() != messages.size()) {
                        messages.clear();
                        messages.addAll(newMessages);
                        adapter.notifyDataSetChanged();
                        binding.recyclerMessages.scrollToPosition(messages.size() - 1);
                    }
                }
            }
            @Override
            public void onFailure(Call<List<ChatMessage>> call, Throwable t) {}
        });
    }

    private void sendMessage() {
        String text = binding.etMessage.getText() != null ? binding.etMessage.getText().toString().trim() : "";
        if (TextUtils.isEmpty(text)) return;
        ChatMessage payload = new ChatMessage(text);
        payload.setSenderId(sessionManager.getIdpers());

        ApiClient.getApiService().postChatMessage(nocde, payload).enqueue(new Callback<ChatMessage>() {
            @Override
            public void onResponse(Call<ChatMessage> call, Response<ChatMessage> response) {
                if (response.isSuccessful()) {
                    binding.etMessage.setText("");
                    loadMessages();
                } else {
                    String errorMsg = "Erreur " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorMsg = "Erreur lors de l'envoi du message";
                    }
                    Toast.makeText(LivraisonChatActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ChatMessage> call, Throwable t) {
                Toast.makeText(LivraisonChatActivity.this, "Erreur réseau: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
