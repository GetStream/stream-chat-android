package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.utils.MessageListItemDiffCallback;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import org.jetbrains.annotations.NotNull;

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
    private MessageListView.UserClickListener userClickListener;
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

    @Override
    public long getItemId(int position) {
        return messageListItemList.get(position).getStableID();
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
                new MessageListItemDiffCallback(messageListItemList, newEntities), true);

        // only update those rows that change...
        result.dispatchUpdatesTo(this);
        messageListItemList = newEntities;

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

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent,
                                                      int viewType) {
        return this.viewHolderFactory.createMessageViewHolder(this, parent, viewType);
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
                attachmentClickListener,
                userClickListener);


    }
    public void setChannelState(ChannelState channelState) {
        this.channelState = channelState;
    }
    public void setMessageClickListener(MessageListView.MessageClickListener messageClickListener) {
        if (style.isEnableReaction())
            this.messageClickListener = messageClickListener;
    }
    public void setMessageLongClickListener(MessageListView.MessageLongClickListener messageLongClickListener){
        this.messageLongClickListener = messageLongClickListener;
    }
    public void setAttachmentClickListener(MessageListView.AttachmentClickListener attachmentClickListener) {
        this.attachmentClickListener = attachmentClickListener;
    }

    public void setUserClickListener(MessageListView.UserClickListener userClickListener) {
        this.userClickListener = userClickListener;
    }

    @Override
    public int getItemCount() {

        return messageListItemList.size();
    }
}