package com.getstream.sdk.chat.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ChannelViewModelFactory implements ViewModelProvider.Factory {

    private Application application;
    private String channelId;

    public ChannelViewModelFactory(Application application, String channelId) {
        this.application = application;
        this.channelId = channelId;
    }

    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ChannelViewModel(application, channelId);
    }
}