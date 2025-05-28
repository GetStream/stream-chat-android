/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ui.common.feature.channel.draft

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.query.CreateChannelParams
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.MemberData
import io.getstream.chat.android.ui.common.state.channel.draft.DraftChannelViewState
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.onSuccessSuspend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Controller responsible for managing the state and events related to the draft channel.
 */
@InternalStreamChatApi
public class DraftChannelViewController(
    private val memberIds: List<String>,
    private val scope: CoroutineScope,
    private val chatClient: ChatClient = ChatClient.instance(),
) {
    private val logger by taggedLogger("Chat:NewDirectChannelViewController")

    private lateinit var cid: String

    /**
     * A [StateFlow] representing the current state of the draft channel.
     */
    public val state: StateFlow<DraftChannelViewState> =
        flow { createDraftChannel(::emit) }
            .map { channel ->
                cid = channel.cid
                DraftChannelViewState.Content(channel = channel)
            }.stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_IN_MILLIS),
                initialValue = DraftChannelViewState.Loading,
            )

    private val _events = MutableSharedFlow<DraftChannelViewEvent>(extraBufferCapacity = 1)

    /**
     * A [SharedFlow] that emits one-shot events related to the draft channel, such as navigation events.
     */
    public val events: SharedFlow<DraftChannelViewEvent> = _events.asSharedFlow()

    /**
     * Handles actions related to the draft channel view.
     *
     * @param action The [DraftChannelViewAction] representing the action to be handled.
     */
    public fun onViewAction(action: DraftChannelViewAction) {
        logger.d { "[onViewAction] action: $action" }
        when (action) {
            DraftChannelViewAction.MessageSent -> updateChannel()
        }
    }

    private suspend fun createDraftChannel(onSuccess: suspend (channel: Channel) -> Unit = {}) {
        logger.d { "[createDraftChannel] memberIds: $memberIds" }

        val onError: (Error) -> Unit = { error ->
            logger.e { "[createDraftChannel] error: $error" }
            _events.tryEmit(DraftChannelViewEvent.DraftChannelError)
        }

        runCatching {
            requireNotNull(chatClient.getCurrentUser()?.id) { "User not connected" }
        }.onSuccess { currentUserId ->
            chatClient.createChannel(
                channelType = "messaging",
                channelId = "",
                params = CreateChannelParams(
                    members = (memberIds + currentUserId).map(::MemberData),
                    extraData = mapOf("draft" to true),
                ),
            ).await()
                .onSuccessSuspend(onSuccess)
                .onError(onError)
        }.onFailure { cause ->
            onError(Error.ThrowableError(message = cause.message.orEmpty(), cause = cause))
        }
    }

    private fun updateChannel() {
        logger.d { "[updateChannel] cid: $cid" }
        scope.launch {
            chatClient.channel(cid)
                .update(message = null, extraData = mapOf("draft" to false))
                .await()
                .onSuccess {
                    _events.tryEmit(DraftChannelViewEvent.NavigateToChannel(cid))
                }
                .onError {
                    logger.e { "[updateChannel] Failed to update channel: $cid" }
                    _events.tryEmit(DraftChannelViewEvent.DraftChannelError)
                }
        }
    }
}

private const val STOP_TIMEOUT_IN_MILLIS = 5_000L
