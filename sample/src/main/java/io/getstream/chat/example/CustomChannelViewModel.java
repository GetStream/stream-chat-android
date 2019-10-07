package io.getstream.chat.example;

import android.app.Application;

import androidx.annotation.NonNull;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

public class CustomChannelViewModel extends ChannelViewModel {
    public CustomChannelViewModel(Application application, Channel channel) {
        super(application, channel);
    }

    public CustomChannelViewModel(@NonNull Application application) {
        super(application);
    }
}
