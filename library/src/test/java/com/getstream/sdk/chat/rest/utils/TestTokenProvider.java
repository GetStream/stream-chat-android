package com.getstream.sdk.chat.rest.utils;

import com.getstream.sdk.chat.interfaces.TokenProvider;

/*
 * Created by Anton Bevza on 2019-10-18.
 */
public class TestTokenProvider implements TokenProvider {
    public static String TEST_TOKEN = "testToken";

    @Override
    public void getToken(TokenProvider.TokenProviderListener listener) {
        listener.onSuccess(TEST_TOKEN);
    }
}
