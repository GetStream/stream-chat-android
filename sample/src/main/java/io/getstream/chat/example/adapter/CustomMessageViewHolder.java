package io.getstream.chat.example.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.getstream.sdk.chat.adapter.BaseMessageListItemViewHolder;
import com.getstream.sdk.chat.adapter.MessageListItem;
import com.getstream.sdk.chat.adapter.MessageViewHolderFactory;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.view.AttachmentListView;

public class CustomMessageViewHolder extends BaseMessageListItemViewHolder {

    private AttachmentListView attachmentListView;
    private MessageViewHolderFactory viewHolderFactory;

    public CustomMessageViewHolder(int resId, ViewGroup parent) {
        super(resId, parent);
        attachmentListView = itemView.findViewById(com.getstream.sdk.chat.R.id.attachmentview);
    }

    @Override
    public void bind(Context context, ChannelState channelState, @NonNull MessageListItem messageListItem, int position) {
        attachmentListView.setViewHolderFactory(viewHolderFactory);
        attachmentListView.setBubbleHelper(bubbleHelper);
        attachmentListView.setEntity(messageListItem);
    }

    @Override
    public void setViewHolderFactory(MessageViewHolderFactory factory) {
        this.viewHolderFactory = factory;
    }
}