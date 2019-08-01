package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.getstream.sdk.chat.rest.response.ChannelResponse;

public abstract class BaseChannelListItemViewHolder extends RecyclerView.ViewHolder {


    public BaseChannelListItemViewHolder(int resId, ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(resId, parent, false));
    }

    public abstract void bind(Context context, ChannelResponse channelResponse, int position, View.OnClickListener clickListener, View.OnLongClickListener longClickListener);
}
