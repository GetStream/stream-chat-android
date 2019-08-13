package com.getstream.sdk.chat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.databinding.ToolbarChannelHeaderBinding;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

public class ChannelHeaderView extends RelativeLayout implements View.OnClickListener {

    final String TAG = ChannelHeaderView.class.getSimpleName();

    // binding for this view
    private ToolbarChannelHeaderBinding binding;
    private OnBackClickListener onBackClickListener;
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
    }

    public ChannelHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parseAttr(context, attrs);
        binding = initBinding(context);
    }

    private void parseAttr(Context context, @Nullable AttributeSet attrs) {
        // parse the attributes
        style = new ChannelHeaderViewStyle(context, attrs);
    }

    public void setViewModel(ChannelViewModel model, LifecycleOwner lifecycleOwner) {
        this.viewModel = model;
        binding.setLifecycleOwner(lifecycleOwner);
        binding.setViewModel(viewModel);
        viewModel.loading.observe(lifecycleOwner, (Boolean loading) -> {
            if (!loading) configHeaderAvatar();
        });
        configUIs();
    }

    private ToolbarChannelHeaderBinding initBinding(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = ToolbarChannelHeaderBinding.inflate(inflater, this, true);
        // setup the onClick listener for the back button
        binding.tvBack.setOnClickListener(this);
        return binding;
    }

    public void setOnBackClickListener(OnBackClickListener onBackClickListener) {
        this.onBackClickListener = onBackClickListener;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_back) {
            if (this.onBackClickListener != null) {
                this.onBackClickListener.onClick(v);
            }
        }
    }

    public interface OnBackClickListener {
        void onClick(View v);
    }

    private void configUIs() {
        // back button
        binding.tvBack.setVisibility(style.isBackButtonShow() ? VISIBLE : INVISIBLE);
        // Title
        binding.tvChannelName.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getChannelTitleTextSize());
        binding.tvChannelName.setTextColor(style.getChannelTitleTextColor());
        binding.tvChannelName.setTypeface(binding.tvChannelName.getTypeface(), style.getChannelTitleTextStyle());
    }

    private void configHeaderAvatar() {
        // TODO:
        // - the avatar should be it's own view since the logic is quite complex and needed in many screens
        // - IE: channelImage with fallback to list of images from other users, fallback to initial of other users
        // - once we simplify this to it's own view we can just use databinding for the data and remove configHeaderAvatar
        Channel channel = this.viewModel.getChannel();
        ChannelState channelState = channel.getChannelState();
        AvatarGroupView<ChannelHeaderViewStyle> avatarGroupView = binding.avatarGroup;
        avatarGroupView.setChannelAndLastActiveUsers(channel, channelState.getOtherUsers(), style);
    }
}
