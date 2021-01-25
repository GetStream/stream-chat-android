package io.getstream.chat.android.client.api2

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.GsonChatApi

internal class MoshiChatApi(
    private val legacyApiDelegate: GsonChatApi,
) : ChatApi by legacyApiDelegate
