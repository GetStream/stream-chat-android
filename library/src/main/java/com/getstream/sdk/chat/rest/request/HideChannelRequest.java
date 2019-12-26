package com.getstream.sdk.chat.rest.request;

public class HideChannelRequest {
    @SuppressWarnings("WeakerAccess")
    boolean clearHistory;

    public HideChannelRequest(boolean clearHistory) {
        this.clearHistory = clearHistory;
    }

    public HideChannelRequest() {
    }
}
