package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.view.AvatarGroupView;
import com.getstream.sdk.chat.view.ChannelListView;
import com.getstream.sdk.chat.view.ChannelListViewStyle;
import com.getstream.sdk.chat.view.ReadStateView;

import java.text.SimpleDateFormat;
import java.util.List;

public class ChannelListItemViewHolder extends BaseChannelListItemViewHolder {

    static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d");

    public ConstraintLayout cl_root;
    public TextView tv_name, tv_last_message, tv_date, tv_click;
    public ReadStateView read_state;
    public AvatarGroupView<ChannelListViewStyle> avatarGroupView;

    private Context context;

    private ChannelListView.UserClickListener userClickListener;
    private ChannelListView.ChannelClickListener channelClickListener;
    private ChannelListView.ChannelClickListener channelLongClickListener;
    private ChannelListViewStyle style;

    public ChannelListItemViewHolder(@NonNull View itemView) {
        super(itemView);
        findReferences();
    }


    public void setUserClickListener(ChannelListView.UserClickListener l) {
        userClickListener = l;
    }

    public void setChannelClickListener(ChannelListView.ChannelClickListener l ) {
        channelClickListener = l;
    }
    public void setChannelLongClickListener(ChannelListView.ChannelClickListener l ) {
        channelLongClickListener = l;
    }

    public void findReferences() {
        cl_root = itemView.findViewById(R.id.cl_root);
        tv_name = itemView.findViewById(R.id.tv_name);
        tv_last_message = itemView.findViewById(R.id.tv_last_message);
        tv_date = itemView.findViewById(R.id.tv_date);

        tv_click = itemView.findViewById(R.id.tv_click);
        avatarGroupView = itemView.findViewById(R.id.avatar_group);

        read_state = itemView.findViewById(R.id.read_state);

    }

    public void setStyle(ChannelListViewStyle style) {
        this.style = style;


        if (style.getDateTextColor() != -1) {
            tv_date.setTextColor(style.getDateTextColor());
        }

        if (style.getDateTextSize() != -1) {
            tv_date.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getDateTextSize());
        }

        if (style.getTitleTextSize() != -1) {
            tv_name.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getTitleTextSize());
        }
        if (style.getMessageTextSize() != -1) {
            tv_last_message.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getMessageTextSize());
        }

    }

    public void applyUnreadStyle() {
        // channel name
        tv_name.setTextColor(style.getUnreadTitleTextColor());
        tv_name.setTypeface(tv_name.getTypeface(), style.getUnreadTitleTextStyle());

        // last message
        tv_last_message.setTypeface(tv_last_message.getTypeface(),  style.getUnreadMessageTextStyle());
        tv_last_message.setTextColor(style.getUnreadMessageTextColor());
    }

    public void applyReadStyle() {
        // channel name
        tv_name.setTextColor(style.getTitleTextColor());
        tv_name.setTypeface(tv_name.getTypeface(), style.getTitleTextStyle());
        // last messsage
        tv_last_message.setTypeface(tv_last_message.getTypeface(), style.getMessageTextStyle());
        tv_last_message.setTextColor(style.getMessageTextColor());
    }

    @Override
    public void bind(Context context, ChannelState channelState, int position) {

        // setup the click listeners and the markdown builder
        this.context = context;

        // the UI depends on the
        // - lastMessage
        // - unread count
        // - read state for this channel
        Message lastMessage = channelState.getLastMessage();
        int unreadCount = channelState.getCurrentUserUnreadMessageCount();
        List<ChannelUserRead> lastMessageReads = channelState.getLastMessageReads();
        List<User> otherUsers = channelState.getOtherUsers();
        String channelName = channelState.getChannelNameOrMembers();
        Channel channel = channelState.getChannel();

        // set the data for the avatar
        avatarGroupView.setChannelAndLastActiveUsers(channelState.getChannel(), otherUsers, style);

        // set the channel name
        tv_name.setText(channelName);

        if (lastMessage != null) {
            // set the lastMessage and last messageDate
            tv_last_message.setText(lastMessage.getText());
            if (lastMessage.isToday())
                tv_date.setText(lastMessage.getTime());
            else
                tv_date.setText(dateFormat.format(lastMessage.getCreatedAt()));
        }

        // read indicators
        read_state.setReads(lastMessageReads,true, style);

        // apply unread style or read style
        if (unreadCount == 0) {
            this.applyReadStyle();
        } else {
            this.applyUnreadStyle();
        }

        // click listeners
        avatarGroupView.setOnClickListener(view -> {
            // if there is 1 user
            if (otherUsers.size() == 1 && this.userClickListener != null) {
                this.userClickListener.onUserClick(otherUsers.get(0));
            } else if (this.channelClickListener != null) {
                this.channelClickListener.onClick(channel);

            }
        });

        tv_click.setOnClickListener(view -> {
            Utils.setButtonDelayEnable(view);
            tv_click.setBackgroundColor(Color.parseColor("#14000000"));
            new Handler().postDelayed(() ->tv_click.setBackgroundColor(0), 500);
            if (this.channelClickListener != null) {
                this.channelClickListener.onClick(channel);
            }

        });

        tv_click.setOnLongClickListener(view -> {
            if (this.channelLongClickListener != null) {
                this.channelLongClickListener.onClick(channel);
            }

            return true;
        });
    }
}
