package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.enums.Dates;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.Date;

import static android.text.format.DateUtils.getRelativeTimeSpanString;

public class DateSeparatorViewHolder extends BaseMessageListItemViewHolder {
    private MessageListViewStyle style;
    private Context context;
    private TextView tv_date;
    private ImageView iv_line_right, iv_line_left;

    public DateSeparatorViewHolder(int resId, ViewGroup viewGroup) {
        super(resId, viewGroup);
        tv_date = itemView.findViewById(R.id.tv_date);
        iv_line_right = itemView.findViewById(R.id.iv_line_right);
        iv_line_left = itemView.findViewById(R.id.iv_line_left);
    }

    @Override
    public void bind(Context context,
                     ChannelState channelState,
                     MessageListItem messageListItem,
                     int position) {
        this.context = context;
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

        tv_date.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getDateSeparatorDateTextSize());
        tv_date.setTextColor(style.getDateSeparatorDateTextColor());
        tv_date.setTypeface(Typeface.DEFAULT, style.getDateSeparatorDateTextStyle());
        if (style.getDateSeparatorLineDrawable() != -1) {
            int drawable = style.getDateSeparatorLineDrawable();
            iv_line_right.setBackground(context.getDrawable(drawable));
            iv_line_left.setBackground(context.getDrawable(drawable));
        } else {
            iv_line_right.setBackgroundColor(style.getDateSeparatorLineColor());
            iv_line_left.setBackgroundColor(style.getDateSeparatorLineColor());
        }
        configSeparatorLineWidth(iv_line_right);
        configSeparatorLineWidth(iv_line_left);
    }

    private void configSeparatorLineWidth(View view) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        params.height = style.getDateSeparatorLineWidth();
        view.setLayoutParams(params);
    }

    @Override
    public void setStyle(MessageListViewStyle style) {
        this.style = style;
    }
}
