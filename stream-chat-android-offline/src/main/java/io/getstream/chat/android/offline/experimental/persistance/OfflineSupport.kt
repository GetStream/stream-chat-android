package io.getstream.chat.android.offline.experimental.persistance

import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry

@ExperimentalStreamChatApi
internal class OfflineSupport(
    val state: StateRegistry,
    val logic: LogicRegistry,
    val globalState: GlobalState
)
