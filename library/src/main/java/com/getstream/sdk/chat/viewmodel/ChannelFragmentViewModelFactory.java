package com.getstream.sdk.chat.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.getstream.sdk.chat.rest.response.ChannelState;

public class ChannelFragmentViewModelFactory implements ViewModelProvider.Factory {
    private ChannelState channelState;

    public ChannelFragmentViewModelFactory(ChannelState channelState){
        this.channelState = channelState;
    }

    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ChannelViewModel.class)) {
            return (T) new ChannelViewModel(channelState);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
