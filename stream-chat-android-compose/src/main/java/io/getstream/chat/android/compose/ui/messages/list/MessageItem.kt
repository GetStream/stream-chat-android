package io.getstream.chat.android.compose.ui.messages.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResult
import io.getstream.chat.android.compose.state.messages.list.DateSeparatorState
import io.getstream.chat.android.compose.state.messages.list.GiphyAction
import io.getstream.chat.android.compose.state.messages.list.MessageFocused
import io.getstream.chat.android.compose.state.messages.list.MessageItemGroupPosition.Bottom
import io.getstream.chat.android.compose.state.messages.list.MessageItemGroupPosition.Middle
import io.getstream.chat.android.compose.state.messages.list.MessageItemGroupPosition.None
import io.getstream.chat.android.compose.state.messages.list.MessageItemGroupPosition.Top
import io.getstream.chat.android.compose.state.messages.list.MessageItemState
import io.getstream.chat.android.compose.state.messages.list.MessageListItemState
import io.getstream.chat.android.compose.state.messages.list.SystemMessageState
import io.getstream.chat.android.compose.state.messages.list.ThreadSeparatorState
import io.getstream.chat.android.compose.state.reactionoptions.ReactionOptionItemState
import io.getstream.chat.android.compose.ui.attachments.content.MessageAttachmentsContent
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.components.messages.DefaultMessageContent
import io.getstream.chat.android.compose.ui.components.messages.GiphyMessageContent
import io.getstream.chat.android.compose.ui.components.messages.MessageBubble
import io.getstream.chat.android.compose.ui.components.messages.MessageDateSeparator
import io.getstream.chat.android.compose.ui.components.messages.MessageFooter
import io.getstream.chat.android.compose.ui.components.messages.MessageHeaderLabel
import io.getstream.chat.android.compose.ui.components.messages.MessageReactions
import io.getstream.chat.android.compose.ui.components.messages.MessageThreadSeparator
import io.getstream.chat.android.compose.ui.components.messages.OwnedMessageVisibilityContent
import io.getstream.chat.android.compose.ui.components.messages.SystemMessage
import io.getstream.chat.android.compose.ui.components.messages.UploadingFooter
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.hasThread
import io.getstream.chat.android.compose.ui.util.isDeleted
import io.getstream.chat.android.compose.ui.util.isGiphyEphemeral
import io.getstream.chat.android.compose.ui.util.isUploading

/**
 * Represents the time the highlight fade out transition will take.
 */
public const val HIGHLIGHT_FADE_OUT_DURATION_MILLIS: Int = 1000

/**
 * Represents the default message item that's shown for each item in the list.
 *
 * Detects if we're dealing with a [DateSeparatorState] or a [MessageItemState] and shows the required UI.
 *
 * @param messageListItem The item that holds the data.
 * @param modifier Modifier for styling.
 * @param onLongItemClick Handler when the user long taps on an item.
 * @param onThreadClick Handler when the user taps on a thread in a message item.
 * @param onGiphyActionClick Handler when the user taps on an action button in a giphy message item.
 * @param onImagePreviewResult Handler when the user receives a result from previewing message attachments.
 * @param systemMessageContent Customizable composable function that represents the system message item.
 * @param leadingContent The content shown at the start of a message list item. By default, we provide
 * [DefaultMessageItemLeadingContent], which shows a user avatar if the message doesn't belong to the
 * current user.
 * @param headerContent The content shown at the top of a message list item. By default, we provide
 * [DefaultMessageItemHeaderContent], which shows a list of reactions for the message.
 * @param footerContent The content shown at the bottom of a message list item. By default, we provide
 * [DefaultMessageItemFooterContent], which shows the information like thread participants, upload status, etc.
 * @param trailingContent The content shown at the end of a message list item. By default, we provide
 * [DefaultMessageItemTrailingContent], which adds an extra spacing to the end of the message list item.
 * @param content The content shown at the center of a message list item. By default, we provide
 * [DefaultMessageItemContent], which shows the message bubble with message text and attachments.
 */
