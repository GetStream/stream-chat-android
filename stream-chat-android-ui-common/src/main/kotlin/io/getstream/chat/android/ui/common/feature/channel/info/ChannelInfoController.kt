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

@file:OptIn(ExperimentalStreamChatApi::class)

package io.getstream.chat.android.ui.common.feature.channel.info

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.extensions.isGroupChannel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.extensions.watchChannelAsState
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoEvent
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoState
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

    private val _state = MutableStateFlow<ChannelInfoState>(ChannelInfoState.Loading)

    public val state: StateFlow<ChannelInfoState> = _state.asStateFlow()

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
            .onEach { (channelData, members, isMuted, isHidden) ->
                onChannelInfoData(channelData, members, isMuted, isHidden)
            }
            .launchIn(scope)
    }

    private fun onChannelInfoData(
        channelData: ChannelData,
        members: List<Member>,
        isMuted: Boolean,
        isHidden: Boolean,
    ) {
        val capability = channelData.toCapability()

        logger.d {
            "[onChannelInfoData] cid: ${channelData.cid}, " +
                "name: ${channelData.name}, " +
                "members: ${members.size}, " +
                "isMuted: $isMuted, " +
                "isHidden: $isHidden, " +
                "$capability"
        }

        val contentMembers = members
            .run { takeIf { channelData.isGroupChannel } ?: filterNotCurrentUser() }
            .map { member -> member.toContentMember(channelData.createdBy) }

        _state.update { currentState ->
            when (currentState) {
                is ChannelInfoState.Loading -> {
                    ChannelInfoState.Content(
                        members = ExpandableList(
                            items = contentMembers,
                            minimumVisibleItems = MINIMUM_VISIBLE_MEMBERS,
                        ),
                        name = channelData.name,
                        isMuted = isMuted,
                        isHidden = isHidden,
                        capability = capability
                    )
                }

                is ChannelInfoState.Content -> {
                    currentState.copy(
                        members = currentState.members.copy(
                            items = contentMembers,
                        ),
                        name = channelData.name,
                        isMuted = isMuted,
                        isHidden = isHidden,
                        capability = capability
                    )
                }
            }
        }
    }

    public fun expandMembers() {
        _state.updateContent { content ->
            content.copy(
                members = content.members.copy(
                    isCollapsed = false,
                ),
            )
        }
    }

    public fun collapseMembers() {
        _state.updateContent { content ->
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
                ).await()
                    .onSuccess {
                        _events.tryEmit(
                            ChannelInfoEvent.LeaveSuccess,
                        )
                    }
                    .onError { error ->
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

    public fun delete() {
        scope.launch {
            channelClient.delete().await()
                .onSuccess {
                    _events.tryEmit(
                        ChannelInfoEvent.DeleteSuccess,
                    )
                }
                .onError { error ->
                    _events.tryEmit(
                        ChannelInfoEvent.DeleteError(message = error.message),
                    )
                }
        }
    }

    private fun List<Member>.filterNotCurrentUser() =
        filter { member -> member.user.id != chatClient.getCurrentOrStoredUserId() }
}

private const val MINIMUM_VISIBLE_MEMBERS = 5

private data class ChannelInfoData(
    val channelData: ChannelData,
    val members: List<Member>,
    val isMuted: Boolean,
    val isHidden: Boolean,
)

private fun Member.toContentMember(createdBy: User) = ChannelInfoState.Content.Member(
    user = user,
    role = if (createdBy.id == user.id) {
        ChannelInfoState.Content.Role.Owner
    } else {
        when (channelRole) {
            "channel_moderator" -> ChannelInfoState.Content.Role.Moderator
            "channel_member" -> ChannelInfoState.Content.Role.Member
            else -> ChannelInfoState.Content.Role.Other(channelRole.orEmpty())
        }
    },
)

private fun ChannelData.toCapability() = ChannelInfoState.Content.Capability(
    canAddMember = ownCapabilities.contains(ChannelCapabilities.UPDATE_CHANNEL_MEMBERS),
    canRename = ownCapabilities.contains(ChannelCapabilities.UPDATE_CHANNEL),
    canMute = ownCapabilities.contains(ChannelCapabilities.MUTE_CHANNEL),
    canLeave = ownCapabilities.contains(ChannelCapabilities.LEAVE_CHANNEL),
    canDelete = ownCapabilities.contains(ChannelCapabilities.DELETE_CHANNEL),
)

private fun MutableStateFlow<ChannelInfoState>.updateContent(
    transformation: (content: ChannelInfoState.Content) -> ChannelInfoState.Content,
) {
    update { currentState ->
        if (currentState is ChannelInfoState.Content) {
            transformation(currentState)
        } else {
            currentState
        }
    }
}
