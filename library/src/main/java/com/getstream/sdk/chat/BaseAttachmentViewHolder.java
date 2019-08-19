package com.getstream.sdk.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.adapter.MessageListItem;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.view.MessageListView;

public abstract class BaseAttachmentViewHolder extends RecyclerView.ViewHolder {

    private MessageListView.BubbleHelper bubbleHelper;

    public BaseAttachmentViewHolder(int resId, ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(resId, parent, false));
    }

    public abstract void bind(Context context,
                              MessageListItem messageListItem,
                              Attachment attachment,
                              MessageListView.AttachmentClickListener clickListener,
                              MessageListView.MessageLongClickListener longClickListener);

    public MessageListView.BubbleHelper getBubbleHelper() {
        return bubbleHelper;
    }

    public void setBubbleHelper(MessageListView.BubbleHelper bubbleHelper) {
        this.bubbleHelper = bubbleHelper;
    }
}