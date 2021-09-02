package io.getstream.chat.android.client.experimental.plugin.listeners

import io.getstream.chat.android.core.ExperimentalStreamChatApi

@ExperimentalStreamChatApi
public interface OperationListenersFacade : QueryChannelsListener, QueryChannelListener, SendMessageListener
