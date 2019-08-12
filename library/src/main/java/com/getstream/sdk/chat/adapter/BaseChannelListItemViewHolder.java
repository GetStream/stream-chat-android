package com.getstream.sdk.chat.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.view.ChannelListView;

public abstract class BaseChannelListItemViewHolder extends RecyclerView.ViewHolder {


    public BaseChannelListItemViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public abstract void bind(Context context, ChannelState channelState, int position);

    public abstract void setStyle(ChannelListView.Style style);
    public abstract void setUserClickListener(ChannelListView.UserClickListener l);
    public abstract void setChannelClickListener(ChannelListView.ChannelClickListener l );
    public abstract void setChannelLongClickListener(ChannelListView.ChannelClickListener l );
}
