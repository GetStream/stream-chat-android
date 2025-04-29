package io.getstream.chat.android.ui.common.feature.channel.info

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.state.extensions.watchChannelAsState
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoState
import io.getstream.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

@InternalStreamChatApi
public class ChannelInfoController(
    scope: CoroutineScope,
    cid: String,
    chatClient: ChatClient = ChatClient.instance(),
) {
    private val logger by taggedLogger("Chat:ChannelInfoController")

    private val _state = MutableStateFlow(ChannelInfoState())

    public val state: StateFlow<ChannelInfoState> = _state.asStateFlow()

    init {
        chatClient.watchChannelAsState(cid = cid, messageLimit = 0, coroutineScope = scope)
            .filterNotNull()
            .flatMapLatest { channelState ->
                combine(channelState.channelData, channelState.members) { channel, members ->
                    channel to members
                }
            }
            .onEach { (channel, members) ->
                val channelMembers = members
                    .filter { member ->
                        member.user.id != chatClient.getCurrentOrStoredUserId()
                    }
                    .map { member ->
                        ChannelInfoState.Member(
                            user = member.user,
                            role = if (channel.createdBy.id == member.user.id) {
                                ChannelInfoState.Role.Owner
                            } else {
                                when (member.channelRole) {
                                    "channel_moderator" -> ChannelInfoState.Role.Moderator
                                    "channel_member" -> ChannelInfoState.Role.Member
                                    else -> ChannelInfoState.Role.Other(member.channelRole.orEmpty())
                                }
                            }
                        )
                    }
                val expandedMembers = channelMembers.take(EXPANDED_MEMBER_COUNT)
                val collapsedMemberCount = (channelMembers.size - EXPANDED_MEMBER_COUNT).coerceAtLeast(0)
                val collapsedMembers = channelMembers.takeLast(collapsedMemberCount)

                logger.d {
                    "[onSuccessContent] members: ${channelMembers.size}, " +
                        "expanded: ${expandedMembers.size}, " +
                        "collapsed: ${collapsedMembers.size}"
                }

                _state.update { currentState ->
                    when (currentState.content) {
                        is ChannelInfoState.Content.Loading -> {
                            currentState.copy(
                                content = ChannelInfoState.Content.Success(
                                    expandedMembers = expandedMembers,
                                    collapsedMembers = collapsedMembers,
                                    areMembersExpandable = collapsedMemberCount > 0,
                                    areMembersExpanded = false,
                                )
                            )
                        }

                        is ChannelInfoState.Content.Success -> {
                            currentState.copy(
                                content = currentState.content.copy(
                                    expandedMembers = expandedMembers,
                                    collapsedMembers = collapsedMembers,
                                    areMembersExpandable = collapsedMemberCount > 0,
                                )
                            )
                        }

                        else -> currentState
                    }
                }
            }
            .launchIn(scope)
    }

    public fun expandMembers() {
        _state.update { currentState ->
            (currentState.content as? ChannelInfoState.Content.Success)?.let { content ->
                currentState.copy(
                    content = content.copy(areMembersExpanded = true)
                )
            } ?: currentState
        }
    }

    public fun collapseMembers() {
        _state.update { currentState ->
            (currentState.content as? ChannelInfoState.Content.Success)?.let { content ->
                currentState.copy(
                    content = content.copy(areMembersExpanded = false)
                )
            } ?: currentState
        }
    }
}

private const val EXPANDED_MEMBER_COUNT = 5
