package com.getstream.sdk.chat.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.getstream.sdk.chat.databinding.ViewChannelHeaderBinding;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

import java.util.Date;

import static android.text.format.DateUtils.getRelativeTimeSpanString;

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
    private ViewChannelHeaderBinding binding;
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

        viewModel.getChannelState().observe(lifecycleOwner, channelState -> setHeaderTitle(channelState));
        viewModel.getChannelState().observe(lifecycleOwner, channelState -> setHeaderLastActive(channelState));
        viewModel.getChannelState().observe(lifecycleOwner, channelState -> configHeaderAvatar(channelState));
    }

    private ViewChannelHeaderBinding initBinding(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = ViewChannelHeaderBinding.inflate(inflater, this, true);
        // setup the onMessageClick listener for the back button
        binding.tvBack.setOnClickListener(view -> ((Activity) getContext()).finish());
        return binding;
    }


    private void applyStyle() {
        // Title
        binding.tvChannelName.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getChannelTitleTextSize());
        binding.tvChannelName.setTextColor(style.getChannelTitleTextColor());
        binding.tvChannelName.setTypeface(Typeface.DEFAULT, style.getChannelTitleTextStyle());
        // Last Active
        binding.tvActive.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getLastActiveTextSize());
        binding.tvActive.setTextColor(style.getLastActiveTextColor());
        binding.tvActive.setTypeface(Typeface.DEFAULT, style.getLastActiveTextStyle());
        // back button
        binding.tvBack.setVisibility(style.isBackButtonShow() ? VISIBLE : INVISIBLE);
    }


    protected void setHeaderTitle(ChannelState channelState){
        binding.setChannelName(channelState.getChannelNameOrMembers());
    }

    protected void setHeaderLastActive(ChannelState channelState){
        Date lastActive = channelState.getLastActive();
        Date now = new Date();
        String timeAgo = getRelativeTimeSpanString(lastActive.getTime()).toString();

        if (now.getTime() - lastActive.getTime() < 60000) {
            timeAgo = "just now";
        }

        binding.setChannelLastActive(String.format("Active %s", timeAgo));
    }

    private void configHeaderAvatar(ChannelState channelState) {
        AvatarGroupView<ChannelHeaderViewStyle> avatarGroupView = binding.avatarGroup;
        avatarGroupView.setChannelAndLastActiveUsers(channelState.getChannel(), channelState.getOtherUsers(), style);
    }
}
