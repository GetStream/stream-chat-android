package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.enums.ReadIndicator;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.StringUtility;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.view.AvatarGroupView;
import com.getstream.sdk.chat.view.ChannelListView;
import com.getstream.sdk.chat.view.ReadStateView;

import java.text.SimpleDateFormat;
import java.util.List;

import ru.noties.markwon.Markwon;
import ru.noties.markwon.core.CorePlugin;
import ru.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import ru.noties.markwon.image.ImagesPlugin;

import static android.text.format.DateUtils.getRelativeTimeSpanString;

public class ChannelListItemViewHolder extends BaseChannelListItemViewHolder {

    static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d");

    public ConstraintLayout cl_root;
    public TextView tv_name, tv_last_message, tv_date, tv_click;
    public ReadStateView read_state;
    public ImageView iv_indicator;
    public AvatarGroupView iv_avatar;

    private Context context;

    private Markwon markdownBuilder;
    private ChannelListView.UserClickListener userClickListener;
    private ChannelListView.ChannelClickListener channelClickListener;
    private ChannelListView.ChannelClickListener channelLongClickListener;
    private ChannelListView.Style style;
    
    public ChannelListItemViewHolder(int resId, ViewGroup parent, ChannelListView.Style style) {
        super(resId, parent);


        this.style = style;
        findReferences();
        applyStyle();
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
        iv_avatar = itemView.findViewById(R.id.avatar_group);

        read_state = itemView.findViewById(R.id.read_state);

    }

    public void applyStyle() {
        // TODO: apply more styles here
        //tv_date.setTextSize(style.dateTextSize);
    }

    public void applyUnreadStyle() {
        tv_last_message.setTypeface(tv_last_message.getTypeface(), Typeface.BOLD);
        tv_last_message.setTextColor(ContextCompat.getColor(context, R.color.black));

    }

    public void applyReadStyle() {
        tv_last_message.setTypeface(tv_last_message.getTypeface(), Typeface.NORMAL);
        tv_last_message.setTextColor(ContextCompat.getColor(context, R.color.gray_dark));
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
        iv_avatar.setChannelAndOtherUsers(channelState.getChannel(), otherUsers);

        // set the channel name
        tv_name.setText(channelName);

        if (lastMessage != null) {
            // set the lastMessage and last messageDate
            tv_last_message.setText(lastMessage.getText());
            if (lastMessage.isToday())
                tv_date.setText(lastMessage.getTime());
            else
                tv_date.setText(dateFormat.format(lastMessage.getCreatedAtDate()));
        }

        // read indicators
        read_state.setReads(lastMessageReads);

        // apply unread style or read style
        if (unreadCount == 0) {
            this.applyReadStyle();
        } else {
            this.applyUnreadStyle();
        }

        // click listeners
        iv_avatar.setOnClickListener(view -> {
            // if there is 1 user
            if (otherUsers.size() == 1 && this.userClickListener != null) {
                this.userClickListener.onClick(otherUsers.get(0));
            } else {
                this.channelClickListener.onClick(channel);

            }
        });

        tv_click.setOnClickListener(view -> {
            Utils.setButtonDelayEnable(view);
            tv_click.setBackgroundColor(Color.parseColor("#14000000"));
            new Handler().postDelayed(() ->tv_click.setBackgroundColor(0), 500);
            this.channelClickListener.onClick(channel);
        });

        tv_click.setOnLongClickListener(view -> {
            this.channelLongClickListener.onClick(channel);
            return true;
        });
    }
}
