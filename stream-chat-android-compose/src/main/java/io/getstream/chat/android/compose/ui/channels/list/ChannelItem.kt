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

@file:Suppress("TooManyFunctions")

package io.getstream.chat.android.compose.ui.channels.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.client.extensions.currentUserUnreadCount
import io.getstream.chat.android.client.extensions.getCreatedAtOrNull
import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.ui.components.Timestamp
import io.getstream.chat.android.compose.ui.components.TypingIndicator
import io.getstream.chat.android.compose.ui.theme.ChannelListConfig
import io.getstream.chat.android.compose.ui.theme.ChatConfig
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MuteIndicatorPosition
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.getLastMessage
import io.getstream.chat.android.compose.ui.util.getLastMessageIncludingDeleted
import io.getstream.chat.android.compose.ui.util.isOneToOne
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewChannelData
import io.getstream.chat.android.previewdata.PreviewChannelUserRead
import io.getstream.chat.android.previewdata.PreviewUserData
import java.util.Date

/**
 * The basic channel item, that shows the channel in a list and exposes single and long click actions.
 *
 * @param channelItem The channel data to show.
 * @param currentUser The user that's currently logged in.
 * @param onChannelClick Handler for a single tap on an item.
 * @param onChannelLongClick Handler for a long tap on an item.
 * @param modifier Modifier for styling.
 * @param leadingContent Customizable composable function that represents the leading content of a channel item, usually
 * the avatar that holds an image of the channel or its members.
 * @param centerContent Customizable composable function that represents the center content of a channel item, usually
 * holding information about its name and the last message.
 * @param trailingContent Customizable composable function that represents the trailing content of the a channel item,
 * usually information about the last message and the number of unread messages.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun ChannelItem(
    channelItem: ItemState.ChannelItemState,
    currentUser: User?,
    onChannelClick: (Channel) -> Unit,
    onChannelLongClick: (Channel) -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: @Composable RowScope.(ItemState.ChannelItemState) -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelItemLeadingContent(
                channelItem = channelItem,
                currentUser = currentUser,
            )
        }
    },
    centerContent: @Composable RowScope.(ItemState.ChannelItemState) -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelItemCenterContent(
                channelItem = channelItem,
                currentUser = currentUser,
            )
        }
    },
    trailingContent: @Composable RowScope.(ItemState.ChannelItemState) -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelItemTrailingContent(
                channelItem = channelItem,
                currentUser = currentUser,
            )
        }
    },
) {
    val channel = channelItem.channel
    val description = stringResource(id = R.string.stream_compose_cd_channel_item)

    Column(
        modifier = modifier
            .testTag("Stream_ChannelItem")
            .fillMaxWidth()
            .wrapContentHeight()
            .semantics { contentDescription = description }
            .combinedClickable(
                onClick = { onChannelClick(channel) },
                onLongClick = { onChannelLongClick(channel) },
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() },
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingContent(channelItem)

            centerContent(channelItem)

            trailingContent(channelItem)
        }
    }
}

/**
 * Represents the default leading content of [ChannelItem], that shows the channel avatar.
 *
 * @param channelItem The channel to show the avatar of.
 * @param currentUser The currently logged in user.
 */
@Composable
internal fun DefaultChannelItemLeadingContent(
    channelItem: ItemState.ChannelItemState,
    currentUser: User?,
) {
    ChatTheme.componentFactory.ChannelAvatar(
        modifier = Modifier
            .padding(
                start = StreamTokens.spacingMd, // 16dp (was channelItemHorizontalPadding = 8dp)
                end = StreamTokens.spacingMd, // 16dp gap between avatar and content column
                top = StreamTokens.spacingMd, // 16dp (was channelItemVerticalPadding = 12dp)
                bottom = StreamTokens.spacingMd, // 16dp (was channelItemVerticalPadding = 12dp)
            )
            // 48dp (new dimen, NOT channelAvatarSize which = 40dp for header)
            .size(ChatTheme.dimens.channelListItemAvatarSize),
        channel = channelItem.channel,
        currentUser = currentUser,
        showIndicator = false,
        showBorder = false,
    )
}

