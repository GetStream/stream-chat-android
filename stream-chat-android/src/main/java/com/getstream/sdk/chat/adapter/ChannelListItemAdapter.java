package com.getstream.sdk.chat.adapter;

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
    private static final ChannelItemPayloadDiff FULL_CHANNEL_ITEM_PAYLOAD_DIFF =
            new ChannelItemPayloadDiff(true, true, true, true, true, true);
    private static final ChannelItemPayloadDiff EMPTY_CHANNEL_ITEM_PAYLOAD_DIFF =
            new ChannelItemPayloadDiff(false, false, false, false, false, false);
    private List<Channel> channels; // cached list of channels
    private ChannelListView.ChannelClickListener channelClickListener;
    private ChannelListView.ChannelClickListener channelLongClickListener;
    private ChannelListView.UserClickListener userClickListener;
    private ChannelListViewStyle style;
    private ChannelViewHolderFactory viewHolderFactory;

    public ChannelListItemAdapter(List<Channel> channels) {
        this.channels = channels;
        this.viewHolderFactory = new ChannelViewHolderFactory();
    }

    public ChannelListItemAdapter() {
        this(new ArrayList<>());
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

        return viewHolderFactory.createChannelViewHolder(this, parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Channel channelState = channels.get(position);
        ((BaseChannelListItemViewHolder) holder).bind(channelState, position, FULL_CHANNEL_ITEM_PAYLOAD_DIFF);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        Channel channelState = channels.get(position);
        ChannelItemPayloadDiff diff = EMPTY_CHANNEL_ITEM_PAYLOAD_DIFF;
        if (payloads.isEmpty()) {
            diff = FULL_CHANNEL_ITEM_PAYLOAD_DIFF;
        } else {
            for (int i = 0; i < payloads.size(); i++) {
                diff = diff.plus((ChannelItemPayloadDiff) payloads.get(i));
            }
        }
        ((BaseChannelListItemViewHolder) holder).bind(channelState, position, diff);
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
