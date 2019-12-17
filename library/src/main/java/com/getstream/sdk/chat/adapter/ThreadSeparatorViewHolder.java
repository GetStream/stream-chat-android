package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

public class ThreadSeparatorViewHolder extends BaseMessageListItemViewHolder {

    private TextView tv_text;
    public ThreadSeparatorViewHolder(int resId, ViewGroup viewGroup) {
        super(resId, viewGroup);
        tv_text = itemView.findViewById(R.id.tv_text);
    }

    @Override
    public void bind(@NonNull Context context,
                     @NonNull ChannelState channelState,
                     @NonNull MessageListItem messageListItem,
                     @NonNull MessageListViewStyle style,
                     @NonNull MessageListView.BubbleHelper bubbleHelper,
                     @NonNull MessageViewHolderFactory factory,
                     int position) {
//        tv_text.setText();
    }
}
