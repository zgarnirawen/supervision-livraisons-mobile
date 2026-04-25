package com.supervision.livraisons.ui.common;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
        private final TextView tvNoCde;
        private final TextView tvClient;
        private final TextView tvAdresse;
        private final TextView tvVille;
        private final TextView tvStatut;
        private final TextView tvModePay;
        private final TextView tvLivreur;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_livraison);
            tvNoCde = itemView.findViewById(R.id.tv_nocde);
            tvClient = itemView.findViewById(R.id.tv_client);
            tvAdresse = itemView.findViewById(R.id.tv_adresse);
            tvVille = itemView.findViewById(R.id.tv_ville);
            tvStatut = itemView.findViewById(R.id.tv_statut);
            tvModePay = itemView.findViewById(R.id.tv_mode_pay);
            tvLivreur = itemView.findViewById(R.id.tv_livreur);
        }

        public void bind(LivraisonMobile l, OnItemClickListener listener) {
            tvNoCde.setText("Commande N°" + l.getNocde());
            tvClient.setText(l.getClientNomComplet());
            tvAdresse.setText(l.getClientAdresse());
            tvVille.setText("📍 " + l.getClientVille() + " " + l.getClientCodePostal());
            tvModePay.setText(UiUtils.formatModePay(l.getModepay()));
            tvLivreur.setText("🚚 " + l.getLivreurNomComplet());

            UiUtils.applyStatutStyle(itemView.getContext(), tvStatut, l.getEtatliv());

            // Accentuer la bordure gauche selon statut
            int borderColor;
            switch (l.getEtatliv() != null ? l.getEtatliv() : "") {
                case "LI": borderColor = itemView.getContext()
                        .getColor(R.color.status_li_border); break;
                case "AL": borderColor = itemView.getContext()
                        .getColor(R.color.status_al_border); break;
                default:   borderColor = itemView.getContext()
                        .getColor(R.color.status_ec_border); break;
            }
            itemView.findViewById(R.id.view_status_bar).setBackgroundColor(borderColor);

            card.setOnClickListener(v -> listener.onItemClick(l.getNocde()));

            // Ripple animation
            card.setClickable(true);
            card.setFocusable(true);
        }
    }
}
