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
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.state.extensions.watchChannelAsState
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoMemberViewState
import io.getstream.log.taggedLogger
import io.getstream.result.onErrorSuspend
import io.getstream.result.onSuccessSuspend
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

/**
 * Controller responsible for managing the state and events related to channel member information.
 *
 * @param cid The full channel identifier (e.g., "messaging:123").
 * @param memberId The member ID of the user whose information is being managed.
 * @param scope The [CoroutineScope] used for launching coroutines.
 * @param chatClient The [ChatClient] instance used for interacting with the chat API.
 * @param channelState A [Flow] representing the state of the channel.
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

    private lateinit var member: Member
    private var distinctChannelId: String? = null

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
                    queryDistinctChannel(),
                    ::ChannelInfoMemberData,
                )
            }
            .distinctUntilChanged()
            .onEach { (channelData, member, distinctChannelId) ->
                onChannelInfoData(channelData, member, distinctChannelId)
            }
            .launchIn(scope)
    }

    private fun queryDistinctChannel(): Flow<String?> =
        flow {
            val currentUserId = requireNotNull(chatClient.getCurrentUser()?.id)
            logger.d { "[queryDistinctChannel] currentUserId: $currentUserId, memberId: $memberId" }
            chatClient.queryChannels(
                request = QueryChannelsRequest(
                    filter = Filters.and(
                        Filters.eq("type", "messaging"),
                        Filters.distinct(listOf(memberId, currentUserId)),
                    ),
                    querySort = QuerySortByField.descByName("last_updated"),
                    messageLimit = 0,
                    limit = 1,
                ),
            ).await()
                .onSuccessSuspend { channels ->
                    if (channels.isEmpty()) {
                        logger.w { "[queryDistinctChannel] No distinct channel found of member: $memberId" }
                        emit(null)
                    } else {
                        val channel = channels.first()
                        logger.d { "[queryDistinctChannel] Found distinct channel: ${channel.cid}" }
                        emit(channel.cid)
                    }
                }
                .onErrorSuspend {
                    logger.e { "[queryDistinctChannel] Error querying distinct channel of member: $memberId" }
                    emit(null)
                }
        }

    private fun onChannelInfoData(
        channelData: ChannelData,
        member: Member,
        distinctChannelId: String?,
    ) {
        this.member = member
        this.distinctChannelId = distinctChannelId

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
            is ChannelInfoMemberViewAction.MessageMemberClick ->
                _events.tryEmit(ChannelInfoMemberViewEvent.MessageMember(memberId, distinctChannelId))

            is ChannelInfoMemberViewAction.BanMemberClick ->
                _events.tryEmit(ChannelInfoMemberViewEvent.BanMember(member))

            is ChannelInfoMemberViewAction.UnbanMemberClick ->
                _events.tryEmit(ChannelInfoMemberViewEvent.UnbanMember(member))

            is ChannelInfoMemberViewAction.RemoveMemberClick ->
                _events.tryEmit(ChannelInfoMemberViewEvent.RemoveMember(member))
        }
    }
}

private data class ChannelInfoMemberData(
    val channelData: ChannelData,
    val member: Member,
    val distinctChannelId: String?,
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
