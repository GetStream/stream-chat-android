package com.getstream.sdk.chat.rest.core.providers;

import com.getstream.sdk.chat.interfaces.CachedTokenProvider;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.storage.BaseStorage;

/*
 * Created by Anton Bevza on 2019-10-28.
 */
public interface UploadStorageProvider {
    BaseStorage provideUploadStorage(CachedTokenProvider tokenProvider, Client client);
}
