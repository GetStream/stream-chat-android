package com.getstream.sdk.chat.viewmodel;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

import com.getstream.sdk.chat.rest.response.ChannelState;

public class ChannelFragmentViewModelFactory implements ViewModelProvider.Factory {
    private ChannelState channelState;

    public ChannelFragmentViewModelFactory(ChannelState channelState){
        this.channelState = channelState;
    }

    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ChannelViewModelOld.class)) {
            return (T) new ChannelViewModelOld(channelState);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
