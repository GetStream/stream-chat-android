package io.getstream.chat.example.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.getstream.sdk.chat.adapter.BaseMessageListItemViewHolder;
import com.getstream.sdk.chat.adapter.MessageListItem;
import com.getstream.sdk.chat.adapter.MessageViewHolderFactory;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.view.AttachmentListView;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

public class CustomMessageViewHolder extends BaseMessageListItemViewHolder {

    private AttachmentListView attachmentview;

    private MessageListView.MessageClickListener messageClickListener;
    private MessageListView.MessageLongClickListener messageLongClickListener;
    private MessageListView.AttachmentClickListener attachmentClickListener;

    public CustomMessageViewHolder(int resId, ViewGroup parent) {
        super(resId, parent);
        attachmentview = itemView.findViewById(com.getstream.sdk.chat.R.id.attachmentview);
    }

    @Override
    public void bind(@NonNull Context context,
                     @NonNull ChannelState channelState,
                     @NonNull MessageListItem messageListItem,
                     @NonNull MessageListViewStyle style,
                     @NonNull MessageListView.BubbleHelper bubbleHelper,
                     @NonNull MessageViewHolderFactory factory,
                     int position){

        attachmentview.setStyle(style);
        attachmentview.setViewHolderFactory(factory);
        attachmentview.setEntity(messageListItem);
        attachmentview.setBubbleHelper(bubbleHelper);
        attachmentview.setAttachmentClickListener(attachmentClickListener);
        attachmentview.setLongClickListener(messageLongClickListener);
    }

    public void setMessageClickListener(MessageListView.MessageClickListener messageClickListener) {
        this.messageClickListener = messageClickListener;
    }

    public void setMessageLongClickListener(MessageListView.MessageLongClickListener messageLongClickListener) {
        this.messageLongClickListener = messageLongClickListener;
    }

    public void setAttachmentClickListener(MessageListView.AttachmentClickListener attachmentClickListener) {
        this.attachmentClickListener = attachmentClickListener;
    }
}