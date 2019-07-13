package com.getstream.sdk.chat.interfaces;


public interface TokenProvider {
    void onResult(TokenListener listener);
    void onError(String error);

    interface TokenListener {
        void onResult(String token);
    }
}


