package io.getstream.chat.android.offline.experimental.extensions

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.experimental.plugin.OfflinePlugin
import io.getstream.chat.android.offline.experimental.plugin.adapter.ChatClientReferenceAdapter
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry

/**
 * [StateRegistry] instance that contains all state objects exposed in offline plugin.
 */
@ExperimentalStreamChatApi
internal val ChatClient.state: StateRegistry
    get() = requireNotNull(offlinePlugin?.state) {
        "Offline plugin must be configured in ChatClient"
    }

/**
 * [LogicRegistry] instance that contains all objects responsible for handling logic in offline plugin.
 */
@ExperimentalStreamChatApi
internal val ChatClient.logic: LogicRegistry
    get() = requireNotNull(offlinePlugin?.logic) {
        "Offline plugin must be configured in ChatClient"
    }

/**
 * Returns [OfflinePlugin] if configured.
 */
@ExperimentalStreamChatApi
private val ChatClient.offlinePlugin: OfflinePlugin?
    get() = plugins.firstOrNull { it.name == OfflinePlugin.MODULE_NAME } as? OfflinePlugin

@InternalStreamChatApi
@ExperimentalStreamChatApi
public fun ChatClient.asReferenced(): ChatClientReferenceAdapter = ChatClientReferenceAdapter(this)
