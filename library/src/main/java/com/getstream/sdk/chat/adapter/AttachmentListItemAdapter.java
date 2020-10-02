package com.getstream.sdk.chat.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.adapter.viewholder.attachment.BaseAttachmentViewHolder;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.List;

import kotlin.collections.CollectionsKt;


public class AttachmentListItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final AttachmentViewHolderFactory factory;
    private final MessageListViewStyle style;

    private final MessageListItem.MessageItem messageListItem;
    private final List<AttachmentListItem> attachments;

    public AttachmentListItemAdapter(@NonNull MessageListItem.MessageItem messageListItem,
                                     @NonNull AttachmentViewHolderFactory factory,
                                     @NonNull MessageListViewStyle style
    ) {
        this.factory = factory;
        this.messageListItem = messageListItem;
        this.attachments = CollectionsKt.map(messageListItem.getMessage().getAttachments(), AttachmentListItem::new);
        this.style = style;
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
        return this.factory.createAttachmentViewHolder(parent, viewType, style, messageListItem);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AttachmentListItem attachmentItem = attachments.get(position);
        ((BaseAttachmentViewHolder) holder).bind(attachmentItem);
    }

}
