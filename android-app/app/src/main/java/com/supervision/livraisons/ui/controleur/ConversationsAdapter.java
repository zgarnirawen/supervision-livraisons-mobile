package com.supervision.livraisons.ui.controleur;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.supervision.livraisons.R;
import com.supervision.livraisons.model.LivraisonMobile;
import java.util.List;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ViewHolder> {

    private final List<LivraisonMobile> items;
    private final OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(int nocde);
    }

    public ConversationsAdapter(List<LivraisonMobile> items, OnConversationClickListener listener) {
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
        LivraisonMobile item = items.get(position);
        holder.tvName.setText(item.getLivreurNom() + " " + item.getLivreurPrenom());
        holder.tvNocde.setText("Commande #" + item.getNocde());
        holder.tvLastMsg.setText("Ouvrir la conversation...");
        holder.tvTime.setText(item.getDerniereModification() != null ? item.getDerniereModification().substring(11, 16) : "");

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
