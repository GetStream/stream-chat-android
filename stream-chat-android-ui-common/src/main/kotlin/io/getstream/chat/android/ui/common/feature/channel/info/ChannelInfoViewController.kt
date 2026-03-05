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
import io.getstream.chat.android.client.api.state.GlobalState
import io.getstream.chat.android.client.api.state.globalStateFlow
import io.getstream.chat.android.client.api.state.watchChannelAsState
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.User
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
 * @param copyToClipboardHandler The [CopyToClipboardHandler] used for copying text to the clipboard.
 * @param optionFilter A filter function for channel options, allowing customization of which options are displayed.
 *                      Defaults to a function that returns true for all options.
 * @param chatClient The [ChatClient] instance used for interacting with the chat API.
 * @param channelState A [Flow] representing the state of the channel.
 * @param channelClient The [ChannelClient] instance for performing channel-specific operations.
 */
@Suppress("TooManyFunctions")
@InternalStreamChatApi
public class ChannelInfoViewController(
    private val cid: String,
    private val scope: CoroutineScope,
    private val copyToClipboardHandler: CopyToClipboardHandler,
    private val optionFilter: (option: ChannelInfoViewState.Content.Option) -> Boolean = { true },
    private val chatClient: ChatClient = ChatClient.instance(),
    channelState: Flow<ChannelState> = chatClient
        .watchChannelAsState(cid = cid, messageLimit = 0, coroutineScope = scope)
        .filterNotNull(),
    private val channelClient: ChannelClient = chatClient.channel(cid),
    globalState: Flow<GlobalState> = chatClient.globalStateFlow,
) {
    private val logger by taggedLogger("Chat:ChannelInfoViewController")

    private val _state = MutableStateFlow<ChannelInfoViewState>(ChannelInfoViewState.Loading)

    /**
     * A [StateFlow] representing the current state of the channel info.
     */
    public val state: StateFlow<ChannelInfoViewState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ChannelInfoViewEvent>(extraBufferCapacity = 1)

    /**
     * A [SharedFlow] that emits one-shot events related to channel info, such as errors or success events.
     */
    public val events: SharedFlow<ChannelInfoViewEvent> = _events.asSharedFlow()

    init {
        @Suppress("OPT_IN_USAGE")
        globalState.flatMapLatest { global ->
            channelState.flatMapLatest { channel ->
                logger.d { "[onChannelState]" }
                combine(
                    combine(
                        channel.channelData.onEach {
                            logger.d {
                                "[onChannelData] cid: ${it.cid}, name: ${it.name}, " +
                                    "capabilities: ${it.ownCapabilities}"
                            }
                        },
                        channel.members.onEach { logger.d { "[onMembers] size: ${it.size}" } },
                        channel.muted.onEach { logger.d { "[onMuted] $it" } },
                        channel.hidden.onEach { logger.d { "[onHidden] $it" } },
                        ::ChannelBaseData,
                    ),
                    global.muted,
                    global.blockedUserIds,
                ) { data4, mutedUsers, blockedUserIds ->
                    ChannelInfoData(
                        channelData = data4.channelData,
                        members = data4.members,
                        isMuted = data4.isMuted,
                        isHidden = data4.isHidden,
                        mutedUsers = mutedUsers,
                        blockedUserIds = blockedUserIds,
                    )
                }
            }
        }
            .distinctUntilChanged()
            .onEach { data ->
                onChannelInfoData(data)
            }
            .launchIn(scope)
    }

    private fun onChannelInfoData(data: ChannelInfoData) {
        val contentMembers = data.members
            .run {
                // Do not filter out the current user if the channel is a group channel or if there is only one member
                takeIf { size == 1 || data.channelData.isGroupChannel } ?: filterNotCurrentUser()
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
            val singleMember = if (contentMembers.size == 1) contentMembers.first() else null
            ChannelInfoViewState.Content(
                owner = data.channelData.createdBy,
                members = expandableMembers,
                options = buildChannelOptionList(
                    channelData = data.channelData,
                    singleMember = singleMember,
                    isMuted = data.isMuted,
                    isHidden = data.isHidden,
                    mutedUsers = data.mutedUsers,
                    blockedUserIds = data.blockedUserIds,
                ).mapNotNull { option ->
                    if (optionFilter(option)) option else null
                },
            )
        }
    }

    /**
     * Handles actions related to channel information view.
     *
     * @param action The [ChannelInfoViewAction] representing the action to be handled.
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

            is ChannelInfoViewAction.MediaAttachmentsClick ->
                _events.tryEmit(ChannelInfoViewEvent.NavigateToMediaAttachments)

            is ChannelInfoViewAction.FilesAttachmentsClick ->
                _events.tryEmit(ChannelInfoViewEvent.NavigateToFilesAttachments)

            is ChannelInfoViewAction.MuteChannelClick -> setChannelMute(mute = true)
            is ChannelInfoViewAction.UnmuteChannelClick -> setChannelMute(mute = false)
            is ChannelInfoViewAction.MuteUserClick -> muteUser()
            is ChannelInfoViewAction.UnmuteUserClick -> unmuteUser()
            is ChannelInfoViewAction.BlockUserClick -> blockUser()
            is ChannelInfoViewAction.UnblockUserClick -> unblockUser()
            is ChannelInfoViewAction.HideChannelClick -> _events.tryEmit(ChannelInfoViewEvent.HideChannelModal)
            is ChannelInfoViewAction.HideChannelConfirmationClick -> setChannelHide(hide = true, action.clearHistory)
            is ChannelInfoViewAction.UnhideChannelClick -> setChannelHide(hide = false)
            is ChannelInfoViewAction.LeaveChannelClick -> _events.tryEmit(ChannelInfoViewEvent.LeaveChannelModal)
            is ChannelInfoViewAction.LeaveChannelConfirmationClick -> leaveChannel(action.quitMessage)
            is ChannelInfoViewAction.DeleteChannelClick -> _events.tryEmit(ChannelInfoViewEvent.DeleteChannelModal)
            is ChannelInfoViewAction.DeleteChannelConfirmationClick -> deleteChannel()
            is ChannelInfoViewAction.BanMemberConfirmationClick -> banMember(action.memberId, action.timeoutInMinutes)
            is ChannelInfoViewAction.RemoveMemberConfirmationClick -> removeMember(action.memberId)
        }
    }

    /**
     * Propagates events from the [ChannelInfoMemberViewEvent] to the [ChannelInfoViewEvent].
     */
    public fun onMemberViewEvent(event: ChannelInfoMemberViewEvent) {
        logger.d { "[onMemberViewEvent] event: $event" }
        when (event) {
            is ChannelInfoMemberViewEvent.MessageMember -> if (event.distinctCid != null) {
                _events.tryEmit(ChannelInfoViewEvent.NavigateToChannel(event.distinctCid))
            } else {
                _events.tryEmit(ChannelInfoViewEvent.NavigateToDraftChannel(event.memberId))
            }

            is ChannelInfoMemberViewEvent.MuteUser -> muteUser(event.member.getUserId())

            is ChannelInfoMemberViewEvent.UnmuteUser -> unmuteUser(event.member.getUserId())

            is ChannelInfoMemberViewEvent.BlockUser -> blockUser(event.member.getUserId())

            is ChannelInfoMemberViewEvent.UnblockUser -> unblockUser(event.member.getUserId())

            is ChannelInfoMemberViewEvent.BanMember ->
                _events.tryEmit(ChannelInfoViewEvent.BanMemberModal(event.member))

            is ChannelInfoMemberViewEvent.UnbanMember -> unbanMember(event.member.getUserId())

            is ChannelInfoMemberViewEvent.RemoveMember ->
                _events.tryEmit(ChannelInfoViewEvent.RemoveMemberModal(event.member))
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
                cid = cid,
                member = action.member,
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

    private fun getDmMemberId(): String? {
        val content = _state.value as? ChannelInfoViewState.Content ?: return null
        return content.members.firstOrNull()?.getUserId()
    }

    private fun muteUser(userId: String? = getDmMemberId()) {
        userId ?: return
        logger.d { "[muteUser] userId: $userId" }
        scope.launch {
            chatClient.muteUser(userId).await()
                .onError { error ->
                    logger.e { "[muteUser] error: ${error.message}" }
                    _events.tryEmit(ChannelInfoViewEvent.MuteUserError)
                }
        }
    }

    private fun unmuteUser(userId: String? = getDmMemberId()) {
        userId ?: return
        logger.d { "[unmuteUser] userId: $userId" }
        scope.launch {
            chatClient.unmuteUser(userId).await()
                .onError { error ->
                    logger.e { "[unmuteUser] error: ${error.message}" }
                    _events.tryEmit(ChannelInfoViewEvent.UnmuteUserError)
                }
        }
    }

    private fun blockUser(userId: String? = getDmMemberId()) {
        userId ?: return
        logger.d { "[blockUser] userId: $userId" }
        scope.launch {
            chatClient.blockUser(userId).await()
                .onError { error ->
                    logger.e { "[blockUser] error: ${error.message}" }
                    _events.tryEmit(ChannelInfoViewEvent.BlockUserError)
                }
        }
    }

    private fun unblockUser(userId: String? = getDmMemberId()) {
        userId ?: return
        logger.d { "[unblockUser] userId: $userId" }
        scope.launch {
            chatClient.unblockUser(userId).await()
                .onError { error ->
                    logger.e { "[unblockUser] error: ${error.message}" }
                    _events.tryEmit(ChannelInfoViewEvent.UnblockUserError)
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

    private fun banMember(memberId: String, timeout: Int?) {
        logger.d { "[banMember] memberId: $memberId" }

        scope.launch {
            channelClient.banUser(
                targetId = memberId,
                reason = null,
                timeout = timeout,
            ).await()
                .onSuccess { /* no-op */ }
                .onError { error ->
                    logger.e { "[banMember] error: ${error.message}" }
                    _events.tryEmit(ChannelInfoViewEvent.BanMemberError)
                }
        }
    }

    private fun unbanMember(memberId: String) {
        logger.d { "[unbanMember] memberId: $memberId" }

        scope.launch {
            channelClient.unbanUser(memberId).await()
                .onSuccess { /* no-op */ }
                .onError { error ->
                    logger.e { "[unbanMember] error: ${error.message}" }
                    _events.tryEmit(ChannelInfoViewEvent.UnbanMemberError)
                }
        }
    }

    private fun removeMember(memberId: String) {
        logger.d { "[removeMember] memberId: $memberId" }

        removeMemberFromChannel(
            memberId = memberId,
            systemMessage = null,
            onSuccess = { /* no-op */ },
            onError = { error ->
                logger.e { "[removeMember] error: ${error.message}" }
                _events.tryEmit(ChannelInfoViewEvent.RemoveMemberError)
            },
        )
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

    private fun List<Member>.filterNotCurrentUser() =
        filter { member -> member.user.id != chatClient.getCurrentUser()?.id }
}

private const val MINIMUM_VISIBLE_MEMBERS = 5

private data class ChannelBaseData(
    val channelData: ChannelData,
    val members: List<Member>,
    val isMuted: Boolean,
    val isHidden: Boolean,
)

private data class ChannelInfoData(
    val channelData: ChannelData,
    val members: List<Member>,
    val isMuted: Boolean,
    val isHidden: Boolean,
    val mutedUsers: List<Mute>,
    val blockedUserIds: List<String>,
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

@Suppress("LongParameterList")
private fun buildChannelOptionList(
    channelData: ChannelData,
    singleMember: Member?,
    isMuted: Boolean,
    isHidden: Boolean,
    mutedUsers: List<Mute>,
    blockedUserIds: List<String>,
) = buildList {
    val isDmChannel = singleMember != null && !channelData.isGroupChannel
    if (channelData.isGroupChannel &&
        channelData.ownCapabilities.contains(ChannelCapabilities.UPDATE_CHANNEL_MEMBERS)
    ) {
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
    if (isDmChannel) {
        // DM channel: user-level mute instead of channel mute, no hide
        add(ChannelInfoViewState.Content.Option.PinnedMessages)
        add(ChannelInfoViewState.Content.Option.MediaAttachments)
        add(ChannelInfoViewState.Content.Option.FilesAttachments)
        val isUserMuted = mutedUsers.any { it.target?.id == singleMember.getUserId() }
        add(ChannelInfoViewState.Content.Option.MuteUser(isMuted = isUserMuted))
        val isUserBlocked = blockedUserIds.contains(singleMember.getUserId())
        add(ChannelInfoViewState.Content.Option.BlockUser(isBlocked = isUserBlocked))
        if (channelData.ownCapabilities.contains(ChannelCapabilities.DELETE_CHANNEL)) {
            add(ChannelInfoViewState.Content.Option.DeleteChannel)
        }
    } else {
        // Group channel: channel-level mute, hide, leave
        if (channelData.ownCapabilities.contains(ChannelCapabilities.MUTE_CHANNEL)) {
            add(ChannelInfoViewState.Content.Option.MuteChannel(isMuted))
        }
        add(ChannelInfoViewState.Content.Option.HideChannel(isHidden))
        add(ChannelInfoViewState.Content.Option.PinnedMessages)
        add(ChannelInfoViewState.Content.Option.MediaAttachments)
        add(ChannelInfoViewState.Content.Option.FilesAttachments)
        if (channelData.ownCapabilities.contains(ChannelCapabilities.DELETE_CHANNEL)) {
            add(ChannelInfoViewState.Content.Option.DeleteChannel)
        } else if (channelData.ownCapabilities.contains(ChannelCapabilities.LEAVE_CHANNEL)) {
            add(ChannelInfoViewState.Content.Option.LeaveChannel)
        }
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
