package com.getstream.sdk.chat.adapter;

import android.content.Context;
import androidx.annotation.NonNull;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.view.ViewGroup;

import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.utils.EntityListDiffCallback;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.ArrayList;
import java.util.List;

public class MessageListItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    public void setBubbleHelper(MessageListView.BubbleHelper bubbleHelper) {
        this.bubbleHelper = bubbleHelper;
    }

    public enum EntityType {
        DATE_SEPARATOR, MESSAGE, TYPING
    }

    private final String TAG = MessageListItemAdapter.class.getSimpleName();

    private ChannelState channelState;
    private MessageListView.MessageClickListener messageClickListener;
    private MessageListView.MessageLongClickListener messageLongClickListener;
    private MessageListView.AttachmentClickListener attachmentClickListener;
    private List<MessageListItem> messageListItemList;
    private boolean isThread;
    private MessageListViewStyle style;

    private Context context;
    private String className;
    private int itemLayoutId;
    private MessageViewHolderFactory viewHolderFactory;
    private MessageListView.BubbleHelper bubbleHelper;

    public MessageListViewStyle getStyle() {
        return style;
    }

    public void setFactory(MessageViewHolderFactory factory) {
        this.viewHolderFactory = factory;
    }

    public MessageListItemAdapter(Context context, ChannelState channelState, @NonNull List<MessageListItem> messageListItemList) {
        this.context = context;
        this.viewHolderFactory = new MessageViewHolderFactory();
        this.channelState = channelState;
        this.messageListItemList = messageListItemList;
    }


    public MessageListItemAdapter(Context context, ChannelState channelState, @NonNull List<MessageListItem> messageListItemList, MessageViewHolderFactory factory) {
        this.context = context;
        this.channelState = channelState;
        this.messageListItemList = messageListItemList;
        this.viewHolderFactory = factory;
    }


    public MessageListItemAdapter(Context context) {
        this.context = context;
        this.viewHolderFactory = new MessageViewHolderFactory();
        this.messageListItemList = new ArrayList<>();
    }

    public void setStyle(MessageListViewStyle s) {
        style = s;
    }

    public void replaceEntities(List<MessageListItem> newEntities) {
        final DiffUtil.DiffResult result = DiffUtil.calculateDiff(
                new EntityListDiffCallback(messageListItemList, newEntities), true);
        messageListItemList = newEntities;
        // only update those rows that change...
        result.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemViewType(int position) {
        try {
            MessageListItem messageListItem = messageListItemList.get(position);
            return viewHolderFactory.getEntityViewType(messageListItem, messageListItem.isMine(), messageListItem.getPositions());
        } catch(IndexOutOfBoundsException e) {
            return 0;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        BaseMessageListItemViewHolder holder = this.viewHolderFactory.createMessageViewHolder(this, parent, viewType);
        return holder;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MessageListItem messageListItem = messageListItemList.get(position);
        ((BaseMessageListItemViewHolder) holder).setBubbleHelper(bubbleHelper);
        ((BaseMessageListItemViewHolder) holder).bind(this.context,
                this.channelState,
                messageListItem,
                position, isThread,
                messageClickListener,
                messageLongClickListener,
                attachmentClickListener);


    }
    public void setChannelState(ChannelState channelState) {
        this.channelState = channelState;
    }
    public void setMessageClickListener(MessageListView.MessageClickListener messageClickListener) {
        this.messageClickListener = messageClickListener;
    }
    public void setMessageLongClickListener(MessageListView.MessageLongClickListener messageLongClickListener){
        this.messageLongClickListener = messageLongClickListener;
    }
    public void setAttachmentClickListener(MessageListView.AttachmentClickListener attachmentClickListener) {
        this.attachmentClickListener = attachmentClickListener;
    }

    @Override
    public int getItemCount() {
        return messageListItemList.size();
    }
}
