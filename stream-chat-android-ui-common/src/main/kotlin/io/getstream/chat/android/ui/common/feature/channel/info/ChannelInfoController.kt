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
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.extensions.watchChannelAsState
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoEvent
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoViewState
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
                println("alor: flatMapLatest")
                combine(
                    channelState.channelData.onEach { println("alor: channelData") },
                    channelState.members.onEach { println("alor: members") },
                    channelState.muted.onEach { println("alor: muted") },
                ) { channel, members, muted ->
                    println("alor: combine")
                    Triple(channel, members, muted)
                }
            }
            .distinctUntilChanged()
            .onEach { (channel, members, muted) ->
                onChannelState(channel, members, muted)
            }
            .launchIn(scope)
    }

    private fun onChannelState(channel: ChannelData, members: List<Member>, muted: Boolean) {
        val channelMembers = members
            .filterNotCurrentUser()
            .map { member -> member.toViewState(channel.createdBy) }

        val expandedMembers = channelMembers.take(EXPANDED_MEMBER_COUNT)
        val collapsedMemberCount = (channelMembers.size - EXPANDED_MEMBER_COUNT).coerceAtLeast(0)
        val collapsedMembers = channelMembers.takeLast(collapsedMemberCount)

        logger.d {
            "[onChannelState] name: ${channel.name}, " +
                "members: ${channelMembers.size}, " +
                "expanded: ${expandedMembers.size}, " +
                "collapsed: ${collapsedMembers.size}, " +
                "muted: $muted"
        }

        println(
            "alor name: ${channel.name}, " +
                "members: ${channelMembers.size}, " +
                "expanded: ${expandedMembers.size}, " +
                "collapsed: ${collapsedMembers.size}, " +
                "muted: $muted"
        )

        _state.update { currentState ->
            when (currentState.content) {
                is ChannelInfoViewState.Content.Loading -> {
                    currentState.copy(
                        content = ChannelInfoViewState.Content.Success(
                            expandedMembers = expandedMembers,
                            collapsedMembers = collapsedMembers,
                            areMembersExpandable = collapsedMemberCount > 0,
                            areMembersExpanded = false,
                            name = channel.name,
                            isMuted = muted,
                        ),
                    )
                }

                is ChannelInfoViewState.Content.Success -> {
                    currentState.copy(
                        content = currentState.content.copy(
                            expandedMembers = expandedMembers,
                            collapsedMembers = collapsedMembers,
                            areMembersExpandable = collapsedMemberCount > 0,
                            name = channel.name,
                            isMuted = muted,
                        ),
                    )
                }

                else -> currentState
            }
        }
    }

    public fun expandMembers() {
        _state.updateOnSuccessContent { content ->
            content.copy(areMembersExpanded = true)
        }
    }

    public fun collapseMembers() {
        _state.updateOnSuccessContent { content ->
            content.copy(areMembersExpanded = false)
        }
    }

    public fun updateName(name: String) {
        scope.launch {
            channelClient
                .updatePartial(set = mapOf("name" to name))
                .await()
                .onError { error ->
                    _events.tryEmit(
                        ChannelInfoEvent.UpdateNameError(message = error.message),
                    )
                }
        }
    }

    private fun List<Member>.filterNotCurrentUser() =
        filter { member -> member.user.id != chatClient.getCurrentOrStoredUserId() }
}

private const val EXPANDED_MEMBER_COUNT = 5

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
                content = transformation(currentState.content)
            )
        } else {
            currentState
        }
    }
}
