package com.supervision.livraisons.ui.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.supervision.livraisons.R;
import com.supervision.livraisons.model.ChatChannel;
import java.util.List;

public class ChannelsAdapter extends RecyclerView.Adapter<ChannelsAdapter.ViewHolder> {
    private final List<ChatChannel> channels;
    private final OnChannelClickListener listener;

    public interface OnChannelClickListener {
        void onChannelClick(int nocde);
    }

    public ChannelsAdapter(List<ChatChannel> channels, OnChannelClickListener listener) {
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
    }

    @Override
    public int getItemCount() { return channels.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLastMsg, tvType;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_convo_name);
            tvLastMsg = itemView.findViewById(R.id.tv_convo_last_msg);
            tvType = itemView.findViewById(R.id.tv_convo_nocde);
        }
    }
}