/**
 * Represents the center portion of [ChannelItem], that shows the channel display name
 * and the last message text preview.
 *
 * @param channelItemState The channel to show the info for.
 * @param currentUser The currently logged in user, used for data handling.
 */
@Suppress("LongMethod")
@Composable
internal fun RowScope.DefaultChannelItemCenterContent(
    channelItemState: ItemState.ChannelItemState,
    currentUser: User?,
) {
    val channel = channelItemState.channel
    val isDirectMessaging = channel.isOneToOne(currentUser)
    // Use raw last message (including deleted) for preview; fall back to filtered for non-deleted
    val rawLastMessage = channel.getLastMessageIncludingDeleted(currentUser)
    val isLastMessageDeleted = rawLastMessage?.isDeleted() == true
    val lastMessage = if (isLastMessageDeleted) rawLastMessage else channel.getLastMessage(currentUser)
    val unreadCount = channel.currentUserUnreadCount(currentUserId = currentUser?.id)
    val isLastMessageFromCurrentUser = lastMessage?.user?.id == currentUser?.id

    val mutePosition = ChatTheme.config.channelList.muteIndicatorPosition

    Column(
        modifier = Modifier
            .weight(1f)
            .padding(vertical = StreamTokens.spacing3xs), // 2dp vertical padding
        verticalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs), // 4dp gap between title and message rows
    ) {
        // ── Title Row: [Name] [MuteIcon?] [Timestamp] [Badge?] ──────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs), // 4dp default gap
        ) {
            // Channel name + optional inline mute icon (flex-1)
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
            ) {
                Text(
                    modifier = Modifier
                        .testTag("Stream_ChannelName")
                        .weight(1f, fill = false),
                    text = ChatTheme.channelNameFormatter.formatChannelName(channel, currentUser),
                    style = ChatTheme.typography.bodyDefault,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = ChatTheme.colors.textPrimary,
                )

                // Mute icon — inline in title row (INLINE_TITLE variant)
                if (channelItemState.isMuted && mutePosition == MuteIndicatorPosition.INLINE_TITLE) {
                    Icon(
                        modifier = Modifier
                            .testTag("Stream_ChannelMutedIcon")
                            .size(StreamTokens.size16),
                        painter = painterResource(id = R.drawable.stream_compose_ic_muted),
                        contentDescription = null,
                        tint = ChatTheme.colors.textTertiary,
                    )
                }
            }

            // Trailing: Timestamp + Badge (gap = spacing/xs = 8dp between them)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs), // 8dp between timestamp and badge
            ) {
                // Timestamp
                if (lastMessage != null) {
                    Timestamp(
                        date = lastMessage.getCreatedAtOrNull(),
                        textStyle = ChatTheme.typography.captionDefault.copy(
                            color = ChatTheme.colors.textTertiary,
                        ),
                    )
                }

                // Unread badge (optional)
                if (unreadCount > 0) {
                    ChatTheme.componentFactory.ChannelItemUnreadCountIndicator(
                        unreadCount = unreadCount,
                        modifier = Modifier,
                    )
                }
            }
        }

        // ── Message Row: [DeliveryStatus?] [MessagePreview or TypingIndicator] [MuteIcon?] ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs), // 4dp gap
        ) {
            if (channelItemState.typingUsers.isNotEmpty()) {
                // Typing indicator replaces message preview when active
                UserTypingIndicator(channelItemState.typingUsers, isDirectMessaging)
            } else {
                // Delivery status prefix: checkmark icon (only for own non-deleted messages)
                if (isLastMessageFromCurrentUser && lastMessage != null && !isLastMessageDeleted) {
                    ChatTheme.componentFactory.ChannelItemReadStatusIndicator(
                        channel = channel,
                        message = lastMessage,
                        currentUser = currentUser,
                        modifier = Modifier,
                    )
                }

                // Message preview text (draft overrides last message)
                val lastMessageText =
                    channelItemState.draftMessage
                        ?.let { ChatTheme.messagePreviewFormatter.formatDraftMessagePreview(it) }
                        ?: lastMessage?.let {
                            ChatTheme.messagePreviewFormatter.formatMessagePreview(
                                it, currentUser, isDirectMessaging,
                            )
                        }

                Text(
                    modifier = Modifier
                        .testTag("Stream_MessagePreview")
                        .weight(1f),
                    text = lastMessageText
                        ?: AnnotatedString(stringResource(R.string.stream_compose_no_messages_yet)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = ChatTheme.typography.captionDefault,
                    color = ChatTheme.colors.textSecondary,
                    inlineContent = ChatTheme.messagePreviewIconFactory.createPreviewIcons(),
                )
            }

            // Mute icon — at end of message row (TRAILING_BOTTOM variant)
            if (channelItemState.isMuted && mutePosition == MuteIndicatorPosition.TRAILING_BOTTOM) {
                Icon(
                    modifier = Modifier
                        .testTag("Stream_ChannelMutedIcon")
                        .size(StreamTokens.size16),
                    painter = painterResource(id = R.drawable.stream_compose_ic_muted),
                    contentDescription = null,
                    tint = ChatTheme.colors.textTertiary,
                )
            }
        }
    }
}

