package io.getstream.chat.docs.java.client.helpers;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import io.getstream.result.Error;
import io.getstream.chat.android.models.UploadedFile;
import io.getstream.chat.android.models.UploadedImage;
import io.getstream.chat.android.client.uploader.FileUploader;
import io.getstream.chat.android.client.utils.ProgressCallback;
import io.getstream.result.Result;
import kotlin.Unit;

public class MyFileUploader implements FileUploader {
    @Nullable
    @Override
    public Result<UploadedFile> sendFile(@NotNull String channelType, @NotNull String channelId, @NotNull String userId, @NotNull File file, @NotNull ProgressCallback callback) {
        try {
            return new Result.Success<>(new UploadedFile("file url", "thumb url"));
        } catch (Exception e) {
            return new Result.Failure(new Error.ThrowableError("Could not send file.", e));
        }
    }

    @Nullable
    @Override
    public Result<UploadedFile> sendFile(@NotNull String channelType, @NotNull String channelId, @NotNull String userId, @NotNull File file) {
        try {
            return new Result.Success<>(new UploadedFile("file url", "thumb url"));
        } catch (Exception e) {
            return new Result.Failure(new Error.ThrowableError("Could not send file.", e));
        }
    }

    @Nullable
    @Override
    public Result<UploadedImage> sendImage(@NotNull String channelType, @NotNull String channelId, @NotNull String userId, @NotNull File file, @NotNull ProgressCallback callback) {
        try {
            return new Result.Success<>(new UploadedImage("url", null));
        } catch (Exception e) {
            return new Result.Failure(new Error.ThrowableError("Could not send image.", e));
        }
    }

    @Nullable
    @Override
    public Result<UploadedImage> sendImage(@NotNull String channelType, @NotNull String channelId, @NotNull String userId, @NotNull File file) {
        try {
            return new Result.Success<>(new UploadedImage("url", null));
        } catch (Exception e) {
            return new Result.Failure(new Error.ThrowableError("Could not send image.", e));
        }
    }

    @Override
    public Result<Unit> deleteFile(@NotNull String channelType, @NotNull String channelId, @NotNull String userId, @NotNull String url) {
        try {
            return new Result.Success<>(Unit.INSTANCE);
        } catch (Exception e) {
            return new Result.Failure(new Error.ThrowableError("Could not delete file.", e));
        }
    }

    @Override
    public Result<Unit> deleteImage(@NotNull String channelType, @NotNull String channelId, @NotNull String userId, @NotNull String url) {
        try {
            return new Result.Success<>(Unit.INSTANCE);
        } catch (Exception e) {
            return new Result.Failure(new Error.ThrowableError("Could not delete image.", e));
        }
    }
}
