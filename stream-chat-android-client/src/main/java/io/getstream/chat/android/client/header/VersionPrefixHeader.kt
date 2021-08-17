package io.getstream.chat.android.client.header

import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public enum class VersionPrefixHeader(public val prefix: String) {
    CORE_ANDROID("stream-chat-android-"),
    COMPOSE("stream-chat-compose-")
    // TODO - @Rafal - we'll need to add a value for the old UI component too
}
