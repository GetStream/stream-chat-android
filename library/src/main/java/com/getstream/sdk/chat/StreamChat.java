package com.getstream.sdk.chat;

import android.content.Context;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.core.Client;

import java.util.HashMap;

public class StreamChat {
    private static Client sharedInstance;

    public static synchronized Client getInstance() {
        if (sharedInstance == null) {
            throw new RuntimeException("You must initialize the API client first, make sure to call StreamChat.initialize");
        } else {
            return sharedInstance;
        }
    }

    public static synchronized boolean init(String apiKey, Context context) {
        if (sharedInstance != null) {
            throw new RuntimeException("StreamChat is already initialized!");
        }
        sharedInstance = new Client(apiKey);
        return true;
    }

    public static synchronized Channel channel(String type, String id){
        return getInstance().channel(type, id);
    }

    public static synchronized Channel channel(String type, String id, HashMap<String, Object> extraData){
        return getInstance().channel(type, id, extraData);
    }

    //TODO: once we are happy with this, we should proxy all calls from here to client :)
}
