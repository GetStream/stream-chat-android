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

package io.getstream.chat.android.compose.ui.messages.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import io.getstream.chat.android.client.utils.message.belongsToThread
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.client.utils.message.isPinned
import io.getstream.chat.android.client.utils.message.isPoll
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.state.messages.MessageAlignment
import io.getstream.chat.android.compose.state.messages.MessageReactionItemState
import io.getstream.chat.android.compose.ui.components.messages.MessageContent
import io.getstream.chat.android.compose.ui.components.messages.MessageHeaderLabel
import io.getstream.chat.android.compose.ui.components.messages.PollMessageContent
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageReactionsParams
import io.getstream.chat.android.compose.ui.theme.MessageStyling
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.compose.ui.util.ifNotNull
import io.getstream.chat.android.compose.ui.util.isEmojiOnlyWithoutBubble
import io.getstream.chat.android.compose.ui.util.isErrorOrFailed
import io.getstream.chat.android.compose.ui.util.isUploading
import io.getstream.chat.android.compose.util.extensions.canReplyToMessage
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.ReactionSorting
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.ui.common.state.messages.list.DeletedMessageVisibility
import io.getstream.chat.android.ui.common.state.messages.list.GiphyAction
import io.getstream.chat.android.ui.common.state.messages.list.MessageFocused
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.MessagePosition
import io.getstream.chat.android.ui.common.state.messages.poll.PollSelectionType
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * The default message container for a regular message in the Conversation/Messages screen.
 *
 * It shows the avatar and the message details, which can have a header (reactions), the content which
 * can be a text message, file or image attachment, or a custom attachment and the footer, which can
 * be a deleted message footer (if we own the message) or the default footer, which contains a timestamp
 * or the thread information.
 *
 * It also allows for long click and thread click events.
 *
 * @param messageItem The message item to show, which holds the message and the group position, if the message is in
 * a group of messages from the same user.
 * @param reactionSorting The sorting for the reactions, if we have any.
 * @param onLongItemClick Handler when the user selects a message, on long tap.
 * @param modifier Modifier for styling.
 * @param onReactionsClick Handler when the user taps on message reactions.
 * @param onThreadClick Handler for thread clicks, if this message has a thread going.
 * @param onCastVote Handler for casting a vote on an option.
 * @param onRemoveVote Handler for removing a vote on an option.
 * @param selectPoll Handler for selecting a poll.
 * @param onAddAnswer Handler for adding an answer to a poll.
 * @param onClosePoll Handler for closing a poll.
 * @param onAddPollOption Handler for adding a poll option.
 * @param onGiphyActionClick Handler when the user taps on an action button in a giphy message item.
 * @param onQuotedMessageClick Handler for quoted message click action.
 * @param onUserAvatarClick Handler when users avatar is clicked.
 * @param onLinkClick Handler for clicking on a link in the message.
 * @param onMediaGalleryPreviewResult Handler when the user selects an option in the Media Gallery Preview screen.
 */
