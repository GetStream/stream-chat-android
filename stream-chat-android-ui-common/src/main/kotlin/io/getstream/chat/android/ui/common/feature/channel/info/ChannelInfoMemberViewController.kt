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
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.extensions.watchChannelAsState
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoMemberViewState
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
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Controller responsible for managing the state and events related to channel member information.
 *
 * It provides functionality to observe channel data, members, and perform
 * various channel-related actions such as renaming, muting, hiding, leaving, and deleting the channel.
 * It also handles state updates and emits events for UI consumption.
 *
 * @param cid The full channel identifier (e.g., "messaging:123").
 * @param memberId The member ID of the user whose information is being managed.
 * @param scope The [CoroutineScope] used for launching coroutines.
 * @param chatClient The [ChatClient] instance used for interacting with the chat API.
 * @param channelState A [Flow] representing the state of the channel.
 * @param channelClient The [ChannelClient] instance for performing channel-specific operations.
 */
@InternalStreamChatApi
public class ChannelInfoMemberViewController(
    cid: String,
    private val memberId: String,
    private val scope: CoroutineScope,
    private val chatClient: ChatClient = ChatClient.instance(),
    channelState: Flow<ChannelState> = chatClient
        .watchChannelAsState(cid = cid, messageLimit = 0, coroutineScope = scope)
        .filterNotNull(),
    private val channelClient: ChannelClient = chatClient.channel(cid),
) {
    private val logger by taggedLogger("Chat:ChannelInfoMemberViewController")

    private val _state = MutableStateFlow<ChannelInfoMemberViewState>(ChannelInfoMemberViewState.Loading)

    /**
     * A [StateFlow] representing the current state of the channel info.
     */
    public val state: StateFlow<ChannelInfoMemberViewState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ChannelInfoMemberViewEvent>(extraBufferCapacity = 1)

    /**
     * A [SharedFlow] that emits one-time events related to channel info, such as errors or success events.
     */
    public val events: SharedFlow<ChannelInfoMemberViewEvent> = _events.asSharedFlow()

    private var member = Member(user = User())

    init {
        @Suppress("OPT_IN_USAGE")
        channelState
            .flatMapLatest { channel ->
                logger.d { "[onChannelState]" }
                combine(
                    channel.channelData.onEach {
                        logger.d {
                            "[onChannelData] cid: ${it.cid}, name: ${it.name}, capabilities: ${it.ownCapabilities}"
                        }
                    },
                    channel.members
                        .mapNotNull { members -> members.firstOrNull { it.getUserId() == memberId } }
                        .onEach { logger.d { "[onMember] name: ${it.user.name}" } },
                    ::ChannelInfoMemberData,
                )
            }
            .distinctUntilChanged()
            .onEach { (channelData, member) ->
                onChannelInfoData(channelData, member)
            }
            .launchIn(scope)
    }

    private fun onChannelInfoData(
        channelData: ChannelData,
        member: Member,
    ) {
        this.member = member

        _state.update {
            ChannelInfoMemberViewState.Content(
                member = member,
                options = buildOptionList(
                    member = member,
                    capabilities = channelData.ownCapabilities,
                ),
            )
        }
    }

    /**
     * Handles actions related to channel member information view.
     *
     * @param action The [ChannelInfoMemberViewAction] representing the action to be performed.
     */
    public fun onViewAction(
        action: ChannelInfoMemberViewAction,
    ) {
        logger.d { "[onViewAction] action: $action" }
        when (action) {
            is ChannelInfoMemberViewAction.MemberMessageClick ->
                _events.tryEmit(ChannelInfoMemberViewEvent.NavigateToChannel(channelId = "")) // TODO NavigateToChannel

            is ChannelInfoMemberViewAction.BanMemberClick -> banMember()
            is ChannelInfoMemberViewAction.UnbanMemberClick -> unbanMember()

            is ChannelInfoMemberViewAction.RemoveMemberClick ->
                _events.tryEmit(ChannelInfoMemberViewEvent.RemoveMemberModal(member))

            is ChannelInfoMemberViewAction.RemoveMemberConfirmationClick -> removeMember(memberId)
        }
    }

    private fun banMember() {
        logger.d { "[banMember] member: $member" }

        scope.launch {
            channelClient.banUser(
                targetId = member.getUserId(),
                reason = null,
                timeout = null, // TODO add timeout to ban
            ).await()
                .onSuccess { /* no-op */ }
                .onError { error ->
                    logger.e { "[banMember] error: ${error.message}" }
                    _events.tryEmit(ChannelInfoMemberViewEvent.BanMemberError)
                }
        }
    }

    private fun unbanMember() {
        logger.d { "[unbanMember] member: $member" }

        scope.launch {
            channelClient.unbanUser(member.getUserId()).await()
                .onSuccess { /* no-op */ }
                .onError { error ->
                    logger.e { "[unbanMember] error: ${error.message}" }
                    _events.tryEmit(ChannelInfoMemberViewEvent.UnbanMemberError)
                }
        }
    }

    private fun removeMember(memberId: String) {
        logger.d { "[removeMember] memberId: $memberId" }

        scope.launch {
            channelClient.removeMembers(
                memberIds = listOf(element = memberId),
                systemMessage = null,
            ).await()
                .onSuccess { /* no-op */ }
                .onError { error: Error ->
                    logger.e { "[removeMember] error: ${error.message}" }
                    _events.tryEmit(ChannelInfoMemberViewEvent.RemoveMemberError)
                }
        }
    }
}

private data class ChannelInfoMemberData(
    val channelData: ChannelData,
    val member: Member,
)

private fun buildOptionList(member: Member, capabilities: Set<String>) = buildList {
    add(ChannelInfoMemberViewState.Content.Option.MessageMember(member = member))
    if (capabilities.contains(ChannelCapabilities.BAN_CHANNEL_MEMBERS)) {
        if (member.banned) {
            add(ChannelInfoMemberViewState.Content.Option.UnbanMember(member = member))
        } else {
            add(ChannelInfoMemberViewState.Content.Option.BanMember(member = member))
        }
    }
    if (capabilities.contains(ChannelCapabilities.UPDATE_CHANNEL_MEMBERS)) {
        add(ChannelInfoMemberViewState.Content.Option.RemoveMember(member = member))
    }
}
