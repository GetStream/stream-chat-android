package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.enums.Dates;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.Date;

import io.getstream.chat.android.client.models.Channel;

import static android.text.format.DateUtils.getRelativeTimeSpanString;

public class DateSeparatorViewHolder extends BaseMessageListItemViewHolder<MessageListItem.DateSeparatorItem> {

    private Context context;
    private MessageListItem.DateSeparatorItem messageListItem;
    private MessageListViewStyle style;
    private TextView tv_date;
    private ImageView iv_line_right, iv_line_left;

    public DateSeparatorViewHolder(int resId, ViewGroup viewGroup) {
        super(resId, viewGroup);
        tv_date = itemView.findViewById(R.id.tv_date);
        iv_line_right = itemView.findViewById(R.id.iv_line_right);
        iv_line_left = itemView.findViewById(R.id.iv_line_left);
    }

    @Override
    public void bind(@NonNull Context context,
                     @NonNull Channel channelState,
                     @NonNull MessageListItem.DateSeparatorItem messageListItem,
                     @NonNull MessageListViewStyle style,
                     @NonNull MessageListView.BubbleHelper bubbleHelper,
                     @NonNull MessageViewHolderFactory factory,
                     int position) {
        this.context = context;
        this.messageListItem = messageListItem;
        this.style = style;
        configDate();
        applyStyle();
    }

    private void configDate(){
        String humanizedDate;
        long messageDate = messageListItem.getDate().getTime();
        Date now = new Date();
        if ((now.getTime() - messageDate) < 60 * 1000)
            humanizedDate = Dates.JUST_NOW.getLabel();
        else
            humanizedDate = getRelativeTimeSpanString(messageDate).toString();

        tv_date.setText(humanizedDate);
    }
    private void applyStyle(){

        style.dateSeparatorDateText.apply(tv_date);

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

}