// TODO - We should probably pull these out to the MessageList, so that we have multiple items there - systemContent, dateSeparatorContent, messageContent etc
@Composable
public fun DefaultMessageItem(
    messageListItem: MessageListItemState,
    modifier: Modifier = Modifier,
    onLongItemClick: (Message) -> Unit = {},
    onThreadClick: (Message) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit = {},
    onImagePreviewResult: (ImagePreviewResult?) -> Unit = {},
    systemMessageContent: @Composable (SystemMessageState) -> Unit = {
        SystemMessage(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            systemMessageState = it
        )
    },
    leadingContent: @Composable RowScope.(MessageItemState) -> Unit = {
        DefaultMessageItemLeadingContent(
            messageItem = it,
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)
                .size(24.dp)
                .align(Alignment.Bottom)
        )
    },
    headerContent: @Composable ColumnScope.(MessageItemState) -> Unit = {
        DefaultMessageItemHeaderContent(messageItem = it)
    },
    footerContent: @Composable ColumnScope.(MessageItemState) -> Unit = {
        DefaultMessageItemFooterContent(
            messageItem = it,
        )
    },
    trailingContent: @Composable RowScope.(MessageItemState) -> Unit = {
        DefaultMessageItemTrailingContent(
            messageItem = it,
            modifier = Modifier.width(8.dp)
        )
    },
    content: @Composable ColumnScope.(MessageItemState) -> Unit = {
        DefaultMessageItemContent(
            messageItem = it,
            onLongItemClick = onLongItemClick,
            onGiphyActionClick = onGiphyActionClick,
            onImagePreviewResult = onImagePreviewResult,
            modifier = Modifier.widthIn(max = 250.dp)
        )
    },
) {
    when (messageListItem) {
        is DateSeparatorState -> MessageDateSeparator(
            modifier = Modifier.fillMaxWidth(),
            dateSeparator = messageListItem
        )
        is ThreadSeparatorState -> MessageThreadSeparator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = ChatTheme.dimens.threadSeparatorVerticalPadding),
            threadSeparator = messageListItem
        )
        is SystemMessageState -> systemMessageContent(messageListItem)
        is MessageItemState -> DefaultMessageContainer(
            modifier = modifier,
            messageItem = messageListItem,
            onLongItemClick = onLongItemClick,
            onThreadClick = onThreadClick,
            onImagePreviewResult = onImagePreviewResult,
            leadingContent = leadingContent,
            headerContent = headerContent,
            footerContent = footerContent,
            trailingContent = trailingContent,
            content = content,
        )
    }
}

