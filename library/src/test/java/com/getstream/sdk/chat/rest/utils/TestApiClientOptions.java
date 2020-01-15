package com.getstream.sdk.chat.rest.utils;

import com.getstream.sdk.chat.BuildConfig;
import com.getstream.sdk.chat.rest.core.ApiClientOptions;

/*
 * Created by Anton Bevza on 2019-10-21.
 */
public class TestApiClientOptions extends ApiClientOptions {

    private String baseUrl;
    private static int defaultTimeout = BuildConfig.DEFAULT_API_TIMEOUT;
    private static int defaultCDNTimeout = BuildConfig.DEFAULT_API_TIMEOUT;

    public TestApiClientOptions(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public String getHttpURL() {
        return baseUrl;
    }

    @Override
    public String getCdnHttpURL() {
        return baseUrl;
    }

    @Override
    public String getWssURL() {
        return baseUrl;
    }

    @Override
    public int getTimeout() {
        return defaultTimeout;
    }

    @Override
    public int getCdntimeout() {
        return defaultCDNTimeout;
    }
}
