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
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.extensions.watchChannelAsState
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewEvent.Navigation
import io.getstream.chat.android.ui.common.helper.CopyToClipboardHandler
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoViewState
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
public class ChannelInfoViewController(
    cid: String,
    private val scope: CoroutineScope,
    private val copyToClipboardHandler: CopyToClipboardHandler,
    private val chatClient: ChatClient = ChatClient.instance(),
    channelState: Flow<ChannelState> = chatClient
        .watchChannelAsState(cid = cid, messageLimit = 0, coroutineScope = scope)
        .filterNotNull(),
    private val channelClient: ChannelClient = chatClient.channel(cid),
) {
    private val logger by taggedLogger("Chat:ChannelInfoViewController")

    private val _state = MutableStateFlow<ChannelInfoViewState>(ChannelInfoViewState.Loading)

    /**
     * A [StateFlow] representing the current state of the channel info.
     */
    public val state: StateFlow<ChannelInfoViewState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ChannelInfoViewEvent>(extraBufferCapacity = 1)

    /**
     * A [SharedFlow] that emits one-time events related to channel info, such as errors or success events.
     */
    public val events: SharedFlow<ChannelInfoViewEvent> = _events.asSharedFlow()

    private var capabilities = emptySet<String>()

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
        capabilities = channelData.ownCapabilities

        val contentMembers = members
            .run {
                // Do not filter out the current user if the channel is a group channel or if there is only one member
                takeIf { members.size == 1 || channelData.isGroupChannel } ?: filterNotCurrentUser()
            }

        _state.update { currentState ->
            val expandableMembers = when (currentState) {
                is ChannelInfoViewState.Loading -> {
                    ExpandableList(
                        items = contentMembers,
                        minimumVisibleItems = MINIMUM_VISIBLE_MEMBERS,
                    )
                }

                is ChannelInfoViewState.Content -> {
                    currentState.members.copy(items = contentMembers)
                }
            }
            ChannelInfoViewState.Content(
                owner = channelData.createdBy,
                members = expandableMembers,
                options = buildChannelOptionList(
                    channelData = channelData,
                    singleMember = if (contentMembers.size == 1) contentMembers.first() else null,
                    isMuted = isMuted,
                    isHidden = isHidden,
                ),
            )
        }
    }

    /**
     * Handles actions related to channel information view.
     *
     * @param action The [ChannelInfoViewAction] representing the action to be performed.
     */
    public fun onViewAction(
        action: ChannelInfoViewAction,
    ) {
        logger.d { "[onViewAction] action: $action" }
        when (action) {
            is ChannelInfoViewAction.ExpandMembersClick -> expandMembers()
            is ChannelInfoViewAction.CollapseMembersClick -> collapseMembers()
            is ChannelInfoViewAction.MemberClick -> memberClick(action)
            is ChannelInfoViewAction.UserInfoClick -> userInfoClick(action.user)
            is ChannelInfoViewAction.RenameChannelClick -> renameChannel(action.name)
            is ChannelInfoViewAction.PinnedMessagesClick ->
                _events.tryEmit(ChannelInfoViewEvent.NavigateToPinnedMessages)

            is ChannelInfoViewAction.MuteChannelClick -> setChannelMute(mute = true)
            is ChannelInfoViewAction.UnmuteChannelClick -> setChannelMute(mute = false)
            is ChannelInfoViewAction.HideChannelClick -> _events.tryEmit(ChannelInfoViewEvent.HideChannelModal)
            is ChannelInfoViewAction.HideChannelConfirmationClick -> setChannelHide(hide = true, action.clearHistory)
            is ChannelInfoViewAction.UnhideChannelClick -> setChannelHide(hide = false)
            is ChannelInfoViewAction.LeaveChannelClick -> _events.tryEmit(ChannelInfoViewEvent.LeaveChannelModal)
            is ChannelInfoViewAction.LeaveChannelConfirmationClick -> leaveChannel(action.quitMessage)
            is ChannelInfoViewAction.DeleteChannelClick -> _events.tryEmit(ChannelInfoViewEvent.DeleteChannelModal)
            is ChannelInfoViewAction.DeleteChannelConfirmationClick -> deleteChannel()
            is ChannelInfoViewAction.MemberMessageClick ->
                _events.tryEmit(ChannelInfoViewEvent.NavigateToChannel(channelId = "")) // TODO NavigateToChannel

            is ChannelInfoViewAction.RemoveMemberClick -> removeMember(action.member)
            is ChannelInfoViewAction.BanMemberClick -> banMember(action.member)
            is ChannelInfoViewAction.UnbanMemberClick -> unbanMember(action.member)
        }
    }

    private fun expandMembers() {
        logger.d { "[expandMembers]" }
        _state.updateContent { content ->
            content.copy(
                members = content.members.copy(
                    isCollapsed = false,
                ),
            )
        }
    }

    private fun collapseMembers() {
        logger.d { "[collapseMembers]" }
        _state.updateContent { content ->
            content.copy(
                members = content.members.copy(
                    isCollapsed = true,
                ),
            )
        }
    }

    private fun memberClick(action: ChannelInfoViewAction.MemberClick) {
        logger.d { "[memberClick] member: ${action.member}" }

        _events.tryEmit(
            ChannelInfoViewEvent.MemberInfoModal(
                member = action.member,
                options = buildMemberOptionList(
                    member = action.member,
                    capabilities = capabilities,
                ),
            ),
        )
    }

    private fun userInfoClick(user: User) {
        logger.d { "[userInfoClick] user: $user" }

        copyToClipboardHandler.copy(text = "@${user.name}")
    }

    private fun renameChannel(name: String) {
        logger.d { "[renameChannel] name: $name" }

        scope.launch {
            channelClient.updatePartial(set = mapOf("name" to name)).await()
                .onError { error ->
                    logger.e { "[renameChannel] error: ${error.message}" }
                    _events.tryEmit(ChannelInfoViewEvent.RenameChannelError)
                }
        }
    }

    private fun setChannelMute(mute: Boolean) {
        logger.d { "[setChannelMute] mute: $mute" }

        scope.launch {
            if (mute) {
                channelClient.mute().await()
            } else {
                channelClient.unmute().await()
            }.onError { error ->
                logger.e { "[setChannelMute] error: ${error.message}" }
                _events.tryEmit(
                    if (mute) {
                        ChannelInfoViewEvent.MuteChannelError
                    } else {
                        ChannelInfoViewEvent.UnmuteChannelError
                    },
                )
            }
        }
    }

    private fun setChannelHide(hide: Boolean, clearHistory: Boolean = false) {
        logger.d { "[setChannelHide] hide: $hide, clearHistory: $clearHistory" }

        val onError: (Error) -> Unit = { error ->
            logger.e { "[setChannelHide] error: ${error.message}" }
            _events.tryEmit(
                if (hide) {
                    ChannelInfoViewEvent.HideChannelError
                } else {
                    ChannelInfoViewEvent.UnhideChannelError
                },
            )
        }

        scope.launch {
            if (hide) {
                channelClient.hide(clearHistory)
                    .await()
                    .onSuccess {
                        _events.tryEmit(ChannelInfoViewEvent.NavigateUp(reason = Navigation.Reason.HideChannelSuccess))
                    }
            } else {
                channelClient.show().await()
            }.onError(onError)
        }
    }

    private fun leaveChannel(quitMessage: Message?) {
        logger.d { "[leaveChannel] quitMessage: ${quitMessage?.text}" }

        val onError: (Error) -> Unit = { error ->
            logger.e { "[leaveChannel] error: ${error.message}" }
            _events.tryEmit(ChannelInfoViewEvent.LeaveChannelError)
        }

        runCatching {
            requireNotNull(chatClient.getCurrentUser()?.id) { "User not connected" }
        }.onSuccess { currentUserId ->
            removeMemberFromChannel(
                memberId = currentUserId,
                systemMessage = quitMessage,
                onSuccess = {
                    _events.tryEmit(ChannelInfoViewEvent.NavigateUp(reason = Navigation.Reason.LeaveChannelSuccess))
                },
                onError = onError,
            )
        }.onFailure { cause ->
            onError(Error.ThrowableError(message = cause.message.orEmpty(), cause = cause))
        }
    }

    private fun removeMemberFromChannel(
        memberId: String,
        systemMessage: Message?,
        onSuccess: (Channel) -> Unit,
        onError: (Error) -> Unit,
    ) {
        scope.launch {
            channelClient.removeMembers(
                memberIds = listOf(memberId),
                systemMessage = systemMessage,
            ).await()
                .onSuccess(onSuccess)
                .onError(onError)
        }
    }

    private fun deleteChannel() {
        logger.d { "[deleteChannel]" }

        scope.launch {
            channelClient.delete().await()
                .onSuccess {
                    _events.tryEmit(ChannelInfoViewEvent.NavigateUp(reason = Navigation.Reason.DeleteChannelSuccess))
                }
                .onError { error ->
                    logger.e { "[deleteChannel] error: ${error.message}" }
                    _events.tryEmit(ChannelInfoViewEvent.DeleteChannelError)
                }
        }
    }

    private fun removeMember(member: Member) {
        logger.d { "[removeMember] member: $member" }

        removeMemberFromChannel(
            memberId = member.getUserId(),
            systemMessage = null,
            onSuccess = { /* no-op */ },
            onError = { error ->
                logger.e { "[removeMember] error: ${error.message}" }
                _events.tryEmit(ChannelInfoViewEvent.RemoveMemberError)
            },
        )
    }

    private fun banMember(member: Member) {
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
                    _events.tryEmit(ChannelInfoViewEvent.BanMemberError)
                }
        }
    }

    private fun unbanMember(member: Member) {
        logger.d { "[unbanMember] member: $member" }

        scope.launch {
            channelClient.unbanUser(member.getUserId()).await()
                .onSuccess { /* no-op */ }
                .onError { error ->
                    logger.e { "[unbanMember] error: ${error.message}" }
                    _events.tryEmit(ChannelInfoViewEvent.UnbanMemberError)
                }
        }
    }

    private fun List<Member>.filterNotCurrentUser() =
        filter { member -> member.user.id != chatClient.getCurrentUser()?.id }
}

