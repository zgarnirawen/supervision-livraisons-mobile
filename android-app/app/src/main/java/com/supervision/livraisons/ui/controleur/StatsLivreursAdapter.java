package com.supervision.livraisons.ui.controleur;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.supervision.livraisons.databinding.ItemStatLivreurBinding;
import com.supervision.livraisons.model.StatsDuJour;
import java.util.List;

public class StatsLivreursAdapter extends RecyclerView.Adapter<StatsLivreursAdapter.StatLivreurVH> {
    private final List<StatsDuJour.StatsLivreur> stats;
    private final OnLivreurActionClickListener listener;

    public interface OnLivreurActionClickListener {
        void onFilterClick(int livreurId);
    }

    public StatsLivreursAdapter(List<StatsDuJour.StatsLivreur> stats, OnLivreurActionClickListener listener) {
        this.stats = stats;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StatLivreurVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStatLivreurBinding binding = ItemStatLivreurBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new StatLivreurVH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StatLivreurVH holder, int position) {
        StatsDuJour.StatsLivreur stat = stats.get(position);
        holder.binding.tvNom.setText(stat.getLivreurNomComplet());
        holder.binding.tvLiv.setText(String.valueOf(stat.getLivrees()));
        holder.binding.tvEnc.setText(String.valueOf(stat.getEnCours()));
        holder.binding.tvAjo.setText(String.valueOf(stat.getAjournees()));
        
        holder.binding.getRoot().setOnClickListener(v -> listener.onFilterClick(stat.getLivreurId()));
    }

    @Override
    public int getItemCount() { return stats.size(); }

    static class StatLivreurVH extends RecyclerView.ViewHolder {
        private final ItemStatLivreurBinding binding;
        StatLivreurVH(ItemStatLivreurBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
