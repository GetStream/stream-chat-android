package com.getstream.sdk.chat.channels;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelListCallback;
import com.getstream.sdk.chat.rest.request.ChannelQueryRequest;
import com.getstream.sdk.chat.rest.request.QueryChannelsRequest;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.QueryChannelsResponse;
import com.getstream.sdk.chat.utils.ClientError;
import com.getstream.sdk.chat.utils.Observable;
import com.getstream.sdk.chat.utils.Subscription;

import java.util.List;

public class ChannelsRepositoryImpl implements ChannelsRepository {

    private final Client client;
    private final ChannelsCache cache;

    public ChannelsRepositoryImpl(Client client, ChannelsCache cache) {

        this.client = client;
        this.cache = cache;
    }

    @Override
    public Subscription<Channel> getChannel(String channelId) {
        return null;
    }

    @Override
    public Subscription<List<Channel>> getChannels(QueryChannelsRequest query) {

        return (successCallback, errorCallback) -> new Observable<>(successCallback, errorCallback).async((Runnable) () -> {

            //List<Channel> cached = cache.getChannels(query);

            //if (!cached.isEmpty()) {
                //successCallback.onSuccess(cached);
            //}

            client.queryChannels(query, new QueryChannelListCallback() {
                @Override
                public void onSuccess(QueryChannelsResponse response) {
                    //cache.store(query, response.getChannels());
                    successCallback.onSuccess(response.getChannels());
                }

                @Override
                public void onError(String errMsg, int errCode) {
                    errorCallback.error(new ClientError(errMsg, errCode));
                }
            });

        });
    }

    @Override
    public Subscription<Channel> create(Channel channel) {

        return (successCallback, errorCallback) -> new Observable<>(successCallback, errorCallback).async(() -> {

            cache.store(channel);

            client.queryChannel(channel, new ChannelQueryRequest().withMessages(10).withWatch(), new QueryChannelCallback() {
                @Override
                public void onSuccess(ChannelState response) {
                    successCallback.onSuccess(channel);
                }

                @Override
                public void onError(String errMsg, int errCode) {
                    errorCallback.error(new ClientError(errMsg, errCode));
                }
            });
        });
    }
}
