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

package io.getstream.chat.android.ui.common.feature.channel.info

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.extensions.isGroupChannel
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.extensions.watchChannelAsState
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoEvent
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoViewState
import io.getstream.chat.android.ui.common.utils.ExpandableList
import io.getstream.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@InternalStreamChatApi
public class ChannelInfoController(
    cid: String,
    private val scope: CoroutineScope,
    private val chatClient: ChatClient = ChatClient.instance(),
    channelState: Flow<ChannelState> = chatClient
        .watchChannelAsState(cid = cid, messageLimit = 0, coroutineScope = scope)
        .filterNotNull(),
    private val channelClient: ChannelClient = chatClient.channel(cid),
) {
    private val logger by taggedLogger("Chat:ChannelInfoController")

    private val _state = MutableStateFlow(ChannelInfoViewState())

    public val state: StateFlow<ChannelInfoViewState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ChannelInfoEvent>(extraBufferCapacity = 1)

    public val events: SharedFlow<ChannelInfoEvent> = _events.asSharedFlow()

    init {
        channelState
            .flatMapLatest { channelState ->
                logger.d { "[onChannelState]" }
                combine(
                    channelState.channelData.onEach { logger.d { "[onChannelData] cid: ${it.cid}, name: ${it.name}" } },
                    channelState.members.onEach { logger.d { "[onMembers] size: ${it.size}" } },
                    channelState.muted.onEach { logger.d { "[onMuted] $it" } },
                    channelState.hidden.onEach { logger.d { "[onHidden] $it" } },
                    ::ChannelInfoData,
                )
            }
            .distinctUntilChanged()
            .onEach { (channel, members, isMuted, isHidden) ->
                onChannelInfoData(channel, members, isMuted, isHidden)
            }
            .launchIn(scope)
    }

    private fun onChannelInfoData(
        channel: ChannelData,
        members: List<Member>,
        isMuted: Boolean,
        isHidden: Boolean,
    ) {
        logger.d {
            "[onChannelInfoData] cid: ${channel.cid}, " +
                "name: ${channel.name}, " +
                "members: ${members.size}, " +
                "isMuted: $isMuted, " +
                "isHidden: $isHidden"
        }

        val channelMembers = members
            .run { takeIf { channel.isGroupChannel } ?: filterNotCurrentUser() }
            .map { member -> member.toViewState(channel.createdBy) }

        _state.update { currentState ->
            currentState.copy(
                content = when (currentState.content) {
                    is ChannelInfoViewState.Content.Loading -> {
                        ChannelInfoViewState.Content.Success(
                            members = ExpandableList(
                                items = channelMembers,
                                minimumVisibleItems = MINIMUM_VISIBLE_MEMBERS,
                            ),
                            name = channel.name,
                            isMuted = isMuted,
                            isHidden = isHidden,
                        )
                    }

                    is ChannelInfoViewState.Content.Success -> {
                        currentState.content.copy(
                            members = currentState.content.members.copy(
                                items = channelMembers,
                            ),
                            name = channel.name,
                            isMuted = isMuted,
                            isHidden = isHidden,
                        )
                    }

                    else -> currentState.content
                },
            )
        }
    }

    public fun expandMembers() {
        _state.updateOnSuccessContent { content ->
            content.copy(
                members = content.members.copy(
                    isCollapsed = false,
                ),
            )
        }
    }

    public fun collapseMembers() {
        _state.updateOnSuccessContent { content ->
            content.copy(
                members = content.members.copy(
                    isCollapsed = true,
                ),
            )
        }
    }

    public fun updateName(name: String) {
        scope.launch {
            channelClient.updatePartial(set = mapOf("name" to name)).await()
                .onError { error ->
                    _events.tryEmit(
                        ChannelInfoEvent.UpdateNameError(message = error.message),
                    )
                }
        }
    }

    public fun mute() {
        scope.launch {
            channelClient.mute().await()
                .onError { error ->
                    _events.tryEmit(
                        ChannelInfoEvent.MuteError(message = error.message),
                    )
                }
        }
    }

    public fun unmute() {
        scope.launch {
            channelClient.unmute().await()
                .onError { error ->
                    _events.tryEmit(
                        ChannelInfoEvent.UnmuteError(message = error.message),
                    )
                }
        }
    }

    public fun hide(clearHistory: Boolean) {
        scope.launch {
            channelClient.hide(clearHistory).await()
                .onError { error ->
                    _events.tryEmit(
                        ChannelInfoEvent.HideError(message = error.message),
                    )
                }
        }
    }

    public fun unhide() {
        scope.launch {
            channelClient.show().await()
                .onError { error ->
                    _events.tryEmit(
                        ChannelInfoEvent.UnhideError(message = error.message),
                    )
                }
        }
    }

    public fun leave(quitMessage: Message?) {
        scope.launch {
            runCatching {
                val currentUserId = requireNotNull(chatClient.getCurrentOrStoredUserId())
                channelClient.removeMembers(
                    memberIds = listOf(currentUserId),
                    systemMessage = quitMessage,
                ).await().onError { error ->
                    _events.tryEmit(
                        ChannelInfoEvent.LeaveError(message = error.message),
                    )
                }
            }.onFailure { cause ->
                _events.tryEmit(
                    ChannelInfoEvent.LeaveError(message = cause.message.orEmpty()),
                )
            }
        }
    }

    private fun List<Member>.filterNotCurrentUser() =
        filter { member -> member.user.id != chatClient.getCurrentOrStoredUserId() }
}

private const val MINIMUM_VISIBLE_MEMBERS = 5

private data class ChannelInfoData(
    val channel: ChannelData,
    val members: List<Member>,
    val isMuted: Boolean,
    val isHidden: Boolean,
)

private fun Member.toViewState(createdBy: User) = ChannelInfoViewState.Member(
    user = user,
    role = if (createdBy.id == user.id) {
        ChannelInfoViewState.Role.Owner
    } else {
        when (channelRole) {
            "channel_moderator" -> ChannelInfoViewState.Role.Moderator
            "channel_member" -> ChannelInfoViewState.Role.Member
            else -> ChannelInfoViewState.Role.Other(channelRole.orEmpty())
        }
    },
)

private fun MutableStateFlow<ChannelInfoViewState>.updateOnSuccessContent(
    transformation: (content: ChannelInfoViewState.Content.Success) -> ChannelInfoViewState.Content,
) {
    update { currentState ->
        if (currentState.content is ChannelInfoViewState.Content.Success) {
            currentState.copy(
                content = transformation(currentState.content),
            )
        } else {
            currentState
        }
    }
}
