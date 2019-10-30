package com.getstream.sdk.chat.rest.core.providers;

import android.content.Context;

import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.storage.Storage;
import com.getstream.sdk.chat.storage.StreamStorage;

/*
 * Created by Anton Bevza on 2019-10-29.
 */
public class StreamStorageProvider implements StorageProvider {

    @Override
    public Storage provideStorage(Client client, final Context context, final boolean enabled) {
        return StreamStorage.getStorage(client, context, enabled);
    }
}
