package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelResponse;
import com.getstream.sdk.chat.utils.Global;

import java.util.ArrayList;
import java.util.List;

public class ChannelListItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<ChannelResponse> channels;
    public String filter;
    private View.OnClickListener clickListener;
    private String className;
    private int itemLayoutId;
    private View.OnLongClickListener longClickListener;

    public ChannelListItemAdapter(Context context, List<ChannelResponse> channels,
                                  String className, int itemLayoutId,
                                  View.OnClickListener clickListener, View.OnLongClickListener longClickListener) {
        this.context = context;
        this.channels = channels;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        this.className = className;
        this.itemLayoutId = itemLayoutId;
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
        return new ChannelListItemViewHolder(R.layout.list_item_channel, parent);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChannelResponse channelResponse = filterChannels(filter).get(position);
        ((BaseChannelListItemViewHolder) holder).bind(this.context, channelResponse, position, clickListener, longClickListener);
    }

    @Override
    public int getItemCount() {
        return filterChannels(filter).size();
    }


    private List<ChannelResponse> filterChannels(String channelName) {

        if (TextUtils.isEmpty(channelName)) return this.channels;

        List<ChannelResponse> channels_ = new ArrayList<>();
        for (int i = 0; i < this.channels.size(); i++) {
            ChannelResponse response = this.channels.get(i);
            Channel channel = response.getChannel();
            if (TextUtils.isEmpty(channel.getName())) {
                User opponent = Global.getOpponentUser(response);
                if (opponent != null && opponent.getName().toLowerCase().contains(channelName.toLowerCase())) {
                    channels_.add(response);
                }
            } else {
                if (channel.getName().toLowerCase().contains(channelName.toLowerCase())) {
                    channels_.add(response);
                }
            }
        }
        return channels_;
    }
}
