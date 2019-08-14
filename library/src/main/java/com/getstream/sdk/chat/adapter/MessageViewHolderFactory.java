package com.getstream.sdk.chat.adapter;


import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.rest.Message;

import java.util.List;

/**
 * Allows you to easily customize message rendering or message attachment rendering
 */
public class MessageViewHolderFactory {

    public enum Position {
        TOP, MIDDLE, BOTTOM
    }

    public int getMessageViewType(Message message, Boolean mine, Position position) {
        // typing
        // date
        // various message types
        return 1;
    }

    public int getAttachmentViewType(Message message, Boolean mine, Position position, List<Attachment> attachments, Attachment attachment) {
        // video
        // image
        // link/card layout
        // custom attachment types
        return 1;
    }

    public BaseMessageListItemViewHolder createMessageViewHolder(MessageListItemAdapter adapter, ViewGroup parent,int viewType) {
        MessageListItemViewHolder holder = new MessageListItemViewHolder(R.layout.list_item_message, parent);
        holder.setStyle(adapter.getStyle());
        return holder;
    }

//    public RecyclerView.ViewHolder createAttachmentViewHolder(MessageListItemAdapter adapter, ViewGroup parent,int viewType) {
//        // TODO
//    }



}
