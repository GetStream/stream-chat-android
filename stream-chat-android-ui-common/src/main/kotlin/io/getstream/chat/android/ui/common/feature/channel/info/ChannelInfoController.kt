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
import io.getstream.chat.android.models.Channel
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
import io.getstream.result.Error
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

/**
 * Controller responsible for managing the state and events related to channel information.
 *
 * It provides functionality to observe channel data, members, and perform
 * various channel-related actions such as renaming, muting, hiding, leaving, and deleting the channel.
 * It also handles state updates and emits events for UI consumption.
 *
 * @param cid The unique identifier of the channel.
 * @param scope The [CoroutineScope] used for launching coroutines.
 * @param chatClient The [ChatClient] instance used for interacting with the chat API.
 * @param channelState A [Flow] representing the state of the channel.
 * @param channelClient The [ChannelClient] instance for performing channel-specific operations.
 */
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

    /**
     * A [StateFlow] representing the current state of the channel info.
     */
    public val state: StateFlow<ChannelInfoState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ChannelInfoEvent>(extraBufferCapacity = 1)

    /**
     * A [SharedFlow] that emits one-time events related to channel info, such as errors or success events.
     */
    public val events: SharedFlow<ChannelInfoEvent> = _events.asSharedFlow()

    init {
        @Suppress("OPT_IN_USAGE")
        channelState
            .flatMapLatest { channel ->
                logger.d { "[onChannelState]" }
                combine(
                    channel.channelData.onEach { logger.d { "[onChannelData] name: ${it.name}" } },
                    channel.members.onEach { logger.d { "[onMembers] size: ${it.size}" } },
                    channel.muted.onEach { logger.d { "[onMuted] $it" } },
                    channel.hidden.onEach { logger.d { "[onHidden] $it" } },
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
                        capability = capability,
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
                        capability = capability,
                    )
                }
            }
        }
    }

    /**
     * Expands the members list.
     */
    public fun expandMembers() {
        logger.d { "[expandMembers]" }
        _state.updateContent { content ->
            content.copy(
                members = content.members.copy(
                    isCollapsed = false,
                ),
            )
        }
    }

    /**
     * Collapses the members list.
     */
    public fun collapseMembers() {
        logger.d { "[collapseMembers]" }
        _state.updateContent { content ->
            content.copy(
                members = content.members.copy(
                    isCollapsed = true,
                ),
            )
        }
    }

    /**
     * Renames the channel with the given name.
     *
     * @param name The new name for the channel.
     */
    public fun renameChannel(name: String) {
        logger.d { "[renameChannel] name: $name" }

        val onError: (Error) -> Unit = { error ->
            logger.e { "[renameChannel] error: ${error.message}" }
            _events.tryEmit(ChannelInfoEvent.RenameChannelError)
        }

        requireCapability(
            permission = { canRenameChannel },
            onError = onError,
        ) {
            scope.launch {
                channelClient.updatePartial(set = mapOf("name" to name)).await()
                    .onError(onError)
            }
        }
    }

    /**
     * Mutes the channel for the current user.
     */
    public fun muteChannel() {
        logger.d { "[muteChannel]" }

        val onError: (Error) -> Unit = { error ->
            logger.e { "[muteChannel] error: ${error.message}" }
            _events.tryEmit(ChannelInfoEvent.MuteChannelError)
        }

        requireCapability(
            permission = { canMuteChannel },
            onError = onError,
        ) {
            scope.launch {
                channelClient.mute().await()
                    .onError(onError)
            }
        }
    }

    /**
     * Unmutes the channel for the current user.
     */
    public fun unmuteChannel() {
        logger.d { "[unmuteChannel]" }

        val onError: (Error) -> Unit = { error ->
            logger.e { "[unmuteChannel] error: ${error.message}" }
            _events.tryEmit(ChannelInfoEvent.UnmuteChannelError)
        }

        requireCapability(
            permission = { canMuteChannel },
            onError = onError,
        ) {
            scope.launch {
                channelClient.unmute().await()
                    .onError(onError)
            }
        }
    }

    /**
     * Hides the channel.
     *
     * @param clearHistory If true, the channel history will be cleared.
     */
    public fun hideChannel(clearHistory: Boolean) {
        logger.d { "[hideChannel] clearHistory: $clearHistory" }
        scope.launch {
            channelClient.hide(clearHistory).await()
                .onError { error ->
                    logger.e { "[hideChannel] error: ${error.message}" }
                    _events.tryEmit(ChannelInfoEvent.HideChannelError)
                }
        }
    }

    /**
     * Unhides the channel.
     */
    public fun unhideChannel() {
        logger.d { "[unhideChannel]" }
        scope.launch {
            channelClient.show().await()
                .onError { error ->
                    logger.e { "[unhideChannel] error: ${error.message}" }
                    _events.tryEmit(ChannelInfoEvent.UnhideChannelError)
                }
        }
    }

    /**
     * Leaves the channel with an optional quit message.
     *
     * @param quitMessage The system message to send when leaving the channel.
     */
    public fun leaveChannel(quitMessage: Message?) {
        logger.d { "[leaveChannel] quitMessage: ${quitMessage?.text}" }

        val onError: (Error) -> Unit = { error ->
            logger.e { "[leaveChannel] error: ${error.message}" }
            _events.tryEmit(ChannelInfoEvent.LeaveChannelError)
        }

        requireCapability(
            permission = { canLeaveChannel },
            onError = onError,
        ) {
            scope.launch {
                runCatching {
                    requireNotNull(chatClient.getCurrentUser()?.id) { "User not connected" }
                }.onSuccess { currentUserId ->
                    removeMemberFromChannel(
                        memberId = currentUserId,
                        systemMessage = quitMessage,
                        onSuccess = { _events.tryEmit(ChannelInfoEvent.LeaveChannelSuccess) },
                        onError = onError,
                    )
                }.onFailure { cause ->
                    onError(Error.ThrowableError(message = cause.message.orEmpty(), cause = cause))
                }
            }
        }
    }

    private suspend fun removeMemberFromChannel(
        memberId: String,
        systemMessage: Message?,
        onSuccess: (Channel) -> Unit,
        onError: (Error) -> Unit,
    ) {
        channelClient.removeMembers(
            memberIds = listOf(memberId),
            systemMessage = systemMessage,
        ).await()
            .onSuccess(onSuccess)
            .onError(onError)
    }

    /**
     * Deletes the channel.
     */
    public fun deleteChannel() {
        logger.d { "[deleteChannel]" }

        val onError: (Error) -> Unit = { error ->
            logger.e { "[deleteChannel] error: ${error.message}" }
            _events.tryEmit(ChannelInfoEvent.DeleteChannelError)
        }

        requireCapability(
            permission = { canDeleteChannel },
            onError = onError,
        ) {
            scope.launch {
                channelClient.delete().await()
                    .onSuccess { _events.tryEmit(ChannelInfoEvent.DeleteChannelSuccess) }
                    .onError(onError)
            }
        }
    }

    private fun List<Member>.filterNotCurrentUser() =
        filter { member -> member.user.id != chatClient.getCurrentUser()?.id }

    private fun requireCapability(
        permission: ChannelInfoState.Content.Capability.() -> Boolean,
        onError: (error: Error) -> Unit,
        onSuccess: () -> Unit,
    ) {
        (_state.value as? ChannelInfoState.Content)?.capability?.let { capability ->
            runCatching { require(permission(capability)) }
                .onFailure { onError(Error.GenericError("User doesn't have permission")) }
                .onSuccess { onSuccess() }
        }
    }
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
    canAddMembers = ownCapabilities.contains(ChannelCapabilities.UPDATE_CHANNEL_MEMBERS),
    canRemoveMembers = ownCapabilities.contains(ChannelCapabilities.UPDATE_CHANNEL_MEMBERS),
    canBanMembers = ownCapabilities.contains(ChannelCapabilities.BAN_CHANNEL_MEMBERS),
    canRenameChannel = ownCapabilities.contains(ChannelCapabilities.UPDATE_CHANNEL),
    canMuteChannel = ownCapabilities.contains(ChannelCapabilities.MUTE_CHANNEL),
    canLeaveChannel = ownCapabilities.contains(ChannelCapabilities.LEAVE_CHANNEL),
    canDeleteChannel = ownCapabilities.contains(ChannelCapabilities.DELETE_CHANNEL),
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
