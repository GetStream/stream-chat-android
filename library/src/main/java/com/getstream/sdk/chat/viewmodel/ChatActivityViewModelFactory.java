package com.getstream.sdk.chat.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.getstream.sdk.chat.rest.response.ChannelResponse;

public class ChatActivityViewModelFactory implements ViewModelProvider.Factory {
    private ChannelResponse channelResponse;

    public ChatActivityViewModelFactory(ChannelResponse channelResponse){
        this.channelResponse = channelResponse;
    }

    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ChatActivityViewModel.class)) {
            return (T) new ChatActivityViewModel(channelResponse);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
