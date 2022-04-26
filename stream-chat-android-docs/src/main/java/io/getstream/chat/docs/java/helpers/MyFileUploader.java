package io.getstream.chat.docs.java.helpers;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import io.getstream.chat.android.client.uploader.FileUploader;
import io.getstream.chat.android.client.utils.ProgressCallback;
import io.getstream.chat.android.client.utils.Result;
import kotlin.Unit;

public class MyFileUploader implements FileUploader {
    @Nullable
    @Override
    public Result<String> sendFile(@NotNull String channelType, @NotNull String channelId, @NotNull String userId, @NotNull String connectionId, @NotNull File file, @NotNull ProgressCallback callback) {
         try {
            return Result.success("url");
        } catch (Exception e) {
            return Result.error(e);
        }
    }

    @Nullable
    @Override
    public Result<String> sendFile(@NotNull String channelType, @NotNull String channelId, @NotNull String userId, @NotNull String connectionId, @NotNull File file) {
        try {
            return Result.success("url");
        } catch (Exception e) {
            return Result.error(e);
        }
    }

    @Nullable
    @Override
    public Result<String> sendImage(@NotNull String channelType, @NotNull String channelId, @NotNull String userId, @NotNull String connectionId, @NotNull File file, @NotNull ProgressCallback callback) {
        try {
            return Result.success("url");
        } catch (Exception e) {
            return Result.error(e);
        }
    }

    @Nullable
    @Override
    public Result<String> sendImage(@NotNull String channelType, @NotNull String channelId, @NotNull String userId, @NotNull String connectionId, @NotNull File file) {
        try {
            return Result.success("url");
        } catch (Exception e) {
            return Result.error(e);
        }
    }

    @Override
    public Result<Unit> deleteFile(@NotNull String channelType, @NotNull String channelId, @NotNull String userId, @NotNull String connectionId, @NotNull String url) {
        try {
            return Result.success(Unit.INSTANCE);
        } catch (Exception e) {
            return Result.error(e);
        }
    }

    @Override
    public Result<Unit> deleteImage(@NotNull String channelType, @NotNull String channelId, @NotNull String userId, @NotNull String connectionId, @NotNull String url) {
        try {
            return Result.success(Unit.INSTANCE);
        } catch (Exception e) {
            return Result.error(e);
        }
    }
}
