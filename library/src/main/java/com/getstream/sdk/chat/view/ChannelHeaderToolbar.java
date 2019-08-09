package com.getstream.sdk.chat.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.getstream.sdk.chat.databinding.ToolbarChannelHeaderBinding;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

public class ChannelHeaderToolbar extends RelativeLayout {

    final String TAG = ChannelHeaderToolbar.class.getSimpleName();

    // binding for this view
    private ToolbarChannelHeaderBinding binding;

    // our connection to the channel scope
    private ChannelViewModel modelView;

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

    public void setViewModel(ChannelViewModel model) {
        this.modelView = model;
    }

    private ToolbarChannelHeaderBinding initBinding(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = ToolbarChannelHeaderBinding.inflate(inflater, this, true);

        return binding;
    }

    private void configHeaderLastActive(@Nullable Message message) {
        binding.setLastActive("1 hour ago");

    }

    private void configHeaderView() {
        // Avatar
//        if (!TextUtils.isEmpty(channel.getName())) {
//            binding.tvChannelInitial.setText(channel.getInitials());
//            Utils.circleImageLoad(binding.ivHeaderAvatar, channel.getImage());
//            if (StringUtility.isValidImageUrl(channel.getImage())) {
//                binding.ivHeaderAvatar.setVisibility(View.VISIBLE);
//                binding.tvChannelInitial.setVisibility(View.INVISIBLE);
//            } else {
//                binding.ivHeaderAvatar.setVisibility(View.INVISIBLE);
//                binding.tvChannelInitial.setVisibility(View.VISIBLE);
//            }
//        } else {
//            User opponent = Global.getOpponentUser(channelState);
//            if (opponent != null) {
//                binding.tvChannelInitial.setText(opponent.getUserInitials());
//                Utils.circleImageLoad(binding.ivHeaderAvatar, opponent.getImage());
//                binding.tvChannelInitial.setVisibility(View.VISIBLE);
//                binding.ivHeaderAvatar.setVisibility(View.VISIBLE);
//            } else {
//                binding.tvChannelInitial.setVisibility(View.VISIBLE);
//                binding.ivHeaderAvatar.setVisibility(View.INVISIBLE);
//            }
//        }
//        // Channel name
//        String channelName = "";
//
//        if (!TextUtils.isEmpty(channelState.getChannel().getName())) {
//            channelName = channelState.getChannel().getName();
//        } else {
//            User opponent = Global.getOpponentUser(channelState);
//            if (opponent != null) {
//                channelName = opponent.getName();
//            }
//        }
//
//        binding.tvChannelName.setText(channelName);
//
//        // Last Active
//        Message lastMessage = channelState.getLastMessageFromOtherUser();
//        configHeaderLastActive(lastMessage);
//        // Online Mark
//        try {
//            if (Global.getOpponentUser(channelState) == null)
//                binding.ivActiveMark.setVisibility(View.GONE);
//            else {
//                if (Global.getOpponentUser(channelState).getOnline()) {
//                    binding.ivActiveMark.setVisibility(View.VISIBLE);
//                } else {
//                    binding.ivActiveMark.setVisibility(View.GONE);
//                }
//            }
//        } catch (Exception e) {
//            binding.ivActiveMark.setVisibility(View.GONE);
//        }
//
//        binding.tvBack.setVisibility(singleConversation ? View.INVISIBLE : View.VISIBLE);
//        binding.tvBack.setOnClickListener((View v) -> finish());
    }
}
