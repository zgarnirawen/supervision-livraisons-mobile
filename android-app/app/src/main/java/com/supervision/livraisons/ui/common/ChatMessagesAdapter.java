package com.supervision.livraisons.ui.common;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.supervision.livraisons.databinding.ItemChatMessageBinding;
import com.supervision.livraisons.model.ChatMessage;

import java.util.List;

public class ChatMessagesAdapter extends RecyclerView.Adapter<ChatMessagesAdapter.ChatVH> {
    private final List<ChatMessage> messages;
    private final int myId;

    public ChatMessagesAdapter(List<ChatMessage> messages, int myId) {
        this.messages = messages;
        this.myId = myId;
    }

    @NonNull
    @Override
    public ChatVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChatMessageBinding binding = ItemChatMessageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ChatVH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatVH holder, int position) {
        ChatMessage message = messages.get(position);
        holder.binding.tvMessage.setText(message.getMessageText());
        holder.binding.tvSender.setText(message.getSenderId() == myId ? "Moi" : "Interlocuteur");
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatVH extends RecyclerView.ViewHolder {
        private final ItemChatMessageBinding binding;

        ChatVH(ItemChatMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
