package io.getstream.chat.android.offline.experimental.persistance

import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.OfflinePluginImpl
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry

/**
 * The entry point of all offline state ([OfflinePluginImpl.state]) and behavior ([OfflinePluginImpl.logic]).
 */
@ExperimentalStreamChatApi
internal class OfflineSupport(
    val state: StateRegistry,
    val logic: LogicRegistry,
    val globalState: GlobalState
)
