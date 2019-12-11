package com.getstream.sdk.chat.rest.core.providers;

import com.getstream.sdk.chat.interfaces.CachedTokenProvider;
import com.getstream.sdk.chat.rest.controller.APIService;

/*
 * Created by Anton Bevza on 2019-10-28.
 */
public interface ApiServiceProvider {
    APIService provideApiService(CachedTokenProvider tokenProvider, boolean anonymousAuth);
}
