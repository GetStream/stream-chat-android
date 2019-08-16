package com.getstream.sdk.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.view.AttachmentListView;
import com.getstream.sdk.chat.view.MessageListView;

public abstract class BaseAttachmentViewHolder extends RecyclerView.ViewHolder {


    public BaseAttachmentViewHolder(int resId, ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(resId, parent, false));
    }

    public abstract void bind(Context context, Message message, Attachment attachment, MessageListView.AttachmentClickListener clickListener);
}