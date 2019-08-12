package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.view.ChannelListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChannelListItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = ChannelListItemAdapter.class.getSimpleName();
    private Context context;
    private List<Channel> channels; // cached list of channels
    public String filter;
    private ChannelListView.ChannelClickListener channelClickListener;
    private ChannelListView.ChannelClickListener channelLongClickListener;
    private ChannelListView.UserClickListener userClickListener;
    private ChannelListView.Style style;

    public ChannelListItemAdapter(Context context, List<Channel> channels) {
        this.context = context;
        this.channels = channels;
    }

    public ChannelListItemAdapter(Context context, int itemLayoutId) {
        this(context, new ArrayList<>());
    }

    public void setUserClickListener(ChannelListView.UserClickListener l) {
        userClickListener = l;
    }

    public void setChannelClickListener(ChannelListView.ChannelClickListener l ) {
        channelClickListener = l;
    }
    public void setChannelLongClickListener(ChannelListView.ChannelClickListener l ) {
        channelLongClickListener = l;
    }

    public void setStyle(ChannelListView.Style s) {
        style = s;
    }

    public void deleteChannel(Channel channel) {
        int index = 0;
        for (Channel c : channels) {
            if (c.getCid() == channel.getCid()) {
                channels.remove(index);
                notifyItemChanged(index);
            }
            index += 1;
        }
    }

    public void upsertChannel(Channel channel) {
        // try to remove
        this.deleteChannel(channel);
        // always add to the top of the list...
        channels.add(0, channel);
    }

    public void replaceChannels(List<Channel> channelList) {
        channels = channelList;

        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        ChannelListItemViewHolder r =  new ChannelListItemViewHolder(style.channelPreviewLayout, parent, style);
        r.setChannelClickListener(this.channelClickListener);
        r.setChannelLongClickListener(this.channelLongClickListener);
        r.setUserClickListener(this.userClickListener);

        return r;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChannelState channelState = channels.get(position).getChannelState();
        ((BaseChannelListItemViewHolder) holder).bind(this.context, channelState, position);
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }


}
