package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.BaseAttachmentViewHolder;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.view.AttachmentListView;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.List;


public class AttachmentListItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = AttachmentListItemAdapter.class.getSimpleName();

    private Message message;
    private MessageViewHolderFactory factory;
    private Context context;
    private Entity entity;
    private List<Attachment> attachments;
    private MessageListViewStyle style;
    private MessageListView.AttachmentClickListener attachmentClickListener;
    private MessageListView.BubbleHelper bubbleHelper;


    public AttachmentListItemAdapter(Context context, @NonNull  Entity entity,@NonNull  MessageViewHolderFactory factory) {
        this.context = context;
        this.entity = entity;
        this.message = entity.getMessage();
        this.factory = factory;
        this.attachments = message.getAttachments();
    }

    @Override
    public int getItemViewType(int position) {
        try {
            Attachment attachment = attachments.get(position);
            return factory.getAttachmentViewType(message, true, MessageViewHolderFactory.Position.BOTTOM, attachments, attachment);
        } catch(IndexOutOfBoundsException e) {
            return 0;
        }
    }

    @Override
    public int getItemCount() {
        return attachments.size();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        BaseAttachmentViewHolder holder = this.factory.createAttachmentViewHolder(this, parent, viewType);
        return holder;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Attachment attachment = attachments.get(position);
        ((BaseAttachmentViewHolder) holder).setBubbleHelper(bubbleHelper);
        ((BaseAttachmentViewHolder) holder).bind(this.context, entity, attachment, attachmentClickListener);
    }

    public MessageListViewStyle getStyle() {
        return style;
    }

    public void setStyle(MessageListViewStyle style) {
        this.style = style;
    }

    public void setAttachmentClickListener(MessageListView.AttachmentClickListener attachmentClickListener) {
        this.attachmentClickListener = attachmentClickListener;
    }

    public MessageListView.BubbleHelper getBubbleHelper() {
        return bubbleHelper;
    }

    public void setBubbleHelper(MessageListView.BubbleHelper bubbleHelper) {
        this.bubbleHelper = bubbleHelper;
    }
}
