package com.getstream.sdk.chat.interfaces;


public interface TokenProvider {
    void onResult(String token, String error);
}
