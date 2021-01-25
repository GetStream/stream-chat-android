package io.getstream.chat.android.client.api2

import io.getstream.chat.android.client.api.ChatApi

internal class MoshiChatApi(
    private val legacyApiDelegate: ChatApi,
) : ChatApi by legacyApiDelegate