@Suppress("LongMethod")
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun MessageContainer(
    messageItem: MessageItemState,
    reactionSorting: ReactionSorting,
    onLongItemClick: (Message) -> Unit,
    modifier: Modifier = Modifier,
    onReactionsClick: (Message) -> Unit = {},
    onThreadClick: (Message) -> Unit = {},
    onPollUpdated: (Message, Poll) -> Unit = { _, _ -> },
    onCastVote: (Message, Poll, Option) -> Unit = { _, _, _ -> },
    onRemoveVote: (Message, Poll, Vote) -> Unit = { _, _, _ -> },
    selectPoll: (Message, Poll, PollSelectionType) -> Unit = { _, _, _ -> },
    onAddAnswer: (message: Message, poll: Poll, answer: String) -> Unit = { _, _, _ -> },
    onClosePoll: (String) -> Unit = {},
    onAddPollOption: (poll: Poll, option: String) -> Unit = { _, _ -> },
    onGiphyActionClick: (GiphyAction) -> Unit = {},
    onQuotedMessageClick: (Message) -> Unit = {},
    onUserAvatarClick: (() -> Unit)? = null,
    onLinkClick: ((Message, String) -> Unit)? = null,
    onUserMentionClick: (User) -> Unit = {},
    onReply: (Message) -> Unit = {},
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
) {
    val message = messageItem.message
    val focusState = messageItem.focusState
    val haptic = LocalHapticFeedback.current

    val clickModifier = Modifier.combinedClickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = { if (message.belongsToThread()) onThreadClick(message) },
        onLongClick = {
            if (!message.isDeleted() && !message.isUploading()) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onLongItemClick(message)
            }
        },
    )

    val backgroundColor = when (focusState is MessageFocused || message.isPinned(ChatTheme.timeProvider)) {
        true -> ChatTheme.colors.highlight
        else -> ChatTheme.colors.highlight.copy(alpha = 0f) // Ensures a smooth fade-out without unwanted color shifts.
    }

    val color by animateColorAsState(
        targetValue = backgroundColor,
        animationSpec = tween(durationMillis = HighlightFadeOutDurationMillis),
    )

    val messageAlignment = ChatTheme.messageAlignmentProvider.provideMessageAlignment(messageItem)
    val description = stringResource(id = R.string.stream_compose_cd_message_item)
    val isSwipable = ChatTheme.messageOptionsTheme.optionVisibility
        .canReplyToMessage(
            message = message,
            ownCapabilities = messageItem.ownCapabilities,
        )

    // Remember the message to ensure updated values are captured in the onReply lambda
    val replyMessage by rememberUpdatedState(message)
    SwipeToReply(
        modifier = modifier,
        onReply = { onReply(replyMessage) },
        isSwipeable = { isSwipable },
    ) {
        Box(
            modifier = Modifier
                .testTag("Stream_MessageItem")
                .fillMaxWidth()
                .wrapContentHeight()
                .background(color = color)
                .semantics { contentDescription = description },
            contentAlignment = messageAlignment.itemAlignment,
        ) {
            Row(
                Modifier
                    .wrapContentWidth()
                    .then(clickModifier)
                    .testTag("Stream_MessageCell"),
            ) {
                with(ChatTheme.componentFactory) {
                    when (messageAlignment) {
                        MessageAlignment.Start -> MessageAuthor(
                            messageItem = messageItem,
                            onUserAvatarClick = onUserAvatarClick,
                        )

                        MessageAlignment.End -> MessageSpacer(messageItem)
                    }

                    Column(
                        modifier = Modifier.weight(1f, fill = false),
                        horizontalAlignment = messageAlignment.contentAlignment,
                    ) {
                        MessageTop(
                            messageItem = messageItem,
                            reactionSorting = reactionSorting,
                            onReactionsClick = onReactionsClick,
                        )
                        MessageContentWithReactions(
                            messageAlignment = messageAlignment,
                            reactions = rememberMessageReactions(message, reactionSorting)?.let { reactions ->
                                {
                                    MessageReactions(
                                        params = MessageReactionsParams(
                                            message = message,
                                            reactions = reactions,
                                            onClick = onReactionsClick,
                                        ),
                                    )
                                }
                            },
                            content = {
                                MessageContent(
                                    messageItem = messageItem,
                                    onLongItemClick = onLongItemClick,
                                    onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                                    onGiphyActionClick = onGiphyActionClick,
                                    onQuotedMessageClick = onQuotedMessageClick,
                                    onLinkClick = onLinkClick,
                                    onUserMentionClick = onUserMentionClick,
                                    onPollUpdated = onPollUpdated,
                                    onCastVote = onCastVote,
                                    onRemoveVote = onRemoveVote,
                                    selectPoll = selectPoll,
                                    onAddAnswer = onAddAnswer,
                                    onClosePoll = onClosePoll,
                                    onAddPollOption = onAddPollOption,
                                )
                            },
                        )
                        MessageBottom(messageItem = messageItem)
                    }

                    when (messageAlignment) {
                        MessageAlignment.Start -> MessageSpacer(messageItem)
                        MessageAlignment.End -> MessageAuthor(
                            messageItem = messageItem,
                            onUserAvatarClick = onUserAvatarClick,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Represents the default author content for a message.
 *
 * By default, we show a user avatar if the message doesn't belong to the current user.
 *
 * @param messageItem The message item to show the content for.
 * @param onUserAvatarClick Handler when the user taps on the avatar.
 */
@Composable
internal fun RowScope.DefaultMessageAuthor(
    messageItem: MessageItemState,
    onUserAvatarClick: (() -> Unit)? = null,
) {
    val modifier = Modifier
        .padding(start = 8.dp, end = 8.dp)
        .size(24.dp)
        .align(Alignment.Bottom)

    if (messageItem.isMine) {
        // By default, the avatar is not show in outgoing messages
        Spacer(modifier = Modifier.width(8.dp))
    } else if (messageItem.showMessageFooter ||
        messageItem.groupPosition == MessagePosition.BOTTOM ||
        messageItem.groupPosition == MessagePosition.NONE
    ) {
        ChatTheme.componentFactory.UserAvatar(
            modifier = modifier
                .testTag("Stream_UserAvatar")
                .ifNotNull(onUserAvatarClick) { clickable(onClick = it) },
            user = messageItem.message.user,
            showIndicator = false,
            showBorder = false,
        )
    } else {
        Spacer(modifier = modifier)
    }
}

/**
 * Represents the default top content inside the message bubble.
 *
 * By default, we show if the message is pinned and thread labels.
 *
 * @param messageItem The message item to show the content for.
 * @param reactionSorting The sorting for the reactions, if we have any.
 * @param onReactionsClick Handler when the user taps on message reactions.
 */
@Suppress("LongMethod")
@Composable
internal fun DefaultMessageTop(
    messageItem: MessageItemState,
    reactionSorting: ReactionSorting,
    onReactionsClick: (Message) -> Unit = {},
) {
    val message = messageItem.message
    val currentUser = messageItem.currentUser

    if (message.isPinned(ChatTheme.timeProvider)) {
        val pinnedByUser = if (message.pinnedBy?.id == currentUser?.id) {
            stringResource(id = R.string.stream_compose_message_list_you)
        } else {
            message.pinnedBy?.name
        }

        val pinnedByText = if (pinnedByUser != null) {
            stringResource(id = R.string.stream_compose_pinned_to_channel_by, pinnedByUser)
        } else {
            null
        }

        MessageHeaderLabel(
            painter = painterResource(id = R.drawable.stream_compose_ic_message_pinned),
            text = pinnedByText,
        )
    }

    if (message.showInChannel) {
        val alsoSendToChannelTextRes = if (messageItem.isInThread) {
            R.string.stream_compose_also_sent_to_channel
        } else {
            R.string.stream_compose_replied_to_thread
        }

        MessageHeaderLabel(
            painter = painterResource(id = R.drawable.stream_compose_ic_thread),
            text = stringResource(alsoSendToChannelTextRes),
        )
    }
}

/**
 * Lays out message content with optional reactions above it. The layout width is determined by the message content
 * only. Reactions are allowed to overflow beyond the content bounds.
 */
@Composable
private fun MessageContentWithReactions(
    messageAlignment: MessageAlignment,
    reactions: (@Composable () -> Unit)?,
    content: @Composable () -> Unit,
) {
    Layout(
        content = {
            if (reactions != null) reactions()
            content()
        },
    ) { measurables, constraints ->
        val hasReactions = reactions != null
        val contentPlaceable = measurables.last().measure(constraints)
        val reactionsPlaceable = if (hasReactions) {
            measurables.first().measure(constraints.copy(minWidth = 0))
        } else {
            null
        }

        val overlapPx = if (reactionsPlaceable != null) StreamTokens.spacingXs.roundToPx() else 0
        val reactionsHeight = reactionsPlaceable?.height ?: 0
        val width = contentPlaceable.width
        val height = reactionsHeight + contentPlaceable.height - overlapPx

        layout(width, height) {
            reactionsPlaceable?.let { rp ->
                val protrusionPx = StreamTokens.spacingXs.roundToPx()
                // The reactions are aligned opposite to the message alignment
                val x = when (messageAlignment) {
                    MessageAlignment.End -> minOf(0, width - rp.width) - protrusionPx
                    MessageAlignment.Start -> maxOf(0, width - rp.width) + protrusionPx
                }
                rp.place(x, 0, zIndex = 1f)
            }
            contentPlaceable.place(0, reactionsHeight - overlapPx)
        }
    }
}

@Composable
private fun rememberMessageReactions(message: Message, sorting: ReactionSorting): List<MessageReactionItemState>? =
    if (message.isDeleted()) {
        null
    } else {
        val resolver = ChatTheme.reactionResolver
        remember(resolver, message.reactionGroups, sorting) {
            val supported = resolver.supportedReactions
            message.reactionGroups
                .entries
                .filter { it.key in supported }
                .sortedWith { o1, o2 -> sorting.compare(o1.value, o2.value) }
                .map { (type, group) ->
                    MessageReactionItemState(
                        type = type,
                        emoji = resolver.emojiCode(type),
                        count = group.count,
                    )
                }
                .takeIf { it.isNotEmpty() }
        }
    }

/**
 * Represents the default bottom content inside the message bubble.
 *
 * By default, the following can be shown:
 * - uploading status
 * - thread participants
 * - message timestamp
 *
 * @param messageItem The message item to show the content for.
 */
@Composable
internal fun ColumnScope.DefaultMessageBottom(
    messageItem: MessageItemState,
) {
    val message = messageItem.message
    when {
        message.isUploading() -> {
            ChatTheme.componentFactory.MessageFooterUploadingContent(
                modifier = Modifier.align(Alignment.End),
                messageItem = messageItem,
            )
        }

        message.isDeleted() && !message.deletedForMe &&
            messageItem.deletedMessageVisibility == DeletedMessageVisibility.VISIBLE_FOR_CURRENT_USER -> {
            ChatTheme.componentFactory.MessageFooterOnlyVisibleToYouContent(
                messageItem = messageItem,
            )
        }

        else -> ChatTheme.componentFactory.MessageFooterContent(messageItem = messageItem)
    }

    val position = messageItem.groupPosition
    val spacerSize = if (position == MessagePosition.NONE || position == MessagePosition.BOTTOM) 4.dp else 2.dp

    Spacer(Modifier.size(spacerSize))
}

/**
 * Represents the default content inside the message bubble.
 *
 * By default, we show a message bubble with attachments or emoji stickers if message is emoji only.
 *
 * @param messageItem The message item to show the content for.
 * @param modifier Modifier for styling.
 * @param onLongItemClick Handler when the user selects a message, on long tap.
 * @param onGiphyActionClick Handler when the user taps on an action button in a giphy message item.
 * @param onQuotedMessageClick Handler for quoted message click action.
 * @param onLinkClick Handler for clicking on a link in the message.
 * @param onMediaGalleryPreviewResult Handler when the user selects an option in the Media Gallery Preview screen.
 * @param onCastVote Handler when a user cast a vote on an option.
 * @param onRemoveVote Handler when a user cast a remove on an option.
 * @param onClosePoll Handler when a user close a poll.
 * @param onAddPollOption Handler when a user add a poll option.
 */
@Suppress("LongParameterList")
@Composable
public fun DefaultMessageContent(
    modifier: Modifier = Modifier,
    messageItem: MessageItemState,
    onLongItemClick: (Message) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit = {},
    onQuotedMessageClick: (Message) -> Unit = {},
    onLinkClick: ((Message, String) -> Unit)? = null,
    onUserMentionClick: (User) -> Unit = {},
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    onPollUpdated: (Message, Poll) -> Unit,
    onCastVote: (Message, Poll, Option) -> Unit,
    onRemoveVote: (Message, Poll, Vote) -> Unit,
    selectPoll: (Message, Poll, PollSelectionType) -> Unit,
    onAddAnswer: (message: Message, poll: Poll, answer: String) -> Unit,
    onClosePoll: (String) -> Unit,
    onAddPollOption: (poll: Poll, option: String) -> Unit,
) {
    val finalModifier = modifier.widthIn(max = ChatTheme.dimens.messageItemMaxWidth)
    if (messageItem.message.isPoll() && !messageItem.message.isDeleted()) {
        val poll = messageItem.message.poll
        LaunchedEffect(key1 = poll) {
            if (poll != null) {
                onPollUpdated.invoke(messageItem.message, poll)
            }
        }

        PollMessageContent(
            modifier = finalModifier,
            messageItem = messageItem,
            onCastVote = onCastVote,
            onRemoveVote = onRemoveVote,
            selectPoll = selectPoll,
            onClosePoll = onClosePoll,
            onAddPollOption = onAddPollOption,
            onLongItemClick = onLongItemClick,
            onAddAnswer = onAddAnswer,
        )
    } else if (messageItem.message.isEmojiOnlyWithoutBubble()) {
        EmojiMessageContent(
            modifier = finalModifier,
            messageItem = messageItem,
            onLongItemClick = onLongItemClick,
            onGiphyActionClick = onGiphyActionClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
            onQuotedMessageClick = onQuotedMessageClick,
        )
    } else {
        RegularMessageContent(
            modifier = finalModifier,
            messageItem = messageItem,
            onLongItemClick = onLongItemClick,
            onGiphyActionClick = onGiphyActionClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
            onQuotedMessageClick = onQuotedMessageClick,
            onLinkClick = onLinkClick,
            onUserMentionClick = onUserMentionClick,
        )
    }
}

/**
 * Message content when the message consists only of emoji.
 *
 * @param messageItem The message item to show the content for.
 * @param modifier Modifier for styling.
 * @param onLongItemClick Handler when the user selects a message, on long tap.
 * @param onGiphyActionClick Handler when the user taps on an action button in a giphy message item.
 * @param onQuotedMessageClick Handler for quoted message click action.
 * @param onMediaGalleryPreviewResult Handler used when the user selects an option in the Media Gallery Preview screen.
 */
@Composable
public fun EmojiMessageContent(
    messageItem: MessageItemState,
    modifier: Modifier = Modifier,
    onLongItemClick: (Message) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit = {},
    onQuotedMessageClick: (Message) -> Unit = {},
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
) {
    val message = messageItem.message

    if (!messageItem.isErrorOrFailed()) {
        MessageContent(
            message = message,
            currentUser = messageItem.currentUser,
            onLongItemClick = onLongItemClick,
            onGiphyActionClick = onGiphyActionClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
            onQuotedMessageClick = onQuotedMessageClick,
        )
    } else {
        Box(modifier = modifier) {
            MessageContent(
                message = message,
                currentUser = messageItem.currentUser,
                onLongItemClick = onLongItemClick,
                onGiphyActionClick = onGiphyActionClick,
                onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                onQuotedMessageClick = onQuotedMessageClick,
            )

            ChatTheme.componentFactory.MessageFailedIcon(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomEnd),
                message = message,
            )
        }
    }
}

/**
 * Message content for messages which consist of more than just emojis.
 *
 * @param messageItem The message item to show the content for.
 * @param modifier Modifier for styling.
 * @param onLongItemClick Handler when the user selects a message, on long tap.
 * @param onGiphyActionClick Handler when the user taps on an action button in a giphy message item.
 * @param onQuotedMessageClick Handler for quoted message click action.
 * @param onLinkClick Handler for clicking on a link in the message.
 * @param onMediaGalleryPreviewResult Handler when the user selects an option in the Media Gallery Preview screen.
 */
@Composable
public fun RegularMessageContent(
    messageItem: MessageItemState,
    modifier: Modifier = Modifier,
    onLongItemClick: (Message) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit = {},
    onQuotedMessageClick: (Message) -> Unit = {},
    onLinkClick: ((Message, String) -> Unit)? = null,
    onUserMentionClick: (User) -> Unit = {},
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
) {
    val message = messageItem.message
    val ownsMessage = messageItem.isMine

    val messageAlignment = ChatTheme.messageAlignmentProvider.provideMessageAlignment(messageItem)
    val messageBubbleShape = MessageStyling.shape(messageItem.groupPosition, messageAlignment)
    val messageBubbleColor = MessageStyling.backgroundColor(ownsMessage)

    val content = @Composable {
        MessageContent(
            message = message,
            currentUser = messageItem.currentUser,
            onLongItemClick = onLongItemClick,
            onGiphyActionClick = onGiphyActionClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
            onQuotedMessageClick = onQuotedMessageClick,
            onLinkClick = onLinkClick,
            onUserMentionClick = onUserMentionClick,
        )
    }
    if (!messageItem.isErrorOrFailed()) {
        ChatTheme.componentFactory.MessageBubble(
            modifier = modifier,
            message = message,
            shape = messageBubbleShape,
            color = messageBubbleColor,
            border = null,
            content = content,
        )
    } else {
        Box(modifier = modifier) {
            ChatTheme.componentFactory.MessageBubble(
                modifier = Modifier.padding(end = 12.dp),
                message = message,
                shape = messageBubbleShape,
                color = messageBubbleColor,
                border = null,
                content = content,
            )

            ChatTheme.componentFactory.MessageFailedIcon(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomEnd)
                    .testTag("Stream_MessageFailedIcon"),
                message = message,
            )
        }
    }
}

/**
 * Represent a swipe to reply content.
 *
 * The swipe-to-reply content is provided through [ChatTheme.componentFactory].
 *
 * @param modifier Modifier for styling.
 * @param onReply Handler when the user swipes to reply.
 * @param isSwipeable Handler to determine if the message is swipeable.
 * @param content The swipeable content to show when not swiping to reply.
 */
@Suppress("LongMethod")
@Composable
private fun SwipeToReply(
    modifier: Modifier = Modifier,
    onReply: () -> Unit = {},
    isSwipeable: () -> Boolean = { true },
    content: @Composable () -> Unit,
) {
    var swipeToReplyWidth by remember { mutableFloatStateOf(0f) }
    var rowWidth by remember { mutableFloatStateOf(0f) }
    val offset = remember { Animatable(initialValue = 0f) }
    val scope = rememberCoroutineScope()
    val view = LocalView.current

    Box(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .onSizeChanged { swipeToReplyWidth = it.width.toFloat() }
                .offset {
                    val roundToInt = swipeToReplyWidth.roundToInt()
                    IntOffset(
                        x = (offset.value.roundToInt() - roundToInt)
                            .coerceIn(-roundToInt, roundToInt),
                        y = 0,
                    )
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            with(ChatTheme.componentFactory) {
                SwipeToReplyContent()
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { rowWidth = it.width.toFloat() }
                .offset { IntOffset(x = offset.value.roundToInt(), y = 0) }
                .pointerInput(swipeToReplyWidth) {
                    if (isSwipeable()) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, dragAmount ->
                                // Only consume if horizontal drag dominates vertical
                                if (change.positionChange().x.absoluteValue > change.positionChange().y.absoluteValue) {
                                    scope.launch {
                                        val newOffset = (offset.value + dragAmount)
                                            .coerceIn(0f, maxOf((rowWidth / 2), swipeToReplyWidth))
                                        offset.snapTo(newOffset)
                                    }
                                } else {
                                    change.consume()
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    if (offset.value >= swipeToReplyWidth) {
                                        view.performHapticFeedback(HapticFeedbackConstantsCompat.CONFIRM)
                                        onReply()
                                    }
                                    offset.animateTo(0f)
                                }
                            },
                        )
                    }
                },
        ) {
            content()
        }
    }
}

/**
 * Represents the time the highlight fade out transition will take.
 */
public const val HighlightFadeOutDurationMillis: Int = 2000
