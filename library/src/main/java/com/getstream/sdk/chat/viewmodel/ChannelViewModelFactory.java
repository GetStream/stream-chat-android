package com.getstream.sdk.chat.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ChannelViewModelFactory implements ViewModelProvider.Factory {

    private Application app;
    private final String channelType;
    private final String channelId;

    public ChannelViewModelFactory(Application application, String channelType, String channelId) {
        this.app = application;
        this.channelType = channelType;
        this.channelId = channelId;
    }

    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ChannelViewModel(app, channelType, channelId);
    }
}