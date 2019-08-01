package com.getstream.sdk.chat.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.getstream.sdk.chat.rest.response.ChannelResponse;

public class ChannelFragmentViewModelFactory implements ViewModelProvider.Factory {
    private ChannelResponse channelResponse;

    public ChannelFragmentViewModelFactory(ChannelResponse channelResponse){
        this.channelResponse = channelResponse;
    }

    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ChannelViewModel.class)) {
            return (T) new ChannelViewModel(channelResponse);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
