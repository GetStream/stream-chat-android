package com.getstream.sdk.chat;

import android.content.Context;

import com.getstream.sdk.chat.rest.core.Client;

public class StreamChat {
    private static Client INSTANCE;

    public static synchronized Client getInstance(final Context context) {
        if (INSTANCE == null) {
            throw new RuntimeException("You must initialize the API client first, make sure to call StreamChat.initialize");
        } else {
            return INSTANCE;
        }
    }

    public static synchronized boolean init(String apiKey, Context context) {
        if (INSTANCE != null) {
            throw new RuntimeException("StreamChat is already initialized!");
        }
        synchronized (Client.class) {
            if (INSTANCE == null) {
                INSTANCE = new Client(apiKey);
            }
        }
        return true;
    }

}