/**
 * The default message container for all messages in the Conversation/Messages screen.
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
 * @param onLongItemClick Handler when the user selects a message, on long tap.
 * @param modifier Modifier for styling.
 * @param onThreadClick Handler for thread clicks, if this message has a thread going.
 * @param onGiphyActionClick Handler when the user taps on an action button in a giphy message item.
 * @param onImagePreviewResult Handler when the user selects an option in the Image Preview screen.
 * @param leadingContent The content shown at the start of a message list item. By default, we provide
 * [DefaultMessageItemLeadingContent], which shows a user avatar if the message doesn't belong to the
 * current user.
 * @param headerContent The content shown at the top of a message list item. By default, we provide
 * [DefaultMessageItemHeaderContent], which shows a list of reactions for the message.
 * @param footerContent The content shown at the bottom of a message list item. By default, we provide
 * [DefaultMessageItemFooterContent], which shows the information like thread participants, upload status, etc.
 * @param trailingContent The content shown at the end of a message list item. By default, we provide
 * [DefaultMessageItemTrailingContent], which adds an extra spacing to the end of the message list item.
 * @param content The content shown at the center of a message list item. By default, we provide
 * [DefaultMessageItemContent], which shows the message bubble with text and attachments.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun DefaultMessageContainer(
    messageItem: MessageItemState,
    onLongItemClick: (Message) -> Unit,
    modifier: Modifier = Modifier,
    onThreadClick: (Message) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit = {},
    onImagePreviewResult: (ImagePreviewResult?) -> Unit = {},
    leadingContent: @Composable RowScope.(MessageItemState) -> Unit = {
        DefaultMessageItemLeadingContent(
            messageItem = it,
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)
                .size(24.dp)
                .align(Alignment.Bottom)
        )
    },
    headerContent: @Composable ColumnScope.(MessageItemState) -> Unit = {
        DefaultMessageItemHeaderContent(messageItem = it)
    },
    footerContent: @Composable ColumnScope.(MessageItemState) -> Unit = {
        DefaultMessageItemFooterContent(
            messageItem = it,
        )
    },
    trailingContent: @Composable RowScope.(MessageItemState) -> Unit = {
        DefaultMessageItemTrailingContent(
            messageItem = it,
            modifier = Modifier.width(8.dp)
        )
    },
    content: @Composable ColumnScope.(MessageItemState) -> Unit = {
        DefaultMessageItemContent(
            messageItem = it,
            onLongItemClick = onLongItemClick,
            onImagePreviewResult = onImagePreviewResult,
            onGiphyActionClick = onGiphyActionClick,
            modifier = Modifier.widthIn(max = 250.dp)
        )
    },
) {
    val (message, _, _, _, focusState) = messageItem

    val clickModifier = if (message.isDeleted()) {
        Modifier
    } else {
        Modifier.combinedClickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {
                if (message.hasThread()) {
                    onThreadClick(message)
                }
            },
            onLongClick = { onLongItemClick(message) }
        )
    }

    val backgroundColor =
        if (focusState is MessageFocused || message.pinned) ChatTheme.colors.highlight else ChatTheme.colors.appBackground
    val shouldAnimateBackground = !message.pinned && focusState != null

    val color = if (shouldAnimateBackground) animateColorAsState(
        targetValue = backgroundColor,
        animationSpec = tween(
            durationMillis = if (focusState is MessageFocused) {
                AnimationConstants.DefaultDurationMillis
            } else {
                HIGHLIGHT_FADE_OUT_DURATION_MILLIS
            }
        )
    ).value else backgroundColor

    val messageAlignment = ChatTheme.messageAlignmentProvider.provideMessageAlignment(messageItem)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(color = color),
        contentAlignment = messageAlignment.itemAlignment
    ) {
        Row(
            modifier
                .widthIn(max = 300.dp)
                .then(clickModifier)
        ) {

            leadingContent(messageItem)

            Column(horizontalAlignment = messageAlignment.contentAlignment) {
                headerContent(messageItem)

                content(messageItem)

                footerContent(messageItem)
            }

            trailingContent(messageItem)
        }
    }
}

/**
 * Represents the default content shown at the start of the message list item.
 *
 * By default, we show a user avatar if the message doesn't belong to the current user.
 *
 * @param messageItem The message item to show the content for.
 * @param modifier Modifier for styling.
 */
@Composable
public fun DefaultMessageItemLeadingContent(
    messageItem: MessageItemState,
    modifier: Modifier = Modifier,
) {
    val position = messageItem.groupPosition
    if (!messageItem.isMine && (position == Bottom || position == None)) {
        UserAvatar(
            modifier = modifier,
            user = messageItem.message.user,
            showOnlineIndicator = false
        )
    } else {
        Spacer(modifier = modifier)
    }
}

/**
 * Represents the default content shown at the top of the message list item.
 *
 * By default, we show if the message is pinned and a list of reactions for the message.
 *
 * @param messageItem The message item to show the content for.
 */
@Composable
public fun DefaultMessageItemHeaderContent(messageItem: MessageItemState) {
    val message = messageItem.message
    val currentUser = messageItem.currentUser

    if (message.pinned) {
        val pinnedByUser = if (message.pinnedBy?.id == currentUser?.id) {
            stringResource(id = R.string.stream_compose_message_list_you)
        } else {
            message.pinnedBy?.name
        }

        val pinnedByText = if (pinnedByUser != null) {
            stringResource(id = R.string.stream_compose_pinned_to_channel_by, pinnedByUser)
        } else null

        MessageHeaderLabel(
            painter = painterResource(id = R.drawable.stream_compose_ic_message_pinned),
            text = pinnedByText
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
            text = stringResource(alsoSendToChannelTextRes)
        )
    }

    if (!message.isDeleted()) {
        val ownReactions = message.ownReactions
        val supportedReactions = ChatTheme.reactionTypes

        val reactionCounts = message.reactionCounts.ifEmpty { return }
        reactionCounts
            .filter { supportedReactions.containsKey(it.key) }
            .takeIf { it.isNotEmpty() }
            ?.map { it.key }
            ?.map { type ->
                ReactionOptionItemState(
                    painter = painterResource(requireNotNull(supportedReactions[type])),
                    isSelected = ownReactions.any { it.type == type },
                    type = type
                )
            }
            ?.let { options ->
                MessageReactions(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    options = options
                )
            }
    }
}

