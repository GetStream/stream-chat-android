package com.getstream.sdk.chat.rest.core.providers;

import com.getstream.sdk.chat.interfaces.CachedTokenProvider;
import com.getstream.sdk.chat.rest.controller.APIService;
import com.getstream.sdk.chat.rest.controller.RetrofitClient;
import com.getstream.sdk.chat.rest.core.ApiClientOptions;

/*
 * Created by Anton Bevza on 2019-10-24.
 */
public class StreamApiServiceProvider implements ApiServiceProvider {

    private ApiClientOptions apiClientOptions;

    public StreamApiServiceProvider(ApiClientOptions options) {
        this.apiClientOptions = options;
    }

    @Override
    public APIService provideApiService(CachedTokenProvider tokenProvider, boolean anonymousAuth) {
        return RetrofitClient.getClient(apiClientOptions, tokenProvider, anonymousAuth).create(APIService.class);
    }
}
