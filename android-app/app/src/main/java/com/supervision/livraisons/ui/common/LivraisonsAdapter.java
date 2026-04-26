package com.supervision.livraisons.ui.common;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.supervision.livraisons.R;
import com.supervision.livraisons.model.LivraisonMobile;
import com.supervision.livraisons.utils.UiUtils;

import java.util.List;

public class LivraisonsAdapter extends RecyclerView.Adapter<LivraisonsAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int nocde);
    }

    private final List<LivraisonMobile> items;
    private final OnItemClickListener listener;

    public LivraisonsAdapter(List<LivraisonMobile> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_livraison, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LivraisonMobile l = items.get(position);
        holder.bind(l, listener);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView card;
        private final TextView tvNoCde, tvClient, tvAdresse, tvVille, tvStatut, tvModePay;
        private final ImageButton btnCall, btnSms, btnChat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_livraison);
            tvNoCde = itemView.findViewById(R.id.tv_nocde);
            tvClient = itemView.findViewById(R.id.tv_client);
            tvAdresse = itemView.findViewById(R.id.tv_adresse);
            tvVille = itemView.findViewById(R.id.tv_ville);
            tvStatut = itemView.findViewById(R.id.tv_statut);
            tvModePay = itemView.findViewById(R.id.tv_mode_pay);
            btnCall = itemView.findViewById(R.id.btn_list_call);
            btnSms = itemView.findViewById(R.id.btn_list_sms);
            btnChat = itemView.findViewById(R.id.btn_list_chat);
        }

        public void bind(LivraisonMobile l, OnItemClickListener listener) {
            tvNoCde.setText("Commande N°" + l.getNocde());
            tvClient.setText(l.getClientNomComplet());
            tvAdresse.setText(l.getClientAdresse());
            tvVille.setText("📍 " + l.getClientVille());
            tvModePay.setText(UiUtils.formatModePay(l.getModepay()));
            UiUtils.applyStatutStyle(itemView.getContext(), tvStatut, l.getEtatliv());

            card.setOnClickListener(v -> listener.onItemClick(l.getNocde()));

            btnCall.setOnClickListener(v -> {
                if (l.getClientTel() != null) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + l.getClientTel()));
                    itemView.getContext().startActivity(intent);
                }
            });

            btnSms.setOnClickListener(v -> {
                if (l.getClientTel() != null) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + l.getClientTel()));
                    itemView.getContext().startActivity(intent);
                }
            });

            btnChat.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), LivraisonChatActivity.class);
                intent.putExtra("nocde", l.getNocde());
                itemView.getContext().startActivity(intent);
            });
        }
    }
}
