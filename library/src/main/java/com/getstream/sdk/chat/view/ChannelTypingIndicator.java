package com.getstream.sdk.chat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;


import com.getstream.sdk.chat.databinding.StreamChannelTypingIndicatorBinding;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

public class ChannelTypingIndicator extends RelativeLayout {

    final String TAG = ChannelTypingIndicator.class.getSimpleName();

    private ChannelViewModel viewModel;
    private StreamChannelTypingIndicatorBinding binding;

    public ChannelTypingIndicator(Context context) {
        super(context);
        binding = initBinding(context);
    }

    public ChannelTypingIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        binding = initBinding(context);
    }

    public ChannelTypingIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        binding = initBinding(context);
    }

    public void setViewModel(ChannelViewModel model, LifecycleOwner lifecycleOwner) {
        this.viewModel = model;
        binding.setLifecycleOwner(lifecycleOwner);
        binding.setViewModel(viewModel);
    }

    private StreamChannelTypingIndicatorBinding initBinding(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = StreamChannelTypingIndicatorBinding.inflate(inflater, this, true);
        return binding;
    }
}

