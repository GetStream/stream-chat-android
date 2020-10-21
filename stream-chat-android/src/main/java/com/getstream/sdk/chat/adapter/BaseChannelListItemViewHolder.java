package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.view.View;

import com.getstream.sdk.chat.view.channels.ChannelListView;
import com.getstream.sdk.chat.view.channels.ChannelListViewStyle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import io.getstream.chat.android.client.models.Channel;

public abstract class BaseChannelListItemViewHolder extends RecyclerView.ViewHolder {


    public BaseChannelListItemViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public abstract void bind(Context context, Channel channelState, int position, @Nullable ChannelItemPayloadDiff payloads);

    public abstract void setStyle(ChannelListViewStyle style);

    public abstract void setUserClickListener(ChannelListView.UserClickListener l);

    public abstract void setChannelClickListener(ChannelListView.ChannelClickListener l);

    public abstract void setChannelLongClickListener(ChannelListView.ChannelClickListener l);

}
