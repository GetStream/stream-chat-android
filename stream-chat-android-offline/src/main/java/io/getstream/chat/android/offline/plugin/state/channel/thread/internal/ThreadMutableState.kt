package io.getstream.chat.android.offline.plugin.state.channel.thread.internal

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.offline.extensions.internal.wasCreatedAfterOrAt
import io.getstream.chat.android.offline.plugin.state.channel.internal.ChannelMutableState
import io.getstream.chat.android.offline.plugin.state.channel.thread.ThreadState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class ThreadMutableState(
    override val parentId: String,
    private val channelMutableState: ChannelMutableState,
    scope: CoroutineScope,
) : ThreadState {

    internal val _loadingOlderMessages = MutableStateFlow(false)
    internal val _endOfOlderMessages = MutableStateFlow(false)
    internal val _oldestInThread: MutableStateFlow<Message?> = MutableStateFlow(null)

    override val oldestInThread: StateFlow<Message?> = _oldestInThread

    internal val threadMessages: Flow<List<Message>> =
        channelMutableState.messageList.map { messageList -> messageList.filter { it.id == parentId || it.parentId == parentId } }
    internal val sortedVisibleMessages: StateFlow<List<Message>> = threadMessages.map { threadMessages ->
        threadMessages.sortedBy { m -> m.createdAt ?: m.createdLocallyAt }
            .filter {
                channelMutableState.hideMessagesBefore == null ||
                    it.wasCreatedAfterOrAt(channelMutableState.hideMessagesBefore)
            }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val messages: StateFlow<List<Message>> = sortedVisibleMessages
    override val loadingOlderMessages: StateFlow<Boolean> = _loadingOlderMessages
    override val endOfOlderMessages: StateFlow<Boolean> = _endOfOlderMessages
}

internal fun ThreadState.toMutableState(): ThreadMutableState = this as ThreadMutableState