/**
 * Shows the user typing indicator for the provided users.
 *
 * @param users The list of users currently typing.
 */
@Composable
private fun UserTypingIndicator(users: List<User>, isDirectMessaging: Boolean) {
    val context = LocalContext.current
    val typingText = when {
        isDirectMessaging -> stringResource(R.string.stream_compose_channel_list_typing)
        users.size == 1 -> stringResource(R.string.stream_compose_channel_list_typing_one, users.first().name)
        users.size == 2 -> stringResource(
            R.string.stream_compose_channel_list_typing_two,
            users[0].name,
            users[1].name,
        )
        else -> context.resources.getQuantityString(
            R.plurals.stream_compose_channel_list_typing_many,
            users.size,
            users.size,
        )
    }
    Row(
        modifier = Modifier,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs), // 4dp (was 6dp)
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TypingIndicator()
        Text(
            modifier = Modifier.testTag("Stream_ChannelListTypingIndicator"),
            text = typingText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = ChatTheme.typography.captionDefault,
            color = ChatTheme.colors.textSecondary,
        )
    }
}

/**
 * Represents the default trailing content for the channel item. By default it shows
 * the information about the last message for the channel item, such as its read state,
 * timestamp and how many unread messages the user has.
 *
 * @param channel The channel to show the info for.
 * @param currentUser The currently logged in user, used for data handling.
 */
@Composable
internal fun RowScope.DefaultChannelItemTrailingContent(
    channel: Channel,
    currentUser: User?,
) {
    // Timestamp, badge, and delivery status have moved to DefaultChannelItemCenterContent.
    // This slot remains for API compatibility and provides the trailing end padding.
    Spacer(modifier = Modifier.width(StreamTokens.spacingMd)) // 16dp end padding
}

@Preview(showBackground = true)
@Composable
private fun ChannelItemNoMessagesPreview() {
    ChatTheme {
        ChannelItemNoMessages()
    }
}

@Composable
internal fun ChannelItemNoMessages() {
    ChannelItem(
        currentUser = PreviewUserData.user1,
        channel = PreviewChannelData.channelWithImage,
    )
}

@Preview(showBackground = true)
@Composable
private fun ChannelItemMutedPreview() {
    ChatTheme {
        ChannelItemMuted()
    }
}

@Composable
internal fun ChannelItemMuted() {
    ChannelItem(
        currentUser = PreviewUserData.user1,
        channel = PreviewChannelData.channelWithMessages,
        isMuted = true,
    )
}

