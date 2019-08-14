package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.List;

public class DateSeparatorViewHolder extends BaseMessageListItemViewHolder {
    private MessageListViewStyle style;
    private TextView tv_header_date, tv_header_time;

    public DateSeparatorViewHolder(int resId, ViewGroup viewGroup, MessageListViewStyle s) {
        this(resId, viewGroup);
        style = s;
    }

    public DateSeparatorViewHolder(int resId, ViewGroup viewGroup) {
        super(resId, viewGroup);

        tv_header_date = itemView.findViewById(R.id.tv_header_date);
        tv_header_time = itemView.findViewById(R.id.tv_header_time);
    }

    @Override
    public void bind(Context context, ChannelState channelState, @NonNull List<Message> messageList, int position, boolean isThread, View.OnClickListener reactionListener, View.OnLongClickListener longClickListener) {


        String headerDate = message.getDate(), headerTime;
        if (message.isToday())
            headerDate = "Today";

        if (message.isYesterday())
            headerDate = "Yesterday";

        headerTime = message.getTime();
        tv_header_date.setText(headerDate);
        tv_header_time.setText(" AT " + headerTime);
    }
}
