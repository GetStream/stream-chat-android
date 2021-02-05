package io.getstream.chat.docs.java.helpers;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import io.getstream.chat.android.client.uploader.FileUploader;
import io.getstream.chat.android.client.utils.ProgressCallback;

public class MyFileUploader implements FileUploader {
    @Nullable
    @Override
    public String sendFile(@NotNull String channelType, @NotNull String channelId, @NotNull String userId, @NotNull String connectionId, @NotNull File file, @NotNull ProgressCallback callback) {
        return null;
    }

    @Nullable
    @Override
    public String sendFile(@NotNull String channelType, @NotNull String channelId, @NotNull String userId, @NotNull String connectionId, @NotNull File file) {
        return null;
    }

    @Nullable
    @Override
    public String sendImage(@NotNull String channelType, @NotNull String channelId, @NotNull String userId, @NotNull String connectionId, @NotNull File file, @NotNull ProgressCallback callback) {
        return null;
    }

    @Nullable
    @Override
    public String sendImage(@NotNull String channelType, @NotNull String channelId, @NotNull String userId, @NotNull String connectionId, @NotNull File file) {
        return null;
    }

    @Override
    public void deleteFile(@NotNull String channelType, @NotNull String channelId, @NotNull String userId, @NotNull String connectionId, @NotNull String url) {

    }

    @Override
    public void deleteImage(@NotNull String channelType, @NotNull String channelId, @NotNull String userId, @NotNull String connectionId, @NotNull String url) {

    }
}
