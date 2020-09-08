package com.getstream.sdk.chat.adapter;

import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.view.MessageListView;

import io.getstream.chat.android.client.models.Channel;

public class ThreadSeparatorViewHolder extends BaseMessageListItemViewHolder {

    private final TextView tv_text;

    public ThreadSeparatorViewHolder(int resId, ViewGroup viewGroup) {
        super(resId, viewGroup);
        tv_text = itemView.findViewById(R.id.tv_text);
    }

    @Override
    public void bind(@NonNull Channel channelState,
                     @NonNull MessageListItem messageListItem,
                     @NonNull MessageListView.BubbleHelper bubbleHelper,
                     @NonNull MessageViewHolderFactory factory,
                     int position) {
//        tv_text.setText();
    }
}
