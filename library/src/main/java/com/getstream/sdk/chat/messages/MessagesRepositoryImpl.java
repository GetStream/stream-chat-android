package com.getstream.sdk.chat.messages;

import com.getstream.sdk.chat.enums.FilterObject;
import com.getstream.sdk.chat.enums.QuerySort;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelListCallback;
import com.getstream.sdk.chat.rest.request.QueryChannelsRequest;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.QueryChannelsResponse;
import com.getstream.sdk.chat.utils.*;

import java.util.List;

public class MessagesRepositoryImpl implements MessagesRepository {

    private final Client client;

    public MessagesRepositoryImpl(Client client) {
        this.client = client;
    }

    @Override
    public Subscription<List<Message>> getMessages(int offset, int limit, String cid) {
        return new Subscription<List<Message>>() {
            @Override
            public Observable<List<Message>> subscribe(SuccessCallback<List<Message>> successCallback, ErrorCallback errorCallback) {


                return new Observable<>(successCallback, errorCallback).async(new Runnable() {
                    @Override
                    public void run() {


                        Channel channel = new Channel();
                        channel.setCid(cid);
                        channel.setType(ModelType.channel_messaging);

                        FilterObject filter = new FilterObject("cid", cid);
                        QueryChannelsRequest q = new QueryChannelsRequest(filter, new QuerySort());

                        client.queryChannels(q, new QueryChannelListCallback() {
                            @Override
                            public void onSuccess(QueryChannelsResponse response) {
                                List<Channel> channels = response.getChannels();
                                if (channels != null || !channels.isEmpty()) {
                                    Channel ch = channels.get(0);
                                    ChannelState chS = response.getChannelStates().get(0);
                                    List<Message> messages = ch.getChannelState().messages;
                                    successCallback.onSuccess(messages);
                                } else {
                                    errorCallback.error(new ClientError("No channels with cid:" + cid, 0));
                                }
                            }

                            @Override
                            public void onError(String errMsg, int errCode) {
                                errorCallback.error(new ClientError(errMsg, errCode));
                            }
                        });
                    }
                });
            }
        };
    }

    @Override
    public Subscription<Void> sendMessage(Message message) {
        return null;
    }
}
