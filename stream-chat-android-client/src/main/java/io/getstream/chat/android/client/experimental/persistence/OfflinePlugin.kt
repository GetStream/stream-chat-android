package io.getstream.chat.android.client.experimental.persistence

import io.getstream.chat.android.client.experimental.plugin.Plugin
import io.getstream.chat.android.client.experimental.plugin.listeners.ChannelMarkReadListener
import io.getstream.chat.android.client.experimental.plugin.listeners.EditMessageListener
import io.getstream.chat.android.client.experimental.plugin.listeners.GetMessageListener
import io.getstream.chat.android.client.experimental.plugin.listeners.HideChannelListener
import io.getstream.chat.android.client.experimental.plugin.listeners.MarkAllReadListener
import io.getstream.chat.android.client.experimental.plugin.listeners.QueryChannelListener
import io.getstream.chat.android.client.experimental.plugin.listeners.QueryChannelsListener
import io.getstream.chat.android.client.experimental.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.core.ExperimentalStreamChatApi

@ExperimentalStreamChatApi
public interface OfflinePlugin :
    Plugin,
    QueryChannelsListener,
    QueryChannelListener,
    ThreadQueryListener,
    ChannelMarkReadListener,
    EditMessageListener,
    GetMessageListener,
    HideChannelListener,
    MarkAllReadListener {

    public fun clear()
}
