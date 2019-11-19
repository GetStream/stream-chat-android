package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.TextView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.enums.Dates;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.Date;

import static android.text.format.DateUtils.getRelativeTimeSpanString;

public class DateSeparatorViewHolder extends BaseMessageListItemViewHolder {
    private MessageListViewStyle style;
    private TextView tv_date;

    public DateSeparatorViewHolder(int resId, ViewGroup viewGroup) {
        super(resId, viewGroup);
        tv_date = itemView.findViewById(R.id.tv_date);
    }

    @Override
    public void bind(Context context,
                     ChannelState channelState,
                     MessageListItem messageListItem,
                     int position) {

        String humanizedDate;
        long messageDate = messageListItem.getDate().getTime();
        Date now = new Date();
        if ((now.getTime() - messageDate) < 60 * 1000)
            humanizedDate = Dates.JUST_NOW.label;
        else
            humanizedDate = getRelativeTimeSpanString(messageDate).toString();

        tv_date.setText(humanizedDate);
        applyStyle();
    }

    private void applyStyle(){
        tv_date.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getDateSeparatorSize());
        tv_date.setTextColor(style.getDateSeparatorColor());
        tv_date.setTypeface(Typeface.DEFAULT, style.getDateSeparatorStyle());
    }

    @Override
    public void setStyle(MessageListViewStyle style) {
        this.style = style;
    }
}
