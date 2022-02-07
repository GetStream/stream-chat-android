package io.getstream.chat.android.offline.experimental.extensions

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.experimental.plugin.OfflinePluginImpl
import io.getstream.chat.android.offline.experimental.plugin.adapter.ChatClientReferenceAdapter
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry

/**
 * [StateRegistry] instance that contains all state objects exposed in offline plugin.
 */
@ExperimentalStreamChatApi
internal val ChatClient.state: StateRegistry
    get() = requireNotNull(offlinePluginImpl?.state) {
        "Offline plugin must be configured in ChatClient"
    }

/**
 * [LogicRegistry] instance that contains all objects responsible for handling logic in offline plugin.
 */
@ExperimentalStreamChatApi
internal val ChatClient.logic: LogicRegistry
    get() = requireNotNull(offlinePluginImpl?.logic) {
        "Offline plugin must be configured in ChatClient"
    }

/**
 * Returns [OfflinePluginImpl] if configured.
 */
@ExperimentalStreamChatApi
private val ChatClient.offlinePluginImpl: OfflinePluginImpl?
    get() = plugins.firstOrNull { it.name == OfflinePluginImpl.MODULE_NAME } as? OfflinePluginImpl

@InternalStreamChatApi
@ExperimentalStreamChatApi
public fun ChatClient.asReferenced(): ChatClientReferenceAdapter = ChatClientReferenceAdapter(this)
