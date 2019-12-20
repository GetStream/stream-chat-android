package com.getstream.sdk.chat.rest.request;

public class HideChannelRequest {
    boolean clearHistory;

    public HideChannelRequest(boolean clearHistory) {
        this.clearHistory = clearHistory;
    }
}
