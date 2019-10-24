package com.getstream.sdk.chat.rest.core.providers;

import com.getstream.sdk.chat.interfaces.CachedTokenProvider;
import com.getstream.sdk.chat.rest.controller.APIService;
import com.getstream.sdk.chat.rest.controller.RetrofitClient;
import com.getstream.sdk.chat.rest.core.ApiClientOptions;

/*
 * Created by Anton Bevza on 2019-10-24.
 */
public class ApiServiceProvider {

    private ApiClientOptions apiClientOptions;

    public ApiServiceProvider(ApiClientOptions options) {
        this.apiClientOptions = options;
    }

    public APIService provideApiService(CachedTokenProvider tokenProvider) {
        return RetrofitClient.getAuthorizedClient(tokenProvider, apiClientOptions).create(APIService.class);
    }
}
