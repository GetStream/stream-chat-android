package io.getstream.chat.example;

import android.app.Application;

import androidx.annotation.NonNull;

import com.getstream.sdk.chat.viewmodel.ChannelListViewModel;

public class CustomChannelListViewModel extends ChannelListViewModel {
    public CustomChannelListViewModel(@NonNull Application application) {
        super(application);
    }
}
