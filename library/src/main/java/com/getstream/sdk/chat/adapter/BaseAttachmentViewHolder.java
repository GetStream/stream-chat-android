package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

public abstract class BaseAttachmentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    private Context context;
    private Message message;
    private Attachment attachment;
    private MessageListViewStyle style;
    // Action
    private MessageListView.AttachmentClickListener clickListener;
    private MessageListView.MessageLongClickListener longClickListener;
    private MessageListItem messageListItem;

    private MessageListView.BubbleHelper bubbleHelper;

    public BaseAttachmentViewHolder(int resId, ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(resId, parent, false));
    }

    public void bind(Context context,
                     MessageListItem messageListItem,
                     Attachment attachment,
                     MessageListViewStyle style,
                     MessageListView.AttachmentClickListener clickListener,
                     MessageListView.MessageLongClickListener longClickListener) {
        this.context = context;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        this.messageListItem = messageListItem;
        this.message = messageListItem.getMessage();
        this.attachment = attachment;
        this.style = style;
    }

    public MessageListView.BubbleHelper getBubbleHelper() {
        return bubbleHelper;
    }

    public void setBubbleHelper(MessageListView.BubbleHelper bubbleHelper) {
        this.bubbleHelper = bubbleHelper;
    }

    public MessageListViewStyle getStyle() {
        return style;
    }

    public void setStyle(MessageListViewStyle style) {
        this.style = style;
    }

    public MessageListItem getMessageListItem() {
        return messageListItem;
    }

    public void setMessageListItem(MessageListItem messageListItem) {
        this.messageListItem = messageListItem;
    }

    public MessageListView.MessageLongClickListener getLongClickListener() {
        return longClickListener;
    }

    public void setLongClickListener(MessageListView.MessageLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public MessageListView.AttachmentClickListener getClickListener() {
        return clickListener;
    }

    public void setClickListener(MessageListView.AttachmentClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    @Override
    public void onClick(View v) {
        if (this.clickListener != null) {
            this.clickListener.onAttachmentClick(message, attachment);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (longClickListener != null)
            longClickListener.onMessageLongClick(message);
        return true;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}