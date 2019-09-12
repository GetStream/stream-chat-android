package com.getstream.sdk.chat.adapter;


import android.view.ViewGroup;

import com.getstream.sdk.chat.BaseAttachmentViewHolder;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.enums.MessageListItemType;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;

import java.util.List;

/**
 * Allows you to easily customize message rendering or message attachment rendering
 */
public class MessageViewHolderFactory {
    private static String TAG = MessageViewHolderFactory.class.getName();


    private static int GENERIC_ATTACHMENT = 1;
    private static int IMAGE_ATTACHMENT = 2;
    private static int VIDEO_ATTACHMENT = 3;
    private static int FILE_ATTACHMENT = 4;

    public MessageListItemType getEntityViewType(MessageListItem messageListItem, Boolean mine, List<Position> positions) {
        // typing
        // date
        // various message types
        MessageListItemType messageListItemType = messageListItem.getType();
        if (messageListItemType == MessageListItemType.DATE_SEPARATOR) {
            return MessageListItemType.DATE_SEPARATOR;
        } else if (messageListItemType == MessageListItemType.MESSAGE) {
            return MessageListItemType.MESSAGE;
        } else if (messageListItemType == MessageListItemType.TYPING) {
            return MessageListItemType.TYPING;
        } else if (messageListItemType == MessageListItemType.THREAD_SEPARATOR) {
            return MessageListItemType.THREAD_SEPARATOR;
        }
        return MessageListItemType.NOT_FOUND;
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

    public BaseMessageListItemViewHolder createMessageViewHolder(MessageListItemAdapter adapter, ViewGroup parent, MessageListItemType viewType) {
        if (viewType == MessageListItemType.DATE_SEPARATOR) {
            DateSeparatorViewHolder holder = new DateSeparatorViewHolder(R.layout.stream_item_date_separator, parent);
            holder.setStyle(adapter.getStyle());
            return holder;
        } else if (viewType == MessageListItemType.MESSAGE) {
            MessageListItemViewHolder holder = new MessageListItemViewHolder(R.layout.stream_item_message, parent);
            holder.setViewHolderFactory(this);
            holder.setStyle(adapter.getStyle());
            holder.setGiphySendListener(adapter.getGiphySendListener());
            return holder;

        } else if (viewType == MessageListItemType.TYPING) {
            TypingIndicatorViewHolder holder = new TypingIndicatorViewHolder(R.layout.stream_item_type_indicator, parent);
            holder.setStyle(adapter.getStyle());
            return holder;
        } else if (viewType == MessageListItemType.THREAD_SEPARATOR) {
            ThreadSeparatorViewHolder holder = new ThreadSeparatorViewHolder(R.layout.stream_item_thread_separator, parent);
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

    public enum Position {
        TOP, MIDDLE, BOTTOM
    }
}
