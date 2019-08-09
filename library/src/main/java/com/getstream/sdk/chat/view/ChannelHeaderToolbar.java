package com.getstream.sdk.chat.view;


import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.databinding.ToolbarChannelHeaderBinding;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.StringUtility;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

public class ChannelHeaderToolbar extends RelativeLayout implements View.OnClickListener {

    final String TAG = ChannelHeaderToolbar.class.getSimpleName();

    // binding for this view
    private ToolbarChannelHeaderBinding binding;
    private OnBackClickListener onBackClickListener;

    // our connection to the channel scope
    private ChannelViewModel viewModel;

    public ChannelHeaderToolbar(Context context) {
        super(context);
        binding = initBinding(context);
    }

    public ChannelHeaderToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        binding = initBinding(context);
    }

    public ChannelHeaderToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        binding = initBinding(context);
    }

    public void setViewModel(ChannelViewModel model, LifecycleOwner lifecycleOwner) {
        this.viewModel = model;

        configHeaderView();
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


    private void configHeaderView() {
        // TODO:
        // - the avatar should be it's own view since the logic is quite complex and needed in many screens
        // - IE: channelImage with fallback to list of images from other users, fallback to initial of other users
        // - once we simplify this to it's own view we can just use databinding for the data and remove configHeaderView
        Channel channel = this.viewModel.getChannel();
        ChannelState channelState = channel.getChannelState();

        if (StringUtility.isValidImageUrl(channel.getImage())) {
            Utils.circleImageLoad(binding.ivHeaderAvatar, channel.getImage());
            binding.ivHeaderAvatar.setVisibility(View.VISIBLE);
        } else {
            for (User u: channelState.getOtherUsers()) {
                Utils.circleImageLoad(binding.ivHeaderAvatar, u.getImage());
                binding.ivHeaderAvatar.setVisibility(View.VISIBLE);
            }
        }
    }
}
