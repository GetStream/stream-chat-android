package com.getstream.sdk.chat.api.utils;

import com.getstream.sdk.chat.rest.core.ApiClientOptions;

/*
 * Created by Anton Bevza on 2019-10-21.
 */
public class TestApiClientOptions extends ApiClientOptions {

    private String baseUrl;
    private static int defaultTimeout = 100;
    private static int defaultCDNTimeout = 100;

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
