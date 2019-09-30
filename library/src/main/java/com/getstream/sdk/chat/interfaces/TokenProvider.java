package com.getstream.sdk.chat.interfaces;


public interface TokenProvider {
    void getToken(TokenProviderListener listener);

    interface TokenProviderListener {
        void onSuccess(String token);
    }
}
