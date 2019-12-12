package com.getstream.sdk.chat.adapter;


import android.view.ViewGroup;

import com.getstream.sdk.chat.MarkdownImpl;
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

    public static final int MESSAGEITEM_DATE_SEPARATOR = 1;
    public static final int MESSAGEITEM_MESSAGE = 2;
    public static final int MESSAGEITEM_TYPING = 3;
    public static final int MESSAGEITEM_THREAD_SEPARATOR = 4;
    public static final int MESSAGEITEM_NOT_FOUND = 5;

    public static final int GENERIC_ATTACHMENT = 1;
    public static final int IMAGE_ATTACHMENT = 2;
    public static final int VIDEO_ATTACHMENT = 3;
    public static final int FILE_ATTACHMENT = 4;

    public int getMessageViewType(MessageListItem messageListItem, Boolean mine, List<Position> positions) {
        // typing
        // date
        // various message types
        int messageListItemType = messageListItem.getType();
        if (messageListItemType == MESSAGEITEM_DATE_SEPARATOR) {
            return MESSAGEITEM_DATE_SEPARATOR;
        } else if (messageListItemType == MESSAGEITEM_MESSAGE) {
            return MESSAGEITEM_MESSAGE;
        } else if (messageListItemType == MESSAGEITEM_TYPING) {
            return MESSAGEITEM_TYPING;
        } else if (messageListItemType == MESSAGEITEM_THREAD_SEPARATOR) {
            return MESSAGEITEM_THREAD_SEPARATOR;
        }
        return MESSAGEITEM_NOT_FOUND;
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

    public BaseMessageListItemViewHolder createMessageViewHolder(MessageListItemAdapter adapter, ViewGroup parent, int viewType) {
        if (viewType == MESSAGEITEM_DATE_SEPARATOR) {
            DateSeparatorViewHolder holder = new DateSeparatorViewHolder(R.layout.stream_item_date_separator, parent);
            return holder;
        } else if (viewType == MESSAGEITEM_MESSAGE) {
            MessageListItemViewHolder holder = new MessageListItemViewHolder(R.layout.stream_item_message, parent);
            holder.setMarkdownListener(MarkdownImpl.getMarkdownListener());
            holder.setMessageClickListener(adapter.getMessageClickListener());
            holder.setMessageLongClickListener(adapter.getMessageLongClickListener());
            holder.setAttachmentClickListener(adapter.getAttachmentClickListener());
            holder.setReactionViewClickListener(adapter.getReactionViewClickListener());
            holder.setUserClickListener(adapter.getUserClickListener());
            holder.setReadStateClickListener(adapter.getReadStateClickListener());
            holder.setGiphySendListener(adapter.getGiphySendListener());
            return holder;
        } else if (viewType == MESSAGEITEM_TYPING) {
            TypingIndicatorViewHolder holder = new TypingIndicatorViewHolder(R.layout.stream_item_type_indicator, parent);
            return holder;
        } else if (viewType == MESSAGEITEM_THREAD_SEPARATOR) {
            ThreadSeparatorViewHolder holder = new ThreadSeparatorViewHolder(R.layout.stream_item_thread_separator, parent);
            return holder;
        } else {
            return null;
        }
    }

    public BaseAttachmentViewHolder createAttachmentViewHolder(AttachmentListItemAdapter adapter, ViewGroup parent, int viewType) {
        if (viewType == VIDEO_ATTACHMENT || viewType == IMAGE_ATTACHMENT) {
            AttachmentViewHolderMedia holder = new AttachmentViewHolderMedia(R.layout.stream_item_attach_media, parent);
            holder.setGiphySendListener(adapter.getGiphySendListener());
            return holder;
        } else if (viewType == FILE_ATTACHMENT) {
            AttachmentViewHolderFile holder = new AttachmentViewHolderFile(R.layout.stream_item_attachment_file, parent);
            return holder;
        } else {
            AttachmentViewHolder holder = new AttachmentViewHolder(R.layout.stream_item_attachment, parent);
            return holder;
        }
    }

    public enum Position {
        TOP, MIDDLE, BOTTOM
    }
}
