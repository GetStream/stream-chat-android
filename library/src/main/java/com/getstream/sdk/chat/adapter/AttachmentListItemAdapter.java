package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.List;

import io.getstream.chat.android.client.models.Attachment;
import io.getstream.chat.android.client.models.Message;


public class AttachmentListItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = AttachmentListItemAdapter.class.getSimpleName();

    private Message message;
    private MessageViewHolderFactory factory;
    private Context context;
    private MessageListItem.MessageItem messageListItem;
    private List<Attachment> attachments;
    private MessageListViewStyle style;
    private MessageListView.AttachmentClickListener attachmentClickListener;
    private MessageListView.MessageLongClickListener longClickListener;
    private MessageListView.GiphySendListener giphySendListener;
    private MessageListView.BubbleHelper bubbleHelper;


    public AttachmentListItemAdapter(Context context, @NonNull MessageListItem.MessageItem messageListItem, @NonNull MessageViewHolderFactory factory) {
        this.context = context;
        this.messageListItem = messageListItem;
        this.message = messageListItem.getMessage();
        this.factory = factory;
        this.attachments = message.getAttachments();
    }

    @Override
    public int getItemViewType(int position) {
        try {
            Attachment attachment = attachments.get(position);
            return factory.getAttachmentViewType(attachment);
        } catch (IndexOutOfBoundsException e) {
            return 0;
        }
    }

    @Override
    public int getItemCount() {
        return attachments.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                      int viewType) {
        return this.factory.createAttachmentViewHolder(this, parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Attachment attachment = attachments.get(position);
        ((BaseAttachmentViewHolder) holder).bind(
                context,
                messageListItem,
                message,
                attachment,
                style,
                bubbleHelper,
                attachmentClickListener,
                longClickListener);
    }

    public void setStyle(MessageListViewStyle style) {
        this.style = style;
    }

    public void setAttachmentClickListener(MessageListView.AttachmentClickListener attachmentClickListener) {
        this.attachmentClickListener = attachmentClickListener;
    }

    public void setLongClickListener(MessageListView.MessageLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public MessageListView.GiphySendListener getGiphySendListener() {
        return giphySendListener;
    }

    public void setGiphySendListener(MessageListView.GiphySendListener giphySendListener) {
        this.giphySendListener = giphySendListener;
    }

    public void setBubbleHelper(MessageListView.BubbleHelper bubbleHelper) {
        this.bubbleHelper = bubbleHelper;
    }
}
