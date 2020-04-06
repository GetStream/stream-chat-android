package com.getstream.sdk.chat.rest.controller;

import com.getstream.sdk.chat.BuildConfig;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.interfaces.CachedTokenProvider;
import com.getstream.sdk.chat.rest.response.ErrorResponse;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenAuthInterceptor implements Interceptor {

    private final String TAG = getClass().getSimpleName();

    private CachedTokenProvider tokenProvider;
    private String token;

    TokenAuthInterceptor(CachedTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        final CountDownLatch latch = new CountDownLatch(token == null ? 1 : 0);

        if (token == null) {
            tokenProvider.getToken(token -> {
                this.token = token;
                latch.countDown();
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            StreamChat.getLogger().logT(this, e);
        }

        Request request = chain.request();
        request = addTokenHeader(request);
        request = addVersionHeader(request);
        Response response = chain.proceed(request);

        // check the error and only hit this path if the token was expired (error response code)
        if (!response.isSuccessful()) {
            ErrorResponse err = ErrorResponse.parseError(response);
            if (err.getCode() == ErrorResponse.TOKEN_EXPIRED_CODE) {
                StreamChat.getLogger().logD(this, "Retrying new request");
                token = null; // invalidate local cache
                tokenProvider.tokenExpired();
                response.close();
                response = chain.proceed(request);
            }
        }

        return response;
    }

    private Request addTokenHeader(Request req) {
        return req.newBuilder().header("Authorization", token).build();
    }

    private Request addVersionHeader(Request req) {
        return req.newBuilder().header(Constant.HEADER_VERSION, Utils.version()).build();
    }

}
