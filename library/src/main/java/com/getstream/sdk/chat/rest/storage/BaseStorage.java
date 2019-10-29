package com.getstream.sdk.chat.rest.storage;

import com.bumptech.glide.load.model.GlideUrl;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.controller.APIService;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.CompletableCallback;
import com.getstream.sdk.chat.rest.interfaces.UploadFileCallback;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public abstract class BaseStorage {
    protected Client client;
    protected APIService mCDNService;

    BaseStorage(Client client) {
        this.client = client;

    }

    public abstract void sendFile(Channel channel, File file, String mimeType, UploadFileCallback callback);

    /**
     * Delete a file with a given URL.
     *
     * @param channel  the channel where needs to delete the file
     * @param url      the file URL
     * @param callback the result callback
     */
    public abstract void deleteFile(@NotNull Channel channel, @NotNull String url, @NotNull CompletableCallback callback);

    /**
     * Delete a image with a given URL.
     *
     * @param channel  the channel where needs to delete the image
     * @param url      the image URL
     * @param callback the result callback
     */
    public abstract void deleteImage(@NotNull Channel channel, @NotNull String url, @NotNull CompletableCallback callback);

    /**
     * signFileUrl allows you to add a token your file for authorization
     *
     * @param url
     * @return
     */
    public abstract String signFileUrl(String url);

    /**
     * signGlideUrl returns a GlidUrl for the given url string.
     * This allows you to add a token to either the headers or the query params
     *
     * @param url
     * @return
     */
    public abstract GlideUrl signGlideUrl(String url);
}
