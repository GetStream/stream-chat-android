package com.getstream.sdk.chat.rest.core.providers;

import com.getstream.sdk.chat.interfaces.CachedTokenProvider;
import com.getstream.sdk.chat.rest.core.ApiClientOptions;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.storage.BaseStorage;
import com.getstream.sdk.chat.rest.storage.StreamPublicStorage;

/*
 * Created by Anton Bevza on 2019-10-24.
 */
public class StreamUploadStorageProvider implements UploadStorageProvider{

    private ApiClientOptions apiClientOptions;

    public StreamUploadStorageProvider(ApiClientOptions options) {
        this.apiClientOptions = options;
    }

    public BaseStorage provideUploadStorage(CachedTokenProvider tokenProvider, Client client) {
        return new StreamPublicStorage(client, tokenProvider, apiClientOptions);
    }
}
