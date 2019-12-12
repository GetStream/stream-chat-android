package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

public abstract class BaseAttachmentViewHolder extends RecyclerView.ViewHolder{

    public BaseAttachmentViewHolder(int resId, ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(resId, parent, false));
    }

    public abstract void bind(@NonNull Context context,
                              @NonNull MessageListItem messageListItem,
                              @NonNull Message message,
                              @NonNull Attachment attachment,
                              @NonNull MessageListViewStyle style,
                              @NonNull MessageListView.BubbleHelper bubbleHelper,
                              @Nullable MessageListView.AttachmentClickListener clickListener,
                              @Nullable MessageListView.MessageLongClickListener longClickListener);
}