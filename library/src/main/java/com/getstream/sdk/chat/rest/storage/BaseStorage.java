package com.getstream.sdk.chat.rest.storage;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.controller.APIService;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.UploadFileCallback;

import java.io.File;

public abstract class BaseStorage {
    protected Client client;
    protected APIService mCDNService;

    BaseStorage(Client client) {
        this.client = client;

    }

    public abstract void sendFile(Channel channel, File file, String mimeType, UploadFileCallback callback);
}
