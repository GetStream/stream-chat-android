package com.getstream.sdk.chat

import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.ChatClient

public class MockChatClientBuilder(private val builderFunction: () -> ChatClient = { mock() }) : ChatClient.ChatClientBuilder() {
    override fun buildChatClient(): ChatClient = builderFunction()
}
