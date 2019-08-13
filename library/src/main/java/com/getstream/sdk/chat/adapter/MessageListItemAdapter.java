package com.getstream.sdk.chat.adapter;

import android.content.Context;
import androidx.annotation.NonNull;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.utils.ChannelListDiffCallback;
import com.getstream.sdk.chat.utils.MessageListDiffCallback;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MessageListItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String TAG = MessageListItemAdapter.class.getSimpleName();

    private ChannelState channelState;
    private View.OnClickListener clickListener;
    private View.OnLongClickListener longClickListener;
    private List<Message> messageList;
    private boolean isThread;
    private MessageListViewStyle style;
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


    public MessageListItemAdapter(Context context) {
        this.context = context;
    }

    public void setStyle(MessageListViewStyle s) {
        style = s;
    }



    public void addNewMessage(Message message) {
        Log.i(TAG, "MessageListItem adapter addNewMessage called");
        int position = messageList.size();
        messageList.add(position, message);
        notifyItemInserted(position);

        // TODO: scroll to the bottom if the user isn't currently scrolling up. if the user is scrolling up set a new message indicator

    }

    public void replaceMessages(List<Message> newMessages) {
        final DiffUtil.DiffResult result = DiffUtil.calculateDiff(
                new MessageListDiffCallback(messageList, newMessages), true);
        messageList = newMessages;
        // only update those rows that change...
        result.dispatchUpdatesTo(this);
    }


    public boolean addOldMessages(List<Message> messages) {
        Log.i(TAG, "MessageListItem adapter addOldMessages");
        messageList.addAll(0, messages);
        // TODO: scroll to maintain the original scroll position
        return false;
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
            //e.printStackTrace();
        }
        return new MessageListItemViewHolder(R.layout.list_item_message, parent, style);
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