/**
 * Represents the default content shown at the bottom of the message list item.
 *
 * By default, the following can be shown in the footer:
 * - uploading status
 * - thread participants
 * - message timestamp
 *
 * @param messageItem The message item to show the content for.
 * @param modifier Modifier for styling.
 */
@Composable
public fun ColumnScope.DefaultMessageItemFooterContent(
    messageItem: MessageItemState,
    modifier: Modifier = Modifier,
) {
    val message = messageItem.message
    when {
        message.isUploading() -> {
            UploadingFooter(
                modifier = modifier.align(End),
                message = message
            )
        }
        message.isDeleted() && messageItem.isMine -> {
            OwnedMessageVisibilityContent(
                modifier = modifier,
                message = message
            )
        }
        !message.isDeleted() -> {
            MessageFooter(
                messageItem = messageItem,
                modifier = modifier
            )
        }
    }

    val position = messageItem.groupPosition
    val spacerSize = if (position == None || position == Bottom) 4.dp else 2.dp

    Spacer(Modifier.size(spacerSize))
}

/**
 * Represents the default content shown at the end of the message list item.
 *
 * By default, we show an extra spacing at the end of the message list item.
 *
 * @param messageItem The message item to show the content for.
 * @param modifier Modifier for styling.
 */
@Composable
public fun DefaultMessageItemTrailingContent(
    messageItem: MessageItemState,
    modifier: Modifier = Modifier,
) {
    if (messageItem.isMine) {
        Spacer(modifier = modifier)
    }
}

/**
 * Represents the default content shown at the center of the message list item.
 *
 * By default, we show a message bubble with attachments.
 *
 * @param messageItem The message item to show the content for.
 * @param modifier Modifier for styling.
 * @param onLongItemClick Handler when the user selects a message, on long tap.
 * @param onGiphyActionClick Handler when the user taps on an action button in a giphy message item.
 * @param onImagePreviewResult Handler when the user selects an option in the Image Preview screen.
 */
@Composable
public fun DefaultMessageItemContent(
    messageItem: MessageItemState,
    modifier: Modifier = Modifier,
    onLongItemClick: (Message) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit = {},
    onImagePreviewResult: (ImagePreviewResult?) -> Unit = {},
) {
    val (message, position, _, ownsMessage, _) = messageItem

    val messageBubbleShape = when (position) {
        Top, Middle -> RoundedCornerShape(16.dp)
        else -> {
            if (ownsMessage) ChatTheme.shapes.myMessageBubble else ChatTheme.shapes.otherMessageBubble
        }
    }

    val messageBubbleColor = when {
        message.isGiphyEphemeral() -> ChatTheme.colors.giphyMessageBackground
        message.isDeleted() -> ChatTheme.colors.deletedMessagesBackground
        ownsMessage -> ChatTheme.colors.ownMessagesBackground
        else -> ChatTheme.colors.otherMessagesBackground
    }

    MessageBubble(
        modifier = modifier,
        shape = messageBubbleShape,
        color = messageBubbleColor,
        content = {
            when {
                message.isGiphyEphemeral() -> {
                    GiphyMessageContent(
                        message = messageItem.message,
                        onGiphyActionClick = onGiphyActionClick
                    )
                }
                message.isDeleted() -> {
                    Text(
                        modifier = modifier
                            .padding(
                                start = 12.dp,
                                end = 12.dp,
                                top = 8.dp,
                                bottom = 8.dp
                            ),
                        text = stringResource(id = R.string.stream_compose_message_deleted),
                        color = ChatTheme.colors.textLowEmphasis,
                        style = ChatTheme.typography.footnoteItalic
                    )
                }
                else -> {
                    Column {
                        MessageAttachmentsContent(
                            message = messageItem.message,
                            onLongItemClick = onLongItemClick,
                            onImagePreviewResult = onImagePreviewResult,
                        )

                        if (message.text.isNotEmpty()) {
                            DefaultMessageContent(message = message)
                        }
                    }
                }
            }
        }
    )
}
