package com.getstream.sdk.chat.view;


import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

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

    public void setViewModel(ChannelViewModel model) {
        this.viewModel = model;
        configHeaderView();
    }

    private ToolbarChannelHeaderBinding initBinding(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = ToolbarChannelHeaderBinding.inflate(inflater, this, true);

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

        // setup the onClick listener for the back button
        binding.tvBack.setOnClickListener(this);


        Channel channel = this.viewModel.getChannel();
        ChannelState channelState = channel.getChannelState();

        // avatar, channelName, lastActive, online/offline mark...

        if (!TextUtils.isEmpty(channel.getName())) {
            binding.tvChannelInitial.setText(channel.getInitials());
            Utils.circleImageLoad(binding.ivHeaderAvatar, channel.getImage());
            if (StringUtility.isValidImageUrl(channel.getImage())) {
                binding.ivHeaderAvatar.setVisibility(View.VISIBLE);
                binding.tvChannelInitial.setVisibility(View.INVISIBLE);
            } else {
                binding.ivHeaderAvatar.setVisibility(View.INVISIBLE);
                binding.tvChannelInitial.setVisibility(View.VISIBLE);
            }
        } else {
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
        }
        // Channel name


        binding.tvChannelName.setText(channelState.getChannelNameOrMembers());

        // Last Active
        Message lastMessage = channelState.getLastMessageFromOtherUser();
        // Online Mark
        try {
//            if (Global.getOpponentUser(channelState) == null)
//                binding.ivActiveMark.setVisibility(View.GONE);
//            else {
//                if (Global.getOpponentUser(channelState).getOnline()) {
//                    binding.ivActiveMark.setVisibility(View.VISIBLE);
//                } else {
//                    binding.ivActiveMark.setVisibility(View.GONE);
//                }
//            }
        } catch (Exception e) {
            binding.ivActiveMark.setVisibility(View.GONE);
        }


    }
}
