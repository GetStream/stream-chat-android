package com.getstream.sdk.chat.channels;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.request.QueryChannelsRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelsCacheImpl implements ChannelsCache {

    private Map<String, List<Channel>> queryMap = new HashMap<>();
    private Map<String, Channel> idMap = new HashMap<>();

    @Override
    public Channel getChannel(String channelId) {
        if (idMap.containsKey(channelId)) {
            return idMap.get(channelId);
        } else {
            return null;
        }
    }

    @Override
    public List<Channel> getChannels(QueryChannelsRequest query) {
        String id = query.query().getId();
        if (queryMap.containsKey(id)) {
            return queryMap.get(id);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void store(Channel channel) {
        idMap.put(channel.getCid(), channel);
    }

    @Override
    public void store(QueryChannelsRequest query, List<Channel> channels) {
        queryMap.put(query.query().getId(), channels);
    }
}
