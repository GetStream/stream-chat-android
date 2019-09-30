package com.getstream.sdk.chat.interfaces;

public interface CachedTokenProvider {
    void getToken(TokenProvider.TokenProviderListener listener);
    void tokenExpired();
}