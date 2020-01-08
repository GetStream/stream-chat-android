package io.getstream.chat.example.navigation;

import android.content.Context;
import android.content.Intent;

import com.getstream.sdk.chat.navigation.destinations.ChatDestination;

import io.getstream.chat.example.ChannelActivity;

import static io.getstream.chat.example.view.fragment.ChannelListFragment.EXTRA_CHANNEL_ID;
import static io.getstream.chat.example.view.fragment.ChannelListFragment.EXTRA_CHANNEL_TYPE;

public class ChannelDestination extends ChatDestination {

    private final String channelType;
    private final String channelId;

    public ChannelDestination(String channelType, String channelId, Context context) {
        super(context);
        this.channelType = channelType;
        this.channelId = channelId;
    }

    @Override
    public void navigate() {
        Intent intent = new Intent(context, ChannelActivity.class);
        intent.putExtra(EXTRA_CHANNEL_TYPE, channelType);
        intent.putExtra(EXTRA_CHANNEL_ID, channelId);
        start(intent);
    }
}
