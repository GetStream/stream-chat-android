package com.getstream.sdk.chat.channels;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.request.ChannelQueryRequest;
import com.getstream.sdk.chat.utils.Subscription;
import com.getstream.sdk.chat.utils.UseCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CreateNewChannel extends UseCase {

    private final ChannelsRepository channelsRepository;
    private final Client client;

    public CreateNewChannel(ChannelsRepository channelsRepository, Client client) {

        this.channelsRepository = channelsRepository;
        this.client = client;
    }

    public Subscription<Channel> create(String name) {

        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("name", name);

        List<String> members = new ArrayList<>();
        members.add(client.getState().user.getId());
        extraData.put("members", members);

        String channelId = name.replaceAll(" ", "-").toLowerCase();
        Channel channel = new Channel(client, ModelType.channel_messaging, channelId, extraData);

        return channelsRepository.create(channel);
    }
}