package io.getstream.chat.android.offline.experimental.extensions

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.experimental.plugin.OfflinePlugin
import io.getstream.chat.android.offline.experimental.plugin.adapter.ChatClientReferenceAdapter
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry

@ExperimentalStreamChatApi
internal val ChatClient.state: StateRegistry
    get() = requireNotNull((plugins.firstOrNull { it.name == OfflinePlugin.MODULE_NAME } as? OfflinePlugin)?.state) {
        "Offline plugin must be configured in ChatClient"
    }

@InternalStreamChatApi
@ExperimentalStreamChatApi
public fun ChatClient.asReferenced(): ChatClientReferenceAdapter = ChatClientReferenceAdapter(this)