@Preview(showBackground = true)
@Composable
private fun ChannelItemMutedTrailingBottomPreview() {
    ChatTheme(
        config = ChatConfig(
            channelList = ChannelListConfig(muteIndicatorPosition = MuteIndicatorPosition.TRAILING_BOTTOM),
        ),
    ) {
        ChannelItemMutedTrailingBottom()
    }
}

@Composable
internal fun ChannelItemMutedTrailingBottom() {
    ChannelItem(
        currentUser = PreviewUserData.user1,
        channel = PreviewChannelData.channelWithMessages,
        isMuted = true,
    )
}

@Preview(showBackground = true)
@Composable
private fun ChannelItemUnreadMessagesPreview() {
    ChatTheme {
        ChannelItemUnreadMessages()
    }
}

@Composable
internal fun ChannelItemUnreadMessages() {
    ChannelItem(
        currentUser = PreviewUserData.user1,
        channel = PreviewChannelData.channelWithMessages.copy(
            read = listOf(PreviewChannelUserRead.channelUserRead1),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ChannelItemLastMessagePendingStatusPreview() {
    ChatTheme {
        ChannelItemLastMessagePendingStatus()
    }
}

@Composable
internal fun ChannelItemLastMessagePendingStatus() {
    ChannelItem(
        currentUser = PreviewUserData.user1,
        channel = PreviewChannelData.channelWithMessages.copy(
            messages = PreviewChannelData.channelWithMessages.messages.map { message ->
                message.copy(user = PreviewUserData.user1, syncStatus = SyncStatus.SYNC_NEEDED)
            },
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ChannelItemLastMessageSentStatusPreview() {
    ChatTheme {
        ChannelItemLastMessageSentStatus()
    }
}

@Composable
internal fun ChannelItemLastMessageSentStatus() {
    ChannelItem(
        currentUser = PreviewUserData.user1,
        channel = PreviewChannelData.channelWithMessages.copy(
            messages = PreviewChannelData.channelWithMessages.messages.map { message ->
                message.copy(user = PreviewUserData.user1)
            },
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ChannelItemLastMessageDeliveredStatusPreview() {
    ChatTheme {
        ChannelItemLastMessageDeliveredStatus()
    }
}

@Composable
internal fun ChannelItemLastMessageDeliveredStatus() {
    ChannelItem(
        currentUser = PreviewUserData.user1,
        channel = PreviewChannelData.channelWithMessages.copy(
            messages = PreviewChannelData.channelWithMessages.messages.map { message ->
                message.copy(user = PreviewUserData.user1)
            },
            read = listOf(
                PreviewChannelUserRead.channelUserRead2.copy(
                    lastRead = NEVER,
                    lastDeliveredAt = Date(),
                ),
            ),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ChannelItemLastMessageSeenStatusPreview() {
    ChatTheme {
        ChannelItemLastMessageSeenStatus()
    }
}

@Composable
internal fun ChannelItemLastMessageSeenStatus() {
    ChannelItem(
        currentUser = PreviewUserData.user1,
        channel = PreviewChannelData.channelWithMessages.copy(
            messages = PreviewChannelData.channelWithMessages.messages.map { message ->
                message.copy(user = PreviewUserData.user1)
            },
            read = listOf(PreviewChannelUserRead.channelUserRead2),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ChannelItemDraftMessagePreview() {
    ChatTheme {
        ChannelItemDraftMessage()
    }
}

@Composable
internal fun ChannelItemDraftMessage() {
    ChannelItem(
        currentUser = PreviewUserData.user1,
        channel = PreviewChannelData.channelWithMessages,
        draftMessage = DraftMessage(text = "message"),
    )
}

@Composable
private fun ChannelItem(
    currentUser: User?,
    channel: Channel,
    isMuted: Boolean = false,
    draftMessage: DraftMessage? = null,
) {
    ChannelItem(
        channelItem = ItemState.ChannelItemState(
            channel = channel,
            isMuted = isMuted,
            draftMessage = draftMessage,
        ),
        currentUser = currentUser,
        onChannelClick = {},
        onChannelLongClick = {},
    )
}
