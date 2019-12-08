package io.getstream.chat.example.adapter;

import android.view.ViewGroup;

import com.getstream.sdk.chat.adapter.AttachmentListItemAdapter;
import com.getstream.sdk.chat.adapter.BaseAttachmentViewHolder;
import com.getstream.sdk.chat.adapter.BaseMessageListItemViewHolder;
import com.getstream.sdk.chat.adapter.MessageListItem;
import com.getstream.sdk.chat.adapter.MessageListItemAdapter;
import com.getstream.sdk.chat.adapter.MessageViewHolderFactory;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.rest.Message;

import java.util.List;

import io.getstream.chat.example.R;

public class CustomMessageViewHolderFactory extends MessageViewHolderFactory {
    private static int CUSTOM_TYPE = 0;

    @Override
    public int getMessageViewType(MessageListItem messageListItem, Boolean mine, List<Position> positions) {
        if (isCUSTOM_TYPE(messageListItem))
            return CUSTOM_TYPE;

        return super.getMessageViewType(messageListItem, mine, positions);
    }

    @Override
    public int getAttachmentViewType(Message message, Boolean mine, Position position, List<Attachment> attachments, Attachment attachment) {
        if (isCUSTOM_TYPE(attachment))
            return CUSTOM_TYPE;

        return super.getAttachmentViewType(message, mine, position, attachments, attachment);
    }

    public BaseMessageListItemViewHolder createMessageViewHolder(MessageListItemAdapter adapter, ViewGroup parent, int viewType) {
        if (viewType == CUSTOM_TYPE) {
            CustomMessageViewHolder holder = new CustomMessageViewHolder(R.layout.list_item_message_custom, parent);
            return holder;
        }
        return super.createMessageViewHolder(adapter, parent, viewType);
    }

    @Override
    public BaseAttachmentViewHolder createAttachmentViewHolder(AttachmentListItemAdapter adapter, ViewGroup parent, int viewType) {
        BaseAttachmentViewHolder holder;
        if (viewType == CUSTOM_TYPE) {
            holder = new AttachmentViewHolderCustom(R.layout.list_item_attach_custom, parent);
        } else {
            holder = super.createAttachmentViewHolder(adapter, parent, viewType);
        }
        return holder;
    }

    private boolean isCUSTOM_TYPE(Attachment attachment){
        // TODO: check if the attachment is CUSTOM_TYPE type.
        return (attachment.getImageURL() != null && attachment.getImageURL().indexOf("imgur") != -1) ;
    }

    private boolean isCUSTOM_TYPE(MessageListItem messageListItem){
        // TODO: check if messageListItem has attachamt of CUSTOM_TYPE type.
        return false;
    }
}