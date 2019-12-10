package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

public abstract class BaseMessageListItemViewHolder extends RecyclerView.ViewHolder {

    public BaseMessageListItemViewHolder(int resId, ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(resId, parent, false));
    }

    public abstract void bind(@NonNull Context context,
                              @NonNull ChannelState channelState,
                              @NonNull MessageListItem messageListItem,
                              @NonNull MessageListViewStyle style,
                              @NonNull MessageListView.BubbleHelper bubbleHelper,
                              @NonNull MessageViewHolderFactory factory,
                              int position);
}
