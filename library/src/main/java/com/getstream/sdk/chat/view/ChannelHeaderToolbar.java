package com.getstream.sdk.chat.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.getstream.sdk.chat.databinding.ToolbarChannelHeaderBinding;
import com.getstream.sdk.chat.rest.Message;

public class ChannelHeaderToolbar extends RelativeLayout {

    final String TAG = ChannelHeaderToolbar.class.getSimpleName();

    // binding for this view
    private ToolbarChannelHeaderBinding binding;

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
        // TODO: Rewrite once channel objects actually do what they are supposed to
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
//            User opponent = Global.getOpponentUser(channelResponse);
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
//        if (!TextUtils.isEmpty(channelResponse.getChannel().getName())) {
//            channelName = channelResponse.getChannel().getName();
//        } else {
//            User opponent = Global.getOpponentUser(channelResponse);
//            if (opponent != null) {
//                channelName = opponent.getName();
//            }
//        }
//
//        binding.tvChannelName.setText(channelName);
//
//        // Last Active
//        Message lastMessage = channelResponse.getOpponentLastMessage();
//        configHeaderLastActive(lastMessage);
//        // Online Mark
//        try {
//            if (Global.getOpponentUser(channelResponse) == null)
//                binding.ivActiveMark.setVisibility(View.GONE);
//            else {
//                if (Global.getOpponentUser(channelResponse).getOnline()) {
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
        //binding.tvBack.setOnClickListener((View v) -> finish());
    }
}
