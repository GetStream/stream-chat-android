package com.getstream.sdk.chat.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.getstream.sdk.chat.rest.response.ChannelState;

public abstract class BaseChannelListItemViewHolder extends RecyclerView.ViewHolder {


    public BaseChannelListItemViewHolder(int resId, ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(resId, parent, false));
    }

    public abstract void bind(Context context, ChannelState channelState, int position, View.OnClickListener clickListener, View.OnLongClickListener longClickListener);
}
