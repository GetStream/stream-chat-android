package io.getstream.chat.android.client.header

import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public enum class VersionPrefixHeader(public val prefix: String) {
    DEFAULT("stream-chat-android-"),
    OLD_UI_COMPONENTS("stream-chat-android-old-ui-"),
    UI_COMPONENTS("stream-chat-android-ui-components-"),
    COMPOSE("stream-chat-android-compose-")
}
