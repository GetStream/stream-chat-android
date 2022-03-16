package io.getstream.chat.android.offline.plugin.extensions.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.offline.plugin.extensions.state
import io.getstream.chat.android.offline.plugin.internal.logic.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.internal.ChatClientStateCalls
import kotlinx.coroutines.CoroutineScope

/**
 * [LogicRegistry] instance that contains all objects responsible for handling logic in offline plugin.
 */
internal val ChatClient.logic: LogicRegistry
    get() = requireNotNull(LogicRegistry.get()) {
        "Offline plugin must be configured in ChatClient. You must provide StreamOfflinePluginFactory as a " +
            "PluginFactory to be able to use LogicRegistry and StateRegistry from the SDK"
    }

/**
 * Intermediate class to request ChatClient class as states
 *
 * @return [ChatClientStateCalls]
 */
internal fun ChatClient.requestsAsState(scope: CoroutineScope): ChatClientStateCalls =
    ChatClientStateCalls(this, state, scope)