private const val MINIMUM_VISIBLE_MEMBERS = 5

private data class ChannelInfoData(
    val channelData: ChannelData,
    val members: List<Member>,
    val isMuted: Boolean,
    val isHidden: Boolean,
)

/**
 * Group channels are channels with more than 2 members or channels that are not distinct.
 */
private val ChannelData.isGroupChannel: Boolean
    get() = memberCount > 2 || !isDistinct

/**
 * A distinct channel is a channel created for a particular set of users, usually for one-to-one conversations.
 */
private val ChannelData.isDistinct: Boolean
    get() = id.startsWith("!members")

private fun buildChannelOptionList(
    channelData: ChannelData,
    singleMember: Member?,
    isMuted: Boolean,
    isHidden: Boolean,
) = buildList {
    if (channelData.ownCapabilities.contains(ChannelCapabilities.UPDATE_CHANNEL_MEMBERS)) {
        add(ChannelInfoViewState.Content.Option.AddMember)
    }
    if (singleMember != null) {
        add(ChannelInfoViewState.Content.Option.UserInfo(user = singleMember.user))
    } else {
        add(
            ChannelInfoViewState.Content.Option.RenameChannel(
                name = channelData.name,
                isReadOnly = !channelData.ownCapabilities.contains(ChannelCapabilities.UPDATE_CHANNEL),
            ),
        )
    }
    if (channelData.ownCapabilities.contains(ChannelCapabilities.MUTE_CHANNEL)) {
        add(ChannelInfoViewState.Content.Option.MuteChannel(isMuted))
    }
    add(ChannelInfoViewState.Content.Option.HideChannel(isHidden))
    add(ChannelInfoViewState.Content.Option.PinnedMessages)
    add(ChannelInfoViewState.Content.Option.Separator)
    if (channelData.ownCapabilities.contains(ChannelCapabilities.LEAVE_CHANNEL)) {
        add(ChannelInfoViewState.Content.Option.LeaveChannel)
    }
    if (channelData.ownCapabilities.contains(ChannelCapabilities.DELETE_CHANNEL)) {
        add(ChannelInfoViewState.Content.Option.DeleteChannel)
    }
}

private fun buildMemberOptionList(member: Member, capabilities: Set<String>) = buildList {
    add(ChannelInfoViewState.Content.Option.MessageMember(member = member))
    if (capabilities.contains(ChannelCapabilities.BAN_CHANNEL_MEMBERS)) {
        if (member.banned) {
            add(ChannelInfoViewState.Content.Option.UnbanMember(member = member))
        } else {
            add(ChannelInfoViewState.Content.Option.BanMember(member = member))
        }
    }
    if (capabilities.contains(ChannelCapabilities.UPDATE_CHANNEL_MEMBERS)) {
        add(ChannelInfoViewState.Content.Option.RemoveMember(member = member))
    }
}

private fun MutableStateFlow<ChannelInfoViewState>.updateContent(
    transformation: (content: ChannelInfoViewState.Content) -> ChannelInfoViewState.Content,
) {
    update { currentState ->
        if (currentState is ChannelInfoViewState.Content) {
            transformation(currentState)
        } else {
            currentState
        }
    }
}
