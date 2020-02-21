package io.getstream.chat.example.adapter;

import android.view.ViewGroup;

import com.getstream.sdk.chat.adapter.AttachmentListItemAdapter;
import com.getstream.sdk.chat.adapter.BaseAttachmentViewHolder;
import com.getstream.sdk.chat.adapter.BaseMessageListItemViewHolder;
import com.getstream.sdk.chat.adapter.MessageListItem;
import com.getstream.sdk.chat.adapter.MessageListItemAdapter;
import com.getstream.sdk.chat.adapter.MessageViewHolderFactory;
import com.getstream.sdk.chat.model.Attachment;
import io.getstream.chat.android.client.models.Message;

import java.util.List;

import io.getstream.chat.example.R;

public class CustomMessageViewHolderFactory extends MessageViewHolderFactory {
    private int CUSTOM_MEASSAGE_TYPE = 0;
    private int CUSTOM_ATTACHMENT_TYPE = 0;

    @Override
    public int getMessageViewType(MessageListItem messageListItem, Boolean mine, List<Position> positions) {
        if (isCUSTOM_MESSAGE_TYPE(messageListItem))
            return CUSTOM_MEASSAGE_TYPE;

        return super.getMessageViewType(messageListItem, mine, positions);
    }

    @Override
    public int getAttachmentViewType(Message message, Boolean mine, Position position, List<Attachment> attachments, Attachment attachment) {
        if (isCUSTOM_ATTACHMENT_TYPE(attachment))
            return CUSTOM_ATTACHMENT_TYPE;

        return super.getAttachmentViewType(message, mine, position, attachments, attachment);
    }

    public BaseMessageListItemViewHolder createMessageViewHolder(MessageListItemAdapter adapter, ViewGroup parent, int viewType) {
        if (viewType == CUSTOM_MEASSAGE_TYPE){
            CustomMessageViewHolder holder = new CustomMessageViewHolder(R.layout.list_item_message_custom, parent);
            holder.setMessageClickListener(adapter.getMessageClickListener());
            holder.setMessageLongClickListener(adapter.getMessageLongClickListener());
            holder.setAttachmentClickListener(adapter.getAttachmentClickListener());
            /*you can set more variables you need in you CustomViewHolder.*/
//            holder.setMarkdownListener(MarkdownImpl.getMarkdownListener());
//            holder.setReactionViewClickListener(adapter.getReactionViewClickListener());
//            holder.setUserClickListener(adapter.getUserClickListener());
//            holder.setReadStateClickListener(adapter.getReadStateClickListener());
//            holder.setGiphySendListener(adapter.getGiphySendListener());

            return holder;
        }

        return super.createMessageViewHolder(adapter, parent, viewType);
    }

    @Override
    public BaseAttachmentViewHolder createAttachmentViewHolder(AttachmentListItemAdapter adapter, ViewGroup parent, int viewType) {
        if (viewType == CUSTOM_ATTACHMENT_TYPE){
            CustomAttachmentViewHolder holder = new CustomAttachmentViewHolder(R.layout.list_item_attach_custom, parent);
            /*you can set more variables you need in you CustomViewHolder.*/
//            holder.setGiphySendListener(adapter.getGiphySendListener());
            return holder;
        }
        else
            return super.createAttachmentViewHolder(adapter, parent, viewType);
    }

    private boolean isCUSTOM_MESSAGE_TYPE(MessageListItem messageListItem){
        // TODO: check if messageListItem has attachamt of CUSTOM_TYPE type.
//        Message message = messageListItem.getMessage();
//        if (message == null || TextUtils.isEmpty(message.getText())) return false;
//        String text = message.getText();
//        return (text.contains("imgur"));
        return false;
    }

    private boolean isCUSTOM_ATTACHMENT_TYPE(Attachment attachment){
        // TODO: check if the attachment is CUSTOM_TYPE type.
//        return (attachment.getImageURL() != null && attachment.getImageURL().contains("imgur"));
        return false;
    }
}