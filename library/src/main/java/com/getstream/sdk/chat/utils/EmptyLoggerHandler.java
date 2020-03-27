package com.getstream.sdk.chat.utils;

import org.jetbrains.annotations.NotNull;

import io.getstream.chat.android.client.logger.ChatLoggerHandler;

public class EmptyLoggerHandler implements ChatLoggerHandler {
    @Override
    public void logD(@NotNull Object tag, @NotNull String s) {

    }

    @Override
    public void logE(@NotNull Object tag, @NotNull String s) {

    }

    @Override
    public void logE(@NotNull Object tag, @NotNull String s, @NotNull Throwable throwable) {

    }

    @Override
    public void logI(@NotNull Object tag, @NotNull String s) {

    }

    @Override
    public void logT(@NotNull Object tag, @NotNull Throwable throwable) {

    }

    @Override
    public void logT(@NotNull Throwable throwable) {

    }

    @Override
    public void logW(@NotNull Object tag, @NotNull String s) {

    }
}
