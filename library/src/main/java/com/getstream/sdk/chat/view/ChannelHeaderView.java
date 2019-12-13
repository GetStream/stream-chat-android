package com.getstream.sdk.chat.view;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.channels.GetChannelNameOrMembers;
import com.getstream.sdk.chat.channels.GetLastActive;
import com.getstream.sdk.chat.databinding.StreamViewChannelHeaderBinding;
import com.getstream.sdk.chat.enums.OnlineStatus;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.users.GetOtherUsers;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import static android.text.format.DateUtils.getRelativeTimeSpanString;
import static com.getstream.sdk.chat.enums.OnlineStatus.CONNECTED;

/*
ChannelHeaderView is used to show a header for a channel

it binds to ChannelViewModel view model and subscribe to channel data

Out of the box this view shows the following information:

- Channel title
- Online status of other members (using AvatarGroupView)
- Channel last activity from other users

 */
public class ChannelHeaderView extends RelativeLayout {

    final static String TAG = ChannelHeaderView.class.getSimpleName();

    // binding for this view
    private StreamViewChannelHeaderBinding binding;
    private ChannelHeaderViewStyle style;
    // our connection to the channel scope
    private ChannelViewModel viewModel;

    public ChannelHeaderView(Context context) {
        super(context);
        binding = initBinding(context);
    }

    public ChannelHeaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        parseAttr(context, attrs);
        binding = initBinding(context);
        applyStyle();
    }

    private void parseAttr(Context context, @Nullable AttributeSet attrs) {
        // parse the attributes
        style = new ChannelHeaderViewStyle(context, attrs);
    }

    public void setViewModel(ChannelViewModel model, LifecycleOwner lifecycleOwner) {
        this.viewModel = model;
        binding.setLifecycleOwner(lifecycleOwner);
        binding.setViewModel(viewModel);

        //TODO: Refactoring: enable when state is available
        //viewModel.getChannelState().observe(lifecycleOwner, this::setHeaderTitle);
        //viewModel.getChannelState().observe(lifecycleOwner, this::setHeaderLastActive);
        //viewModel.getChannelState().observe(lifecycleOwner, this::configHeaderAvatar);

        //TODO: Refactoring: enable when state is available
        //viewModel.getOnlineState().observe(lifecycleOwner, this::onlineStatus);
        //binding.setOnlineStatus(viewModel.getOnlineState().getValue() == CONNECTED);

        //StreamChat.getOnlineStatus().observe(lifecycleOwner, this::onlineStatus);
        //binding.setOnlineStatus(StreamChat.getOnlineStatus().getValue() == CONNECTED);
    }

    protected void onlineStatus(OnlineStatus status) {
        binding.setOnlineStatus(status == CONNECTED);
    }

    protected void setHeaderTitle(ChannelState channelState) {
        String channelName = new GetChannelNameOrMembers(new GetOtherUsers(StreamChat.getUsersCache())).getChannelNameOrMembers(channelState.getChannel());
        binding.setChannelName(!TextUtils.isEmpty(channelName) ? channelName : style.getChannelWithoutNameText());
    }

    protected void setHeaderLastActive(ChannelState channelState) {
        Date lastActive = new GetLastActive(StreamChat.getUsersCache()).getLastActive(channelState.getChannel());
        Date now = new Date();
        String timeAgo = getRelativeTimeSpanString(lastActive.getTime()).toString();

        if (now.getTime() - lastActive.getTime() < 60000) {
            timeAgo = getContext().getString(R.string.stream_channel_header_active_now);
        }

        binding.setChannelLastActive(String.format(getContext().getString(R.string.stream_channel_header_active), timeAgo));
    }

    protected void configHeaderAvatar(ChannelState channelState) {
        List<User> otherUsers = new GetOtherUsers(StreamChat.getUsersCache()).getOtherUsers(channelState.getChannel());
        AvatarGroupView<ChannelHeaderViewStyle> avatarGroupView = binding.avatarGroup;
        avatarGroupView.setChannelAndLastActiveUsers(channelState.getChannel(), otherUsers, style);
    }

    private StreamViewChannelHeaderBinding initBinding(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = StreamViewChannelHeaderBinding.inflate(inflater, this, true);
        // setup the onMessageClick listener for the back button
        binding.tvBack.setOnClickListener(view -> {
            if (viewModel.isThread())
                viewModel.initThread();
            else
                ((Activity) getContext()).finish();
        });
        return binding;
    }

    public void setHeaderOptionsClickListener(MessageListView.HeaderOptionsClickListener headerOptionsClickListener) {
        binding.tvOption.setOnClickListener(view -> headerOptionsClickListener.onHeaderOptionsClick(viewModel.getChannel()));
    }

    public void setHeaderAvatarGroupClickListener(MessageListView.HeaderAvatarGroupClickListener headerOptionsClickListener) {
        binding.avatarGroup.setOnClickListener(view -> headerOptionsClickListener.onHeaderAvatarGroupClick(viewModel.getChannel()));
    }

    private void applyStyle() {
        // Title
        style.channelTitleText.apply(binding.tvChannelName);

        // Last Active
        style.lastActiveText.apply(binding.tvActive);
        binding.tvActive.setVisibility(style.isLastActiveShow() ? VISIBLE : GONE);
        // Back Button
        binding.tvBack.setVisibility(style.isBackButtonShow() ? VISIBLE : INVISIBLE);
        binding.tvBack.setBackground(style.getBackButtonBackground());
        // Avatar Group
        binding.avatarGroup.setVisibility(style.isAvatarGroupShow() ? VISIBLE : INVISIBLE);
        // Options Button
        binding.tvOption.setVisibility(style.isOptionsButtonShow() ? VISIBLE : INVISIBLE);
        binding.tvOption.setBackground(style.getOptionsButtonBackground());
        binding.tvOption.setTextSize(style.getOptionsButtonTextSize());
        binding.tvOption.setWidth(style.getOptionsButtonWidth());
        binding.tvOption.setHeight(style.getOptionsButtonHeight());
        // Active Badge
        binding.ivActiveBadge.setVisibility(style.isActiveBadgeShow() ? VISIBLE : INVISIBLE);
        binding.setOfflineText(style.getOfflineText());
    }

}
