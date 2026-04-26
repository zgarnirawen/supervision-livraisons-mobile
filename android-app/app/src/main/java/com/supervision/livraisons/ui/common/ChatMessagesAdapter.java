package com.supervision.livraisons.ui.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.supervision.livraisons.R;
import com.supervision.livraisons.model.ChatMessage;

import java.util.List;

public class ChatMessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final List<ChatMessage> messages;
    private final int myId;

    public ChatMessagesAdapter(List<ChatMessage> messages, int myId) {
        this.messages = messages;
        this.myId = myId;
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getSenderId() == myId) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_sent, parent, false);
            return new SentVH(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_received, parent, false);
            return new ReceivedVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        String time = "";
        if (msg.getSentAt() != null && msg.getSentAt().length() >= 16) {
            time = msg.getSentAt().substring(11, 16);
        } else if (msg.getSentAt() != null) {
            time = msg.getSentAt();
        }

        if (holder instanceof SentVH) {
            ((SentVH) holder).tvMessage.setText(msg.getMessageText());
            ((SentVH) holder).tvTime.setText(time);
        } else {
            ((ReceivedVH) holder).tvMessage.setText(msg.getMessageText());
            ((ReceivedVH) holder).tvTime.setText(time);
            ((ReceivedVH) holder).tvSender.setText(msg.getSenderId() == myId ? "Moi" : "Support/Livreur");
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class SentVH extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        SentVH(View v) {
            super(v);
            tvMessage = v.findViewById(R.id.tv_message);
            tvTime = v.findViewById(R.id.tv_time);
        }
    }

    static class ReceivedVH extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvSender;
        ReceivedVH(View v) {
            super(v);
            tvMessage = v.findViewById(R.id.tv_message);
            tvTime = v.findViewById(R.id.tv_time);
            tvSender = v.findViewById(R.id.tv_sender);
        }
    }
}
