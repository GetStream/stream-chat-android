package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;
import com.getstream.sdk.chat.utils.StringUtility;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.view.AvatarGroupView;
import com.getstream.sdk.chat.view.ChannelListView;
import com.getstream.sdk.chat.view.ChannelListViewStyle;
import com.getstream.sdk.chat.view.ReadStateView;

import java.text.SimpleDateFormat;
import java.util.List;

import io.noties.markwon.Markwon;
import io.noties.markwon.core.CorePlugin;
import io.noties.markwon.linkify.LinkifyPlugin;

public class ChannelListItemViewHolder extends BaseChannelListItemViewHolder {

    static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d");

    public ConstraintLayout cl_root;
    public TextView tv_name, tv_last_message, tv_date, tv_click;
    public ReadStateView<ChannelListViewStyle> read_state;
    public AvatarGroupView<ChannelListViewStyle> avatarGroupView;

    private Context context;

    private ChannelListView.UserClickListener userClickListener;
    private ChannelListView.ChannelClickListener channelClickListener;
    private ChannelListView.ChannelClickListener channelLongClickListener;
    private ChannelListViewStyle style;

    private Markwon markwon;

    public ChannelListItemViewHolder(@NonNull View itemView) {
        super(itemView);
        findReferences();
    }


    public void setUserClickListener(ChannelListView.UserClickListener l) {
        userClickListener = l;
    }

    public void setChannelClickListener(ChannelListView.ChannelClickListener l) {
        channelClickListener = l;
    }

    public void setChannelLongClickListener(ChannelListView.ChannelClickListener l) {
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
        tv_date.setTextColor(style.getDateTextColor());
        tv_date.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getDateTextSize());
        tv_name.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getTitleTextSize());
        tv_last_message.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getMessageTextSize());
    }

    public void applyUnreadStyle() {
        // channel name
        tv_name.setTextColor(style.getUnreadTitleTextColor());
        tv_name.setTypeface(Typeface.DEFAULT, style.getUnreadTitleTextStyle());

        // last message
        tv_last_message.setTypeface(Typeface.DEFAULT, style.getUnreadMessageTextStyle());
        tv_last_message.setTextColor(style.getUnreadMessageTextColor());
    }

    public void applyReadStyle() {
        // channel name
        tv_name.setTextColor(style.getTitleTextColor());
        tv_name.setTypeface(Typeface.DEFAULT, style.getTitleTextStyle());
        // last messsage
        tv_last_message.setTypeface(Typeface.DEFAULT, style.getMessageTextStyle());
        tv_last_message.setTextColor(style.getMessageTextColor());
    }

    @Override
    public void bind(Context context, @NonNull ChannelState channelState, int position) {

        // setup the click listeners and the markdown builder
        this.context = context;

        // the UI depends on the
        // - lastMessage
        // - unread count
        // - read state for this channel

        // set the channel name
        configChannelName(channelState);
        // set the data for the avatar
        configAvatarView(channelState);
        // set the lastMessage
        configLastMessage(channelState);
        // set last message date
        configLastMessageDate(channelState);
        // read indicators
        configReadState(channelState);
        // set Click listeners
        configClickListeners(channelState);
        // apply style
        applyStyle(channelState);
    }

    private void configChannelName(ChannelState channelState){
        String channelName = channelState.getChannelNameOrMembers();
        tv_name.setText(channelName);
    }

    private void configAvatarView(ChannelState channelState){
        Channel channel = channelState.getChannel();
        List<User> otherUsers = channelState.getOtherUsers();
        avatarGroupView.setChannelAndLastActiveUsers(channelState.getChannel(), otherUsers, style);
        // click listeners
        avatarGroupView.setOnClickListener(view -> {
            // if there is 1 user
            if (otherUsers.size() == 1 && this.userClickListener != null) {
                this.userClickListener.onUserClick(otherUsers.get(0));
            } else if (this.channelClickListener != null) {
                this.channelClickListener.onClick(channel);
            }
        });
    }

    private void configLastMessage(ChannelState channelState){
        Message lastMessage = channelState.getLastMessage();
        // null check
        if (lastMessage == null){
            tv_last_message.setText("");
            return;
        }
        // set Markwon
        if (!TextUtils.isEmpty(lastMessage.getText())) {
            if (markwon == null)
                markwon = Markwon.builder(context)
                        .usePlugin(CorePlugin.create())
                        .usePlugin(LinkifyPlugin.create())
                        .build();
            markwon.setMarkdown(tv_last_message, StringUtility.getDeletedOrMentionedText(lastMessage));
            return;
        }

        if (lastMessage.getAttachments().isEmpty()){
            tv_last_message.setText("");
            return;
        }

        Attachment attachment = lastMessage.getAttachments().get(0);
        if (attachment.getType() == null)
            return;

        switch (attachment.getType()){
            case ModelType.attach_image:
                break;
            case ModelType.attach_giphy:
                break;
        }
        tv_last_message.setText(!TextUtils.isEmpty(attachment.getTitle()) ? attachment.getTitle() : attachment.getFallback());
    }

    private void configLastMessageDate(ChannelState channelState){
        Message lastMessage = channelState.getLastMessage();
        // null check
        if (lastMessage == null) {
            tv_date.setText("");
            return;
        }

        if (lastMessage.isToday())
            tv_date.setText(lastMessage.getTime());
        else
            tv_date.setText(dateFormat.format(lastMessage.getCreatedAt()));
    }

    private void configReadState(ChannelState channelState){
        List<ChannelUserRead> lastMessageReads = channelState.getLastMessageReads();
        read_state.setReads(lastMessageReads, true, style);
    }

    private void configClickListeners(ChannelState channelState){
        Channel channel = channelState.getChannel();
        tv_click.setOnClickListener(view -> {
            Utils.setButtonDelayEnable(view);
            tv_click.setBackgroundColor(Color.parseColor("#14000000"));
            new Handler().postDelayed(() -> tv_click.setBackgroundColor(0), 500);
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

    private void applyStyle(ChannelState channelState){
        if (channelState.readLastMessage())
            applyReadStyle();
        else
            applyUnreadStyle();
    }
}
