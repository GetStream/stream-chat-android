/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Channel
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

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

    /**
     * A [StateFlow] representing the current state of the channel info.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    public val state: StateFlow<ChannelInfoMemberViewState> =
        channelState.flatMapLatest { channel ->
            combine(
                channel.channelData,
                channel.members
                    .mapNotNull { members -> members.firstOrNull { it.getUserId() == memberId } }
                    .onEach { logger.d { "[onMember] name: ${it.user.name}" } },
                queryDistinctChannel(),
                ::ChannelInfoMemberData,
            )
        }.map { (channelData, member, distinctChannel) ->
            this.member = member
            this.distinctCid = distinctChannel?.cid
            ChannelInfoMemberViewState.Content(
                member = member,
                options = buildOptionList(
                    member = member,
                    capabilities = channelData.ownCapabilities,
                ),
            )
        }.stateIn(
            scope = scope,
            started = WhileSubscribed(STOP_TIMEOUT_IN_MILLIS),
            initialValue = ChannelInfoMemberViewState.Loading,
        )

    private val _events = MutableSharedFlow<ChannelInfoMemberViewEvent>(extraBufferCapacity = 1)

    /**
     * A [SharedFlow] that emits one-shot events related to channel info, such as errors or success events.
     */
    public val events: SharedFlow<ChannelInfoMemberViewEvent> = _events.asSharedFlow()

    private lateinit var member: Member
    private var distinctCid: String? = null

    private fun queryDistinctChannel(): Flow<Channel?> =
        flow {
            chatClient.getCurrentUser()?.id?.let { currentUserId ->
                logger.d { "[queryDistinctChannel] currentUserId: $currentUserId, memberId: $memberId" }
                chatClient.queryChannels(
                    request = QueryChannelsRequest(
                        filter = Filters.distinct(listOf(memberId, currentUserId)),
                        querySort = QuerySortByField.descByName("last_updated"),
                        messageLimit = 0,
                        memberLimit = 1,
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
                            emit(channel)
                        }
                    }
                    .onErrorSuspend {
                        logger.e { "[queryDistinctChannel] Error querying distinct channel of member: $memberId" }
                        emit(null)
                    }
            }
        }

    /**
     * Handles actions related to channel member information view.
     *
     * @param action The [ChannelInfoMemberViewAction] representing the action to be handled.
     */
    public fun onViewAction(
        action: ChannelInfoMemberViewAction,
    ) {
        logger.d { "[onViewAction] action: $action" }
        when (action) {
            is ChannelInfoMemberViewAction.MessageMemberClick ->
                _events.tryEmit(ChannelInfoMemberViewEvent.MessageMember(memberId, distinctCid))

            is ChannelInfoMemberViewAction.BanMemberClick ->
                _events.tryEmit(ChannelInfoMemberViewEvent.BanMember(member))

            is ChannelInfoMemberViewAction.UnbanMemberClick ->
                _events.tryEmit(ChannelInfoMemberViewEvent.UnbanMember(member))

            is ChannelInfoMemberViewAction.RemoveMemberClick ->
                _events.tryEmit(ChannelInfoMemberViewEvent.RemoveMember(member))
        }
    }
}

private const val STOP_TIMEOUT_IN_MILLIS = 5_000L

private data class ChannelInfoMemberData(
    val channelData: ChannelData,
    val member: Member,
    val distinctChannel: Channel?,
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
