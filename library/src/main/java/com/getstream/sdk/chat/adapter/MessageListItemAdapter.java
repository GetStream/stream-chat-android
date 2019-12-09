package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MessageListItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String TAG = MessageListItemAdapter.class.getSimpleName();
    private ChannelState channelState;
    private MessageListView.MessageClickListener messageClickListener;
    private MessageListView.MessageLongClickListener messageLongClickListener;
    private MessageListView.AttachmentClickListener attachmentClickListener;
    private MessageListView.ReactionViewClickListener reactionViewClickListener;
    private MessageListView.UserClickListener userClickListener;
    private MessageListView.ReadStateClickListener readStateClickListener;
    private MessageListView.GiphySendListener giphySendListener;
    private List<MessageListItem> messageListItemList;
    private boolean isThread;
    private MessageListViewStyle style;
    private Context context;
    private String className;
    private int itemLayoutId;
    private MessageViewHolderFactory viewHolderFactory;
    private MessageListView.BubbleHelper bubbleHelper;

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

    public void setBubbleHelper(MessageListView.BubbleHelper bubbleHelper) {
        this.bubbleHelper = bubbleHelper;
    }

    public void setFactory(MessageViewHolderFactory factory) {
        this.viewHolderFactory = factory;
    }

    @Override
    public long getItemId(int position) {
        return messageListItemList.get(position).getStableID();
    }

    public MessageListViewStyle getStyle() {
        return style;
    }

    public void setStyle(MessageListViewStyle s) {
        style = s;
    }

    public MessageListView.GiphySendListener getGiphySendListener() {
        return giphySendListener;
    }

    public void setGiphySendListener(MessageListView.GiphySendListener giphySendListener) {
        this.giphySendListener = giphySendListener;
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
            return viewHolderFactory.getMessageViewType(messageListItem, messageListItem.isMine(), messageListItem.getPositions());
        } catch (IndexOutOfBoundsException e) {
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
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, int position) {
        MessageListItem messageListItem = messageListItemList.get(position);
        ((BaseMessageListItemViewHolder) holder).bind(this.context,
                this.channelState,
                messageListItem,
                style,
                bubbleHelper,
                viewHolderFactory,
                position);
    }

    public void setChannelState(ChannelState channelState) {
        this.channelState = channelState;
    }

    public boolean isThread() {
        return isThread;
    }

    public void setThread(boolean thread) {
        isThread = thread;
    }

    public MessageListView.MessageClickListener getMessageClickListener() {
        return messageClickListener;
    }

    public void setMessageClickListener(MessageListView.MessageClickListener messageClickListener) {
        if (style.isReactionEnabled())
            this.messageClickListener = messageClickListener;
    }

    public MessageListView.MessageLongClickListener getMessageLongClickListener() {
        return messageLongClickListener;
    }

    public void setMessageLongClickListener(MessageListView.MessageLongClickListener messageLongClickListener) {
        this.messageLongClickListener = messageLongClickListener;
    }

    public MessageListView.AttachmentClickListener getAttachmentClickListener() {
        return attachmentClickListener;
    }

    public void setAttachmentClickListener(MessageListView.AttachmentClickListener attachmentClickListener) {
        this.attachmentClickListener = attachmentClickListener;
    }

    public MessageListView.ReactionViewClickListener getReactionViewClickListener() {
        return reactionViewClickListener;
    }

    public void setReactionViewClickListener(MessageListView.ReactionViewClickListener l) {
        this.reactionViewClickListener = l;
    }

    public MessageListView.UserClickListener getUserClickListener() {
        return userClickListener;
    }

    public void setUserClickListener(MessageListView.UserClickListener userClickListener) {
        this.userClickListener = userClickListener;
    }

    public MessageListView.ReadStateClickListener getReadStateClickListener() {
        return readStateClickListener;
    }

    public void setReadStateClickListener(MessageListView.ReadStateClickListener readStateClickListener) {
        this.readStateClickListener = readStateClickListener;
    }

    @Override
    public int getItemCount() {
        return messageListItemList.size();
    }
}
