package com.supervision.livraisons.ui.controleur;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.supervision.livraisons.R;
import com.supervision.livraisons.model.ClientStats;
import java.util.List;

public class ClientsAdapter extends RecyclerView.Adapter<ClientsAdapter.ViewHolder> {
    private final List<ClientStats> clients;
    private final Context context;

    public ClientsAdapter(Context context, List<ClientStats> clients) {
        this.context = context;
        this.clients = clients;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client_stat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClientStats stat = clients.get(position);
        
        String nomComplet = stat.getNom();
        if (stat.getPrenom() != null && !stat.getPrenom().isEmpty()) {
            nomComplet += " " + stat.getPrenom();
        }
        holder.tvNom.setText(nomComplet);
        
        if (stat.getCategorie() != null && !stat.getCategorie().isEmpty()) {
            holder.tvCategorie.setText(stat.getCategorie() + (stat.getVille() != null ? " - " + stat.getVille() : ""));
        } else {
            holder.tvCategorie.setText(stat.getVille() != null ? stat.getVille() : "Standard");
        }

        holder.tvTotal.setText(String.valueOf(stat.getTotalLivraisons()));
        holder.tvLiv.setText(String.valueOf(stat.getLivrees()));
        holder.tvEncours.setText(String.valueOf(stat.getEnCours()));
        holder.tvAjo.setText(String.valueOf(stat.getAjournees()));

        holder.btnCall.setOnClickListener(v -> {
            if (stat.getTel() != null && !stat.getTel().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + stat.getTel()));
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Numéro indisponible", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnMap.setOnClickListener(v -> {
            if (stat.getLatitude() != null && stat.getLongitude() != null) {
                Uri gmmIntentUri = Uri.parse("geo:" + stat.getLatitude() + "," + stat.getLongitude());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(mapIntent);
                }
            } else if (stat.getAdresse() != null || stat.getVille() != null) {
                String query = (stat.getAdresse() != null ? stat.getAdresse() + ", " : "") + 
                              (stat.getVille() != null ? stat.getVille() : "");
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(query));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(mapIntent);
                }
            } else {
                Toast.makeText(context, "Position indisponible", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() { return clients.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNom, tvCategorie, tvTotal, tvLiv, tvEncours, tvAjo;
        ImageButton btnCall, btnMap;

        ViewHolder(View view) {
            super(view);
            tvNom = view.findViewById(R.id.tv_nom);
            tvCategorie = view.findViewById(R.id.tv_categorie);
            tvTotal = view.findViewById(R.id.tv_total);
            tvLiv = view.findViewById(R.id.tv_liv);
            tvEncours = view.findViewById(R.id.tv_encours);
            tvAjo = view.findViewById(R.id.tv_ajo);
            btnCall = view.findViewById(R.id.btn_call);
            btnMap = view.findViewById(R.id.btn_map);
        }
    }
}
