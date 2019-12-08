package io.getstream.chat.example.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.getstream.sdk.chat.adapter.BaseMessageListItemViewHolder;
import com.getstream.sdk.chat.adapter.MessageListItem;
import com.getstream.sdk.chat.adapter.MessageViewHolderFactory;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.view.AttachmentListView;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

public class CustomMessageViewHolder extends BaseMessageListItemViewHolder {

    private AttachmentListView attachmentListView;
    private MessageViewHolderFactory viewHolderFactory;

    public CustomMessageViewHolder(int resId, ViewGroup parent) {
        super(resId, parent);
        attachmentListView = itemView.findViewById(com.getstream.sdk.chat.R.id.attachmentview);
    }

    @Override
    public void bind(@NonNull Context context,
                     @NonNull ChannelState channelState,
                     @NonNull MessageListItem messageListItem,
                     @NonNull MessageListViewStyle style,
                     @NonNull MessageListView.BubbleHelper bubbleHelper,
                     @Nullable MessageViewHolderFactory factory,
                     int position){
        attachmentListView.setViewHolderFactory(factory);
        attachmentListView.setBubbleHelper(bubbleHelper);
        attachmentListView.setEntity(messageListItem);
        attachmentListView.setStyle(style);
        attachmentListView.setBubbleHelper(bubbleHelper);
    }
}