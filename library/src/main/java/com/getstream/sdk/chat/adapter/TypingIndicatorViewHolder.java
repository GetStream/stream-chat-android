package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.List;


public class TypingIndicatorViewHolder extends BaseMessageListItemViewHolder {
    private MessageListViewStyle style;
    // Tying
    private ImageView iv_typing_indicator;
    private LinearLayout ll_typingusers;
    private Context context;

    public TypingIndicatorViewHolder(int resId, ViewGroup viewGroup, MessageListViewStyle s) {
        this(resId, viewGroup);
        style = s;
    }

    public TypingIndicatorViewHolder(int resId, ViewGroup viewGroup) {
        super(resId, viewGroup);
        iv_typing_indicator = itemView.findViewById(R.id.iv_typing_indicator);
        ll_typingusers = itemView.findViewById(R.id.ll_typing_indicator);
    }

    @Override
    public void bind(Context context,
                     ChannelState channelState,
                     MessageListItem messageListItem,
                     int position, boolean isThread,
                     MessageListView.MessageClickListener l1,
                     MessageListView.MessageLongClickListener messageLongClickListener,
                     MessageListView.AttachmentClickListener l2,
                     MessageListView.UserClickListener userClickListener) {

        this.context = context;

        ll_typingusers.setVisibility(View.VISIBLE);
        iv_typing_indicator.setVisibility(View.VISIBLE);
        ll_typingusers.removeAllViews();
        Resources resources = context.getResources();
        float marginLeft = resources.getDimension(R.dimen.user_avatar_margin_left);
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        List<User> users = messageListItem.getUsers();
        int i = 0;

        for (User user : users) {
            View v = vi.inflate(R.layout.view_user_avatar_initials, null);
            TextView textView = v.findViewById(R.id.tv_initials);
            ImageView imageView = v.findViewById(R.id.cv_avatar);
            textView.setText(user.getInitials());
            Utils.circleImageLoad(imageView, user.getImage());
            int height = (int) context.getResources().getDimension(R.dimen.message_typing_indicator_size);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(height, height);
            if (i == 0) {
                params.setMargins(0, 0, 0, 0);
            } else {
                params.setMargins(-(int) marginLeft, 0, 0, 0);
            }

            v.setLayoutParams(params);
            ll_typingusers.addView(v);
            i += 1;
        }
        Glide.with(context).load(R.raw.stream_typing).into(iv_typing_indicator);
    }

    @Override
    public void setStyle(MessageListViewStyle style) {
        this.style = style;
    }

}
