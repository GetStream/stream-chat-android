package io.getstream.chat.example;
import android.view.ViewGroup;

import com.getstream.sdk.chat.adapter.BaseAttachmentViewHolder;
import com.getstream.sdk.chat.adapter.AttachmentListItemAdapter;
import com.getstream.sdk.chat.adapter.MessageViewHolderFactory;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.rest.Message;

import java.util.List;


public class MyMessageViewHolderFactory extends MessageViewHolderFactory {
    private static int IMGUR_TYPE = 0;

    @Override
    public int getAttachmentViewType(Message message, Boolean mine, Position position, List<Attachment> attachments, Attachment attachment) {
        if (attachment.getImageURL() != null && attachment.getImageURL().indexOf("imgur") != -1) {
            return IMGUR_TYPE;
        }
        return super.getAttachmentViewType(message, mine, position, attachments, attachment);
    }

    @Override
    public BaseAttachmentViewHolder createAttachmentViewHolder(AttachmentListItemAdapter adapter, ViewGroup parent, int viewType) {
        BaseAttachmentViewHolder holder;
        if (viewType == IMGUR_TYPE) {
            holder = new AttachmentViewHolderImgur(R.layout.list_item_attach_imgur, parent);
        } else {
            holder = super.createAttachmentViewHolder(adapter, parent, viewType);
        }


        return holder;
    }
}
