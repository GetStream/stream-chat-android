package com.getstream.sdk.chat.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.adapter.viewholder.attachment.BaseAttachmentViewHolder;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.List;

import io.getstream.chat.android.client.models.Message;
import kotlin.collections.CollectionsKt;


public class AttachmentListItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final AttachmentViewHolderFactory factory;
    private final MessageListViewStyle style;

    private final MessageListItem.MessageItem messageListItem;
    private final Message message;
    private final List<AttachmentListItem> attachments;

    private MessageListView.AttachmentClickListener attachmentClickListener;
    private MessageListView.MessageLongClickListener longClickListener;

    public AttachmentListItemAdapter(@NonNull MessageListItem.MessageItem messageListItem,
                                     @NonNull AttachmentViewHolderFactory factory,
                                     @NonNull MessageListViewStyle style,
                                     @NonNull MessageListView.AttachmentClickListener attachmentClickListener,
                                     @NonNull MessageListView.MessageLongClickListener longClickListener
    ) {
        this.messageListItem = messageListItem;
        this.message = messageListItem.getMessage();
        this.factory = factory;
        this.attachments = CollectionsKt.map(message.getAttachments(), AttachmentListItem::new);
        this.style = style;
        this.attachmentClickListener = attachmentClickListener;
        this.longClickListener = longClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return factory.getAttachmentViewType(attachments.get(position));
    }

    @Override
    public int getItemCount() {
        return attachments.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                      int viewType) {
        return this.factory.createAttachmentViewHolder(parent, viewType, style);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AttachmentListItem attachmentItem = attachments.get(position);
        ((BaseAttachmentViewHolder) holder).bind(
                messageListItem,
                message,
                attachmentItem,
                attachmentClickListener,
                longClickListener);
    }

}
