package com.supervision.livraisons.ui.livreur;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.supervision.livraisons.R;
import com.supervision.livraisons.model.ChatChannel;
import java.util.List;

public class ChatChannelAdapter extends RecyclerView.Adapter<ChatChannelAdapter.ViewHolder> {

    private final List<ChatChannel> items;
    private final OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(int nocde);
    }

    public ChatChannelAdapter(List<ChatChannel> items, OnConversationClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatChannel item = items.get(position);
        holder.tvName.setText(item.getTitle());
        holder.tvNocde.setText(item.isSupport() ? "Conversation privée" : "Commande #" + item.getNocde());
        holder.tvLastMsg.setText(item.getLastMessage() != null ? item.getLastMessage() : "Ouvrir la conversation...");
        holder.tvTime.setText(item.getLastMessageAt() != null ? item.getLastMessageAt().substring(11, 16) : "");

        holder.itemView.setOnClickListener(v -> listener.onConversationClick(item.getNocde()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLastMsg, tvTime, tvNocde;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_convo_name);
            tvLastMsg = itemView.findViewById(R.id.tv_convo_last_msg);
            tvTime = itemView.findViewById(R.id.tv_convo_time);
            tvNocde = itemView.findViewById(R.id.tv_convo_nocde);
        }
    }
}
