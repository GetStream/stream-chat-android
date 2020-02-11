package com.getstream.sdk.chat.notifications;

import com.getstream.sdk.chat.rest.Message;

import org.jetbrains.annotations.NotNull;

public interface NotificationMessageLoadListener {

    void onLoadMessageSuccess(@NotNull Message message);

    void onLoadMessageFail(@NotNull String messageId);
}
