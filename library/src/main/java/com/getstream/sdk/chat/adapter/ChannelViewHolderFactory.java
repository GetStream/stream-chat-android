package com.getstream.sdk.chat.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.getstream.sdk.chat.BaseAttachmentViewHolder;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.view.ChannelListViewStyle;

import java.util.List;

/**
 * Allows you to easily customize channel rendering
 */
public class ChannelViewHolderFactory {
    private static int CHANNEL_GENERAL = 0;

    public int getChannelViewType(Channel channel) {
        return CHANNEL_GENERAL;
    }


    public BaseChannelListItemViewHolder createChannelViewHolder(ChannelListItemAdapter adapter, ViewGroup parent,int viewType) {
        if (viewType == CHANNEL_GENERAL) {
            ChannelListViewStyle style = adapter.getStyle();
            View v = LayoutInflater.from(parent.getContext()).inflate(style.channelPreviewLayout, parent, false);
            ChannelListItemViewHolder holder = new ChannelListItemViewHolder(v);
            holder.setStyle(style);
            holder.setChannelClickListener(adapter.getChannelClickListener());
            holder.setChannelLongClickListener(adapter.getChannelLongClickListener());
            holder.setUserClickListener(adapter.getUserClickListener());
            return holder;
        } else {
            return null;
        }
    }

}
