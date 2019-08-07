package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.support.annotation.NonNull;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.response.ChannelState;

import java.util.List;

public class MessageListItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String TAG = MessageListItemAdapter.class.getSimpleName();

    private ChannelState channelState;
    private View.OnClickListener clickListener;
    private View.OnLongClickListener longClickListener;
    private List<Message> messageList;
    private boolean isThread;
    private Context context;
    private String className;
    private int itemLayoutId;

    public MessageListItemAdapter(Context context, ChannelState channelState, @NonNull List<Message> messageList,
                                  boolean isThread, String className, int itemLayoutId,
                                  View.OnClickListener clickListener, View.OnLongClickListener longClickListener
                                  ) {
        this.context = context;
        this.channelState = channelState;
        this.messageList = messageList;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        this.isThread = isThread;
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
            if (obj instanceof BaseMessageListItemViewHolder) {
                return ((BaseMessageListItemViewHolder) obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new MessageListItemViewHolder(R.layout.list_item_message, parent);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((BaseMessageListItemViewHolder) holder).bind(this.context, this.channelState, messageList, position, isThread, clickListener, longClickListener);

    }

    @Override
    public int getItemCount() {
        if (messageList != null) {
            return messageList.size() + 1;
        } else {
            return 0;
        }
    }
}
