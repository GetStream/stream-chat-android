package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.Date;
import java.util.List;

import static android.text.format.DateUtils.getRelativeTimeSpanString;

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
    public void bind(Context context, ChannelState channelState, MessageListItemAdapter.Entity entity, int position, boolean isThread, View.OnClickListener reactionListener, View.OnLongClickListener longClickListener) {

        String humanizedDate = getRelativeTimeSpanString(entity.getDate().getTime()).toString();
        tv_header_date.setText(humanizedDate);
        tv_header_time.setText(" AT " + humanizedDate);
    }
}
