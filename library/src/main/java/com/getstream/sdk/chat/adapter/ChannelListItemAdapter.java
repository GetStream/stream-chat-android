package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.utils.ChannelListDiffCallback;
import com.getstream.sdk.chat.view.ChannelListView;
import com.getstream.sdk.chat.view.ChannelListViewStyle;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class ChannelListItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = ChannelListItemAdapter.class.getSimpleName();

    private Context context;
    private List<Channel> channels; // cached list of channels
    public String filter;
    private ChannelListView.ChannelClickListener channelClickListener;
    private ChannelListView.ChannelClickListener channelLongClickListener;
    private ChannelListView.UserClickListener userClickListener;
    private ChannelListViewStyle style;



    private ChannelViewHolderFactory viewHolderFactory;

    public ChannelListItemAdapter(Context context, List<Channel> channels) {
        this.context = context;
        this.channels = channels;
    }

    public ChannelListItemAdapter(Context context) {
        this(context, new ArrayList<>());
    }

    public ChannelListView.ChannelClickListener getChannelClickListener() {
        return channelClickListener;
    }

    public ChannelListView.ChannelClickListener getChannelLongClickListener() {
        return channelLongClickListener;
    }


    public ChannelListView.UserClickListener getUserClickListener() {
        return userClickListener;
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

    public void setStyle(ChannelListViewStyle s) {
        style = s;
    }

    public void deleteChannel(Channel channel) {
        int index = 0;
        for (Channel c : channels) {
            Log.i(TAG, String.format("channel cid %s iter cid %s", channel.getCid(), c.getCid()));
            if (TextUtils.equals(c.getCid(), channel.getCid())) {
                channels.remove(index);
                notifyItemChanged(index);
                break;
            }
            index += 1;
        }
    }

    public void upsertChannel(Channel channel) {
        // try to remove
        this.deleteChannel(channel);
        // always add to the top of the list...
        channels.add(0, channel);

        notifyDataSetChanged();
    }

    public void replaceChannels(List<Channel> channelList) {
        final DiffUtil.DiffResult result = DiffUtil.calculateDiff(
                new ChannelListDiffCallback(channels, channelList), true);

        // only update those rows that change...
        result.dispatchUpdatesTo(this);
        channels = channelList;
    }

    @Override
    public int getItemViewType(int position) {

        Channel channel = channels.get(position);
        return viewHolderFactory.getChannelViewType(channel);

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        // allow users of this library to use any view holder they want...
        // a subclass of BaseChannelListItemViewHolder is supported, or a completely custom ViewHolder...
        // - if it extends baseChannelListItemView holder apply the click listeners and style;
        // - otherwise do nothing special

        RecyclerView.ViewHolder anyViewHolder = viewHolderFactory.createChannelViewHolder(this, parent, viewType);

        return anyViewHolder;
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

    public ChannelListViewStyle getStyle() {
        return style;
    }



    public void setViewHolderFactory(ChannelViewHolderFactory viewHolderFactory) {
        this.viewHolderFactory = viewHolderFactory;
    }
}
