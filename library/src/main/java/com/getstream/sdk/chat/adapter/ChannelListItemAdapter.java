package com.getstream.sdk.chat.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.view.ChannelListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.ArrayList;
import java.util.List;

public class ChannelListItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = ChannelListItemAdapter.class.getSimpleName();
    private Context context;
    private List<Channel> channels;
    public String filter;
    private View.OnClickListener clickListener;
    private String className;
    private int itemLayoutId;
    private ChannelListView.Style style;
    private View.OnLongClickListener longClickListener;

    public ChannelListItemAdapter(Context context, List<Channel> channels,
                                  String className, int itemLayoutId,
                                  View.OnClickListener clickListener, View.OnLongClickListener longClickListener) {
        this.context = context;
        this.channels = channels;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        this.className = className;
        this.itemLayoutId = itemLayoutId;
    }

    public ChannelListItemAdapter(Context context, int itemLayoutId) {
        this.context = context;
        this.channels = new ArrayList<Channel>();
        this.className = className;
        this.itemLayoutId = itemLayoutId;
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

    public void addChannels(List<Channel> channelList) {
        channels.addAll(channels.size(), channelList);
        notifyDataSetChanged();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        try {
            Class tempClass = Class.forName(className);
            Class[] cArg = new Class[2];
            cArg[0] = int.class;
            cArg[1] = ViewGroup.class;
            Object obj = tempClass.getDeclaredConstructor(cArg).newInstance(itemLayoutId, parent);
            if (obj instanceof BaseChannelListItemViewHolder) {
                return ((BaseChannelListItemViewHolder) obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ChannelListItemViewHolder(R.layout.list_item_channel, parent, style);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChannelState channelState = filterChannels(filter).get(position);
        ((BaseChannelListItemViewHolder) holder).bind(this.context, channelState, position, clickListener, longClickListener);
    }

    @Override
    public int getItemCount() {
        return filterChannels(filter).size();
    }


    private List<ChannelState> filterChannels(String channelName) {

        List<ChannelState> channels_ = new ArrayList<>();

        if (TextUtils.isEmpty(channelName)) {
            for (int i = 0; i < this.channels.size(); i++) {
                Channel channel = this.channels.get(i);
                channels_.add(channel.getChannelState());
            }
        }

        for (int i = 0; i < this.channels.size(); i++) {
            Channel channel = this.channels.get(i);
            ChannelState state = channel.getChannelState();
            if (TextUtils.isEmpty(channel.getName())) {
                User opponent = Global.getOpponentUser(state);
                if (opponent != null && opponent.getName().toLowerCase().contains(channelName.toLowerCase())) {
                    channels_.add(state);
                }
            } else {
                if (channel.getName().toLowerCase().contains(channelName.toLowerCase())) {
                    channels_.add(state);
                }
            }
        }
        return channels_;
    }
}
