package com.getstream.sdk.chat

import io.getstream.chat.android.client.ChatClient
import org.mockito.kotlin.mock

public class MockChatClientBuilder(private val builderFunction: () -> ChatClient = { mock() }) : ChatClient.ChatClientBuilder() {
    override fun internalBuild(): ChatClient = builderFunction()
}
