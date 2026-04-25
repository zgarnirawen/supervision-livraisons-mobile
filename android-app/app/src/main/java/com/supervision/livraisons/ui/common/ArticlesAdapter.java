package com.supervision.livraisons.ui.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.supervision.livraisons.R;
import com.supervision.livraisons.model.ArticleCommande;

import java.util.List;

public class ArticlesAdapter extends RecyclerView.Adapter<ArticlesAdapter.ViewHolder> {

    private final List<ArticleCommande> items;

    public ArticlesAdapter(List<ArticleCommande> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_article, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ArticleCommande a = items.get(position);
        holder.tvDesignation.setText(a.getDesignation());
        holder.tvRef.setText("Réf: " + a.getRefart());
        holder.tvQuantite.setText("Qté: " + a.getQuantite());
        holder.tvPrix.setText(String.format("%.2f DT/u", a.getPrixUnitaire()));
        holder.tvTotal.setText(String.format("= %.2f DT", a.getMontantTotal()));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDesignation, tvRef, tvQuantite, tvPrix, tvTotal;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDesignation = itemView.findViewById(R.id.tv_designation);
            tvRef = itemView.findViewById(R.id.tv_ref);
            tvQuantite = itemView.findViewById(R.id.tv_quantite);
            tvPrix = itemView.findViewById(R.id.tv_prix);
            tvTotal = itemView.findViewById(R.id.tv_total);
        }
    }
}
