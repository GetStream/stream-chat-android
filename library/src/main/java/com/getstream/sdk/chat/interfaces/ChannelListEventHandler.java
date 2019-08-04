package com.getstream.sdk.chat.interfaces;


public interface ChannelListEventHandler {
    void updateChannels();
    void handleConnection();
    void onConnectionFailed(String errMsg, int errCode);
}
