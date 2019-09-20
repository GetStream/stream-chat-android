package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import static android.text.format.DateUtils.getRelativeTimeSpanString;

public class DateSeparatorViewHolder extends BaseMessageListItemViewHolder {
    private MessageListViewStyle style;
    private TextView tv_header_date, tv_header_time;

    public DateSeparatorViewHolder(int resId, ViewGroup viewGroup) {
        super(resId, viewGroup);

        tv_header_date = itemView.findViewById(R.id.tv_header_date);
        tv_header_time = itemView.findViewById(R.id.tv_header_time);
    }

    @Override
    public void bind(Context context,
                     ChannelState channelState,
                     MessageListItem messageListItem,
                     int position,
                     boolean isThread,
                     MessageListView.MessageClickListener l1,
                     MessageListView.MessageLongClickListener messageLongClickListener,
                     MessageListView.AttachmentClickListener l2,
                     MessageListView.UserClickListener userClickListener,
                     MessageListView.ReadStateClickListener readStateClickListener) {

        String humanizedDate = getRelativeTimeSpanString(messageListItem.getDate().getTime()).toString();
        tv_header_date.setText(humanizedDate);
        tv_header_time.setText(" AT " + humanizedDate);
    }

    @Override
    public void setStyle(MessageListViewStyle style) {
        this.style = style;
    }
}
