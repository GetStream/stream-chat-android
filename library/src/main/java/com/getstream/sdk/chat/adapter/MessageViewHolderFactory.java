package com.getstream.sdk.chat.adapter;


import android.view.ViewGroup;

import com.getstream.sdk.chat.BaseAttachmentViewHolder;
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

    public int getEntityViewType(Entity entity, Boolean mine, Position position) {
        // typing
        // date
        // various message types
        MessageListItemAdapter.EntityType entityType = entity.getType();
        if (entityType == MessageListItemAdapter.EntityType.DATE_SEPARATOR) {
            return 1;
        } else if (entityType == MessageListItemAdapter.EntityType.MESSAGE) {
            return 2;
        } else if (entityType == MessageListItemAdapter.EntityType.TYPING) {
            return 3;
        }
        return 0;
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
        holder.setViewHolderFactory(this);
        holder.setStyle(adapter.getStyle());

        return holder;
    }

    public BaseAttachmentViewHolder createAttachmentViewHolder(AttachmentListItemAdapter adapter, ViewGroup parent, int viewType) {
        AttachmentViewHolder holder = new AttachmentViewHolder(R.layout.list_item_attachment, parent);
        holder.setStyle(adapter.getStyle());
        return holder;
    }



}
