package com.getstream.sdk.chat.adapter;


import android.view.ViewGroup;

import com.getstream.sdk.chat.BaseAttachmentViewHolder;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;

import java.util.List;

/**
 * Allows you to easily customize message rendering or message attachment rendering
 */
public class MessageViewHolderFactory {
    private static String TAG = MessageViewHolderFactory.class.getName();

    private static int NOT_FOUND = 0;
    private static int DATE_SEPARATOR = 1;
    private static int MESSAGE = 2;
    private static int TYPING = 3;

    private static int GENERIC_ATTACHMENT = 1;
    private static int IMAGE_ATTACHMENT = 2;
    private static int VIDEO_ATTACHMENT = 3;
    private static int FILE_ATTACHMENT = 4;


    public enum Position {
        TOP, MIDDLE, BOTTOM
    }

    public int getEntityViewType(MessageListItem messageListItem, Boolean mine, List<Position> positions) {
        // typing
        // date
        // various message types
        MessageListItemAdapter.EntityType entityType = messageListItem.getType();
        if (entityType == MessageListItemAdapter.EntityType.DATE_SEPARATOR) {
            return DATE_SEPARATOR;
        } else if (entityType == MessageListItemAdapter.EntityType.MESSAGE) {
            return MESSAGE;
        } else if (entityType == MessageListItemAdapter.EntityType.TYPING) {
            return TYPING;
        }
        return NOT_FOUND;
    }

    public int getAttachmentViewType(Message message, Boolean mine, Position position, List<Attachment> attachments, Attachment attachment) {
        // video
        // image
        // link/card layout
        // custom attachment types
        String t = attachment.getType();
        if (t == null) {
            return GENERIC_ATTACHMENT;
        } else if (t.equals(ModelType.attach_video)) {
            return VIDEO_ATTACHMENT;
        } else if (t.equals(ModelType.attach_image) ||
                t.equals(ModelType.attach_giphy)) {
            return IMAGE_ATTACHMENT;
        } else if (t.equals(ModelType.attach_file)) {
            return FILE_ATTACHMENT;
        } else {
            return GENERIC_ATTACHMENT;
        }

    }

    public BaseMessageListItemViewHolder createMessageViewHolder(MessageListItemAdapter adapter, ViewGroup parent,int viewType) {
        if (viewType == DATE_SEPARATOR) {
            DateSeparatorViewHolder holder = new DateSeparatorViewHolder(R.layout.stream_item_date_separator, parent);
            holder.setStyle(adapter.getStyle());
            return holder;
        } else if (viewType == MESSAGE) {
            MessageListItemViewHolder holder = new MessageListItemViewHolder(R.layout.stream_item_message, parent);
            holder.setViewHolderFactory(this);
            holder.setStyle(adapter.getStyle());
            holder.setGiphySendListener(adapter.getGiphySendListener());
            return holder;

        } else if (viewType == TYPING) {
            TypingIndicatorViewHolder holder = new TypingIndicatorViewHolder(R.layout.stream_item_type_indicator, parent);
            holder.setStyle(adapter.getStyle());
            return holder;
        } else {
            return null;
        }
    }

    public BaseAttachmentViewHolder createAttachmentViewHolder(AttachmentListItemAdapter adapter, ViewGroup parent, int viewType) {
        if (viewType == VIDEO_ATTACHMENT || viewType == IMAGE_ATTACHMENT) {
            AttachmentViewHolderMedia holder = new AttachmentViewHolderMedia(R.layout.stream_item_attach_media, parent);
            holder.setStyle(adapter.getStyle());
            holder.setGiphySendListener(adapter.getGiphySendListener());
            return holder;
        } else if (viewType == FILE_ATTACHMENT) {
            AttachmentViewHolderFile holder = new AttachmentViewHolderFile(R.layout.stream_item_attachment_file, parent);
            holder.setStyle(adapter.getStyle());
            return holder;
        } else {
            AttachmentViewHolder holder = new AttachmentViewHolder(R.layout.stream_item_attachment, parent);
            holder.setStyle(adapter.getStyle());
            return holder;
        }
    }
}
