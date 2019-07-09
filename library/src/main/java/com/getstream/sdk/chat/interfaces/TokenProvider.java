package com.getstream.sdk.chat.interfaces;


public interface DevTokenProvider {
    void onResult(String token, String error);
}
