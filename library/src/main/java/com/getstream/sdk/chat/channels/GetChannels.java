package com.getstream.sdk.chat.channels;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.QueryChannelsQ;
import com.getstream.sdk.chat.rest.request.QueryChannelsRequest;
import com.getstream.sdk.chat.utils.Subscription;
import com.getstream.sdk.chat.utils.UseCase;

import java.util.List;

public class GetChannels extends UseCase {

    private final ChannelsRepository channelsRepository;

    public GetChannels(ChannelsRepository channelsRepository) {

        this.channelsRepository = channelsRepository;
    }

    public Subscription<List<Channel>> get(QueryChannelsRequest query) {
        return channelsRepository.getChannels(query);
    }
}