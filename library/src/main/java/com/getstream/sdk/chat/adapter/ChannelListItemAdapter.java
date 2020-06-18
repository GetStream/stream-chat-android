package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.view.channels.ChannelListView;
import com.getstream.sdk.chat.view.channels.ChannelListViewStyle;

import java.util.ArrayList;
import java.util.List;

import io.getstream.chat.android.client.models.Channel;

public class ChannelListItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<Channel> channels; // cached list of channels
    private ChannelListView.ChannelClickListener channelClickListener;
    private ChannelListView.ChannelClickListener channelLongClickListener;
    private ChannelListView.UserClickListener userClickListener;
    private ChannelListViewStyle style;

    private ChannelViewHolderFactory viewHolderFactory;

    public ChannelListItemAdapter(Context context, List<Channel> channels) {
        this.context = context;
        this.channels = channels;
        this.viewHolderFactory = new ChannelViewHolderFactory();
    }

    public ChannelListItemAdapter(Context context) {
        this(context, new ArrayList<>());
        this.viewHolderFactory = new ChannelViewHolderFactory();
    }

    public ChannelListView.ChannelClickListener getChannelClickListener() {
        return channelClickListener;
    }

    public void setChannelClickListener(ChannelListView.ChannelClickListener l) {
        channelClickListener = l;
    }

    public ChannelListView.ChannelClickListener getChannelLongClickListener() {
        return channelLongClickListener;
    }

    public void setChannelLongClickListener(ChannelListView.ChannelClickListener l) {
        channelLongClickListener = l;
    }

    public ChannelListView.UserClickListener getUserClickListener() {
        return userClickListener;
    }

    public void setUserClickListener(ChannelListView.UserClickListener l) {
        userClickListener = l;
    }

    public void replaceChannels(List<Channel> channelList) {

        List<Channel> cloneChannelList = new ArrayList<>(new ArrayList<>(channelList));

        final DiffUtil.DiffResult result = DiffUtil.calculateDiff(
                new ChannelListDiffCallback(channels, cloneChannelList), true);

        // only update those rows that change...
        result.dispatchUpdatesTo(this);
        channels = cloneChannelList;
    }

    @Override
    public int getItemViewType(int position) {

        Channel channel = channels.get(position);
        return viewHolderFactory.getChannelViewType(channel);

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // allow users of this library to use any view holder they want...
        // a subclass of BaseChannelListItemViewHolder is supported, or a completely custom ViewHolder...
        // - if it extends baseChannelListItemView holder apply the click listeners and style;
        // - otherwise do nothing special

        RecyclerView.ViewHolder anyViewHolder = viewHolderFactory.createChannelViewHolder(this, parent, viewType);

        return anyViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Channel channelState = channels.get(position);
        ((BaseChannelListItemViewHolder) holder).bind(this.context, channelState, position, null);
    }

    private ChannelItemPayloadDiff noDiff =  new ChannelItemPayloadDiff();

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        Channel channelState = channels.get(position);
        ChannelItemPayloadDiff diff;
        if (payloads.isEmpty()) {
            diff = noDiff;
        } else {
            diff = (ChannelItemPayloadDiff) payloads.get(0);
        }
        ((BaseChannelListItemViewHolder) holder).bind(this.context, channelState, position, diff);
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }

    public ChannelListViewStyle getStyle() {
        return style;
    }

    public void setStyle(ChannelListViewStyle s) {
        style = s;
    }

    public void setViewHolderFactory(ChannelViewHolderFactory viewHolderFactory) {
        this.viewHolderFactory = viewHolderFactory;
    }
}
