package com.supervision.livraisons.ui.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.supervision.livraisons.R;
import com.supervision.livraisons.model.ChatChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChannelsAdapter extends RecyclerView.Adapter<ChannelsAdapter.ViewHolder> {
    private final List<ChatChannel> channels;
    private final OnChannelClickListener listener;

    public interface OnChannelClickListener {
        void onChannelClick(int nocde);
    }

    public ChannelsAdapter(List<ChatChannel> channels, OnChannelClickListener listener) {
        // Trier par date (du plus récent au plus ancien)
        Collections.sort(channels, (c1, c2) -> {
            String d1 = c1.getLastMessageAt();
            String d2 = c2.getLastMessageAt();
            if (d1 == null && d2 == null) return 0;
            if (d1 == null) return 1;
            if (d2 == null) return -1;
            return d2.compareTo(d1);
        });
        this.channels = channels;
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
        ChatChannel channel = channels.get(position);
        holder.tvName.setText(channel.getTitle());
        holder.tvLastMsg.setText(channel.getLastMessage());
        holder.itemView.setOnClickListener(v -> listener.onChannelClick(channel.getNocde()));
        
        
        if (channel.isSupport()) {
            holder.tvType.setText("Support Général");
            holder.tvType.setBackgroundResource(R.drawable.bg_tag_accent);
        } else {
            holder.tvType.setText("Livraison #" + channel.getNocde());
            holder.tvType.setBackgroundResource(R.drawable.bg_tag_light);
        }

        // Gérer l'affichage de l'heure
        if (channel.getLastMessageAt() != null && !channel.getLastMessageAt().isEmpty()) {
            try {
                SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = inFormat.parse(channel.getLastMessageAt());
                if (date != null) {
                    SimpleDateFormat outFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    holder.tvTime.setText(outFormat.format(date));
                } else {
                    holder.tvTime.setText("--:--");
                }
            } catch (Exception e) {
                // Si la date contient des millisecondes ou un format différent, on extrait l'heure
                String timeStr = channel.getLastMessageAt();
                if (timeStr.contains("T")) {
                    String[] parts = timeStr.split("T");
                    if (parts.length > 1 && parts[1].length() >= 5) {
                        holder.tvTime.setText(parts[1].substring(0, 5));
                    } else {
                        holder.tvTime.setText("--:--");
                    }
                } else {
                    holder.tvTime.setText("--:--");
                }
            }
        } else {
            holder.tvTime.setText("--:--");
        }
    }

    @Override
    public int getItemCount() { return channels.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLastMsg, tvType, tvTime;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_convo_name);
            tvLastMsg = itemView.findViewById(R.id.tv_convo_last_msg);
            tvType = itemView.findViewById(R.id.tv_convo_nocde);
            tvTime = itemView.findViewById(R.id.tv_convo_time);
        }
    }
}
