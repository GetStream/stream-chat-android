package com.getstream.sdk.chat.rest;

import android.util.Log;

import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.controller.RestController;
import com.getstream.sdk.chat.rest.apimodel.request.ChannelDetailRequest;
import com.getstream.sdk.chat.rest.apimodel.response.ChannelResponse;
import com.getstream.sdk.chat.model.channel.Channel;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.Global;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class URLSessionService {

    private final String TAG = URLSessionService.class.getSimpleName();

    public interface URLSessionHandler {
        void handleURLResponse(ChannelResponse channelResponse);
    }

    URLSessionHandler urlSessionHandler;

    public void setUrlSessionHandler(URLSessionHandler urlSessionHandler) {
        if (this.urlSessionHandler != null)
            this.urlSessionHandler = null;
        this.urlSessionHandler = urlSessionHandler;
    }

    public void setupURLSession(Channel channel_) {
//        String channelId = "general";
//        String channelName = "The water cooler";
//        String channelImage = "https://images.unsplash.com/photo-1512138664757-360e0aad5132?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=2851&q=80";

        String channelId = channel_.getId();
        String channelName = channel_.getName();
        String channelImage = channel_.getImageURL();

        Channel channel = new Channel(ModelType.channel_messaging, channelId, channelName, channelImage, null);

        Map<String, Object> messages = new HashMap<>();
        messages.put("limit", Constant.DEFAULT_LIMIT);
        Map<String, Object> data = new HashMap<>();
        data.put("name", channel.getName());
        data.put("image", channel.getImageURL());
        data.put("members", Arrays.asList(Global.streamChat.getUser().getId()));

        Log.d(TAG, "Channel Connecting...");

        ChannelDetailRequest request = new ChannelDetailRequest(messages, data, true, true);

        RestController.ChannelDetailCallback callback = (ChannelResponse response) -> {
            Log.d(TAG, "Channel Connected!");
            Log.d(TAG, "Channel Id : " + response.getChannel().getId());
            Log.d(TAG, "Message Count : " + response.getMessages().size());
            if (!response.getMessages().isEmpty())
                Global.setStartDay(response.getMessages(),null);
            urlSessionHandler.handleURLResponse(response);
        };
        Global.mRestController.channelDetailWithID(channel.getId(), request, callback, (String errMsg, int errCode) -> {
            Log.d(TAG, "Failed Connect Channel : " + errMsg);
        });
    }
}
