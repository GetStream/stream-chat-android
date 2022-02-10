package io.getstream.chat.android.offline.experimental.persistance

import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.OfflinePlugin
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry

/**
 * The entry point of all offline state ([OfflinePlugin.state]) and behavior ([OfflinePlugin.logic]).
 */
@ExperimentalStreamChatApi
internal class OfflineSupport(
    val state: StateRegistry,
    val logic: LogicRegistry,
    val globalState: GlobalState
)
