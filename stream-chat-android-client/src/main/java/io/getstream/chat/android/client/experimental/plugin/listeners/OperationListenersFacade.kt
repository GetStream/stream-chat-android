package io.getstream.chat.android.client.experimental.plugin.listeners

import io.getstream.chat.android.core.ExperimentalStreamChatApi

@ExperimentalStreamChatApi
/** Facade interface of all listeners. Every plugin should implement it. */
public interface OperationListenersFacade :
    QueryChannelsListener,
    QueryChannelListener,
    SendMessageListener,
    ThreadQueryListener
