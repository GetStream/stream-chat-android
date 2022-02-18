package io.getstream.chat.android.offline.experimental.extensions

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.experimental.plugin.adapter.ChatClientReferenceAdapter
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry

/**
 * [StateRegistry] instance that contains all state objects exposed in offline plugin.
 */
@ExperimentalStreamChatApi
internal val ChatClient.state: StateRegistry
    get() = requireNotNull(StateRegistry.get()) {
        "Offline plugin must be configured in ChatClient. You must provide StreamOfflinePluginFactory as a " +
            "PluginFactory to be able to use LogicRegistry and StateRegistry from the SDK"
    }

/**
 * [LogicRegistry] instance that contains all objects responsible for handling logic in offline plugin.
 */
@ExperimentalStreamChatApi
internal val ChatClient.logic: LogicRegistry
    get() = requireNotNull(LogicRegistry.get()) {
        "Offline plugin must be configured in ChatClient. You must provide StreamOfflinePluginFactory as a " +
            "PluginFactory to be able to use LogicRegistry and StateRegistry from the SDK"
    }

@InternalStreamChatApi
@ExperimentalStreamChatApi
public fun ChatClient.asReferenced(): ChatClientReferenceAdapter = ChatClientReferenceAdapter(this)
