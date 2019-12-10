package io.getstream.chat.example;

import android.app.Application;

import androidx.annotation.NonNull;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

public class CustomChannelViewModel extends ChannelViewModel {

    public CustomChannelViewModel(Application application, String channelId) {
        super(application, channelId);
    }
}
