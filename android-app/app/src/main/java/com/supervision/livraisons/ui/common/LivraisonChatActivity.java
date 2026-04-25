package com.supervision.livraisons.ui.common;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

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
            getSupportActionBar().setTitle("Chat livraison N°" + nocde);
        }

        adapter = new ChatMessagesAdapter(messages, sessionManager.getIdpers());
        binding.recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerMessages.setAdapter(adapter);

        binding.btnSend.setOnClickListener(v -> sendMessage());
        loadMessages();
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
                }
            }

            @Override
            public void onFailure(Call<ChatMessage> call, Throwable t) {
                Toast.makeText(LivraisonChatActivity.this, "Erreur envoi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
