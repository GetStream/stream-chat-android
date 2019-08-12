package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.view.ChannelListView;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class ChannelListItemAdapter<T extends BaseChannelListItemViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = ChannelListItemAdapter.class.getSimpleName();

    private Context context;
    private List<Channel> channels; // cached list of channels
    public String filter;
    private Class<? extends RecyclerView.ViewHolder> viewHolderWrapper;
    private ChannelListView.ChannelClickListener channelClickListener;
    private ChannelListView.ChannelClickListener channelLongClickListener;
    private ChannelListView.UserClickListener userClickListener;
    private ChannelListView.Style style;

    public ChannelListItemAdapter(Context context, List<Channel> channels) {
        this.context = context;
        this.channels = channels;
        this.viewHolderWrapper = ChannelListItemViewHolder.class;
    }

    public void setCustomViewHolder(Class<? extends RecyclerView.ViewHolder> wrapper) {
        this.viewHolderWrapper = wrapper;
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
        // allow users of this library to use any view holder they want...
        // a subclass of BaseChannelListItemViewHolder is supported, or a completely custom ViewHolder...
        // - if it extends baseChannelListItemView holder apply the click listeners and style;
        // - otherwise do nothing special
        View v = LayoutInflater.from(parent.getContext()).inflate(style.channelPreviewLayout, parent, false);
        try {
            Constructor<? extends RecyclerView.ViewHolder> constructor = viewHolderWrapper.getDeclaredConstructor(View.class);
            constructor.setAccessible(true);
            RecyclerView.ViewHolder anyViewHolder = constructor.newInstance(v);
            if (anyViewHolder instanceof BaseChannelListItemViewHolder) {
                BaseChannelListItemViewHolder channelViewHolder = (BaseChannelListItemViewHolder) anyViewHolder;

                channelViewHolder.setChannelClickListener(this.channelClickListener);
                channelViewHolder.setChannelLongClickListener(this.channelLongClickListener);
                channelViewHolder.setUserClickListener(this.userClickListener);
                channelViewHolder.setStyle(style);
                return channelViewHolder;
            } else {
                return anyViewHolder;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
