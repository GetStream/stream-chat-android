package io.getstream.chat.android.compose.ui.messages.list

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.util.PatternsCompat
import coil.compose.rememberImagePainter
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.previewdata.PreviewReactionData
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResult
import io.getstream.chat.android.compose.state.messages.list.CancelGiphy
import io.getstream.chat.android.compose.state.messages.list.DateSeparatorState
import io.getstream.chat.android.compose.state.messages.list.GiphyAction
import io.getstream.chat.android.compose.state.messages.list.MessageFocused
import io.getstream.chat.android.compose.state.messages.list.MessageItemGroupPosition.Bottom
import io.getstream.chat.android.compose.state.messages.list.MessageItemGroupPosition.Middle
import io.getstream.chat.android.compose.state.messages.list.MessageItemGroupPosition.None
import io.getstream.chat.android.compose.state.messages.list.MessageItemGroupPosition.Top
import io.getstream.chat.android.compose.state.messages.list.MessageItemState
import io.getstream.chat.android.compose.state.messages.list.MessageListItemState
import io.getstream.chat.android.compose.state.messages.list.SendGiphy
import io.getstream.chat.android.compose.state.messages.list.ShuffleGiphy
import io.getstream.chat.android.compose.state.messages.list.SystemMessageState
import io.getstream.chat.android.compose.state.messages.list.ThreadSeparatorState
import io.getstream.chat.android.compose.state.messages.reaction.ReactionOptionItemState
import io.getstream.chat.android.compose.ui.attachments.content.MessageAttachmentsContent
import io.getstream.chat.android.compose.ui.common.MessageBubble
import io.getstream.chat.android.compose.ui.common.Timestamp
import io.getstream.chat.android.compose.ui.common.avatar.Avatar
import io.getstream.chat.android.compose.ui.common.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.hasThread
import io.getstream.chat.android.compose.ui.util.isDeleted
import io.getstream.chat.android.compose.ui.util.isGiphyEphemeral
import io.getstream.chat.android.compose.ui.util.isUploading
import java.util.Date

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
 * A system message is a message generated by a system event, such as updating the channel or muting a user.
 *
 * @param systemMessageState The system message item to show.
 * @param modifier Modifier for styling.
 */
@Composable
public fun SystemMessage(
    systemMessageState: SystemMessageState,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = systemMessageState.message.text,
        color = ChatTheme.colors.textLowEmphasis,
        style = ChatTheme.typography.footnoteBold,
        textAlign = TextAlign.Center
    )
}

/**
 * Represents a date separator item that shows whenever messages are too far apart in time.
 *
 * @param dateSeparator The data used to show the separator text.
 * @param modifier Modifier for styling.
 */
@Composable
public fun MessageDateSeparator(
    dateSeparator: DateSeparatorState,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Center) {
        Surface(
            modifier = Modifier
                .padding(vertical = 8.dp),
            color = ChatTheme.colors.overlayDark,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                modifier = Modifier.padding(vertical = 2.dp, horizontal = 16.dp),
                text = DateUtils.getRelativeTimeSpanString(
                    dateSeparator.date.time,
                    System.currentTimeMillis(),
                    DateUtils.DAY_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
                ).toString(),
                color = ChatTheme.colors.barsBackground,
                style = ChatTheme.typography.body
            )
        }
    }
}

/**
 * Represents a thread separator item that is displayed in thread mode to separate a parent message
 * from thread replies.
 *
 * @param threadSeparator The data used to show the separator text.
 * @param modifier Modifier for styling.
 */
@Composable
public fun MessageThreadSeparator(
    threadSeparator: ThreadSeparatorState,
    modifier: Modifier = Modifier,
) {
    val backgroundGradient = Brush.verticalGradient(
        listOf(
            ChatTheme.colors.threadSeparatorGradientStart,
            ChatTheme.colors.threadSeparatorGradientEnd
        )
    )
    val replyCount = threadSeparator.replyCount

    Box(
        modifier = modifier
            .background(brush = backgroundGradient),
        contentAlignment = Center
    ) {
        Text(
            modifier = Modifier.padding(vertical = ChatTheme.dimens.threadSeparatorTextVerticalPadding),
            text = LocalContext.current.resources.getQuantityString(
                R.plurals.stream_compose_message_list_thread_separator,
                replyCount,
                replyCount
            ),
            color = ChatTheme.colors.textLowEmphasis,
            style = ChatTheme.typography.body
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
        val showInChannelTextRes = if (messageItem.isInThread) {
            R.string.stream_compose_also_sent_to_channel
        } else {
            R.string.stream_compose_replied_to_thread
        }

        MessageHeaderLabel(
            painter = painterResource(id = R.drawable.stream_compose_ic_thread_reply),
            text = stringResource(showInChannelTextRes)
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
 * Represents a meta information about the message that is shown above the message bubble.
 *
 * @param painter The icon to be shown.
 * @param text The text to be shown.
 * @param modifier Modifier for styling.
 * @param contentPadding The inner padding inside the component.
 */
@Composable
private fun MessageHeaderLabel(
    painter: Painter,
    modifier: Modifier = Modifier,
    text: String? = null,
    contentPadding: PaddingValues = PaddingValues(vertical = 2.dp, horizontal = 4.dp),
) {
    Row(
        modifier = modifier.padding(contentPadding),
        verticalAlignment = CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .padding(end = 2.dp)
                .size(14.dp),
            painter = painter,
            contentDescription = null,
            tint = ChatTheme.colors.textLowEmphasis
        )

        if (text != null) {
            Text(
                text = text,
                style = ChatTheme.typography.footnote,
                color = ChatTheme.colors.textLowEmphasis
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
                message.isDeleted() -> DeletedMessageContent()
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

/**
 * Represents a reaction bubble with a list of reactions this message has.
 *
 * @param options The list of reactions to display.
 * @param modifier Modifier for styling.
 */
@Composable
public fun MessageReactions(
    options: List<ReactionOptionItemState>,
    modifier: Modifier = Modifier,
    itemContent: @Composable RowScope.(ReactionOptionItemState) -> Unit = { option ->
        MessageReactionsItem(
            modifier = Modifier
                .size(20.dp)
                .padding(2.dp)
                .align(CenterVertically),
            option = option
        )
    },
) {
    Row(
        modifier = modifier
            .background(shape = RoundedCornerShape(16.dp), color = ChatTheme.colors.barsBackground)
            .padding(4.dp),
        verticalAlignment = CenterVertically
    ) {
        options.forEach { option ->
            itemContent(option)
        }
    }
}

/**
 * Represents a reaction item in the reaction bubble.
 *
 * @param option The reaction to display.
 * @param modifier Modifier for styling.
 */
@Composable
public fun MessageReactionsItem(
    option: ReactionOptionItemState,
    modifier: Modifier = Modifier,
) {
    Icon(
        modifier = modifier,
        painter = option.painter,
        tint = if (option.isSelected) ChatTheme.colors.primaryAccent else ChatTheme.colors.textLowEmphasis,
        contentDescription = null
    )
}

/**
 * The default text message content. It holds the quoted message in case there is one.
 *
 * @param message The message to show.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun DefaultMessageContent(
    message: Message,
    modifier: Modifier = Modifier,
) {
    val quotedMessage = message.replyTo

    Column(
        modifier = modifier
    ) {
        if (quotedMessage != null) {
            QuotedMessage(
                modifier = Modifier.padding(8.dp),
                message = quotedMessage
            )
        }
        MessageText(message = message)
    }
}

/**
 * Default text element for messages, with extra styling and padding for the chat bubble.
 *
 * It detects if we have any annotations/links in the message, and if so, it uses the [ClickableText]
 * component to allow for clicks on said links, that will open the link.
 *
 * Alternatively, it just shows a basic [Text] element.
 *
 * @param message Message to show.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun MessageText(
    message: Message,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val styledText = buildAnnotatedMessageText(message)
    val annotations = styledText.getStringAnnotations(0, styledText.lastIndex)

    if (annotations.isNotEmpty()) {
        ClickableText(
            modifier = modifier
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = 8.dp,
                    bottom = 8.dp
                ),
            text = styledText,
            style = ChatTheme.typography.bodyBold
        ) { position ->
            val targetUrl = annotations.firstOrNull {
                position in it.start..it.end
            }?.item

            if (targetUrl != null && targetUrl.isNotEmpty()) {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(targetUrl)
                    )
                )
            }
        }
    } else {
        Text(
            modifier = modifier
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = 8.dp,
                    bottom = 8.dp
                ),
            text = styledText,
            style = ChatTheme.typography.bodyBold
        )
    }
}

/**
 * Takes the given message and builds an annotated message text that shows links and allows for clicks,
 * if there are any links available.
 *
 * @param message The message to extract the text from and style.
 *
 * @return The annotated String, with clickable links, if applicable.
 */
@Composable
private fun buildAnnotatedMessageText(message: Message): AnnotatedString {
    val text = message.text

    return buildAnnotatedString {
        // First we add the whole text to the [AnnotatedString] and style it as a regular text.
        append(text)
        addStyle(
            SpanStyle(
                fontStyle = ChatTheme.typography.body.fontStyle,
                color = ChatTheme.colors.textHighEmphasis
            ),
            start = 0,
            end = text.length
        )

        // Then for each available link in the text, we add a different style, to represent the links,
        // as well as add a String annotation to it. This gives us the ability to open the URL on click.
        @SuppressLint("RestrictedApi")
        val matcher = PatternsCompat.AUTOLINK_WEB_URL.matcher(text)
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()

            addStyle(
                style = SpanStyle(
                    color = ChatTheme.colors.primaryAccent,
                    textDecoration = TextDecoration.Underline,
                ),
                start = start,
                end = end,
            )

            val linkText = requireNotNull(matcher.group(0)!!)

            // Add "http://" prefix if link has no scheme in it
            val url = if (URL_SCHEMES.none { scheme -> linkText.startsWith(scheme) }) {
                URL_SCHEMES[0] + linkText
            } else {
                linkText
            }

            addStringAnnotation(
                tag = "URL",
                annotation = url,
                start = start,
                end = end,
            )
        }
    }
}

private val URL_SCHEMES = listOf("http://", "https://")

/**
 * Shows a row of participants in the message thread, if they exist.
 *
 * @param participants List of users in the thread.
 * @param modifier Modifier for styling.
 * @param text Text of the label.
 */
@Composable
public fun ThreadParticipants(
    participants: List<User>,
    modifier: Modifier = Modifier,
    text: String,
) {
    Row(modifier = modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp)) {
        Text(
            modifier = Modifier.padding(end = 4.dp),
            text = text,
            style = ChatTheme.typography.footnoteBold,
            color = ChatTheme.colors.primaryAccent
        )

        for (user in participants) {
            val painter = rememberImagePainter(data = user.image)
            Avatar(
                modifier = Modifier
                    .padding(2.dp)
                    .size(16.dp),
                painter = painter
            )
        }
    }
}

/**
 * Wraps the quoted message into a special component, that doesn't show some information, like
 * the timestamp, thread participants and similar.
 *
 * @param message Message to show.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun QuotedMessage(
    message: Message,
    modifier: Modifier = Modifier,
) {
    val painter = rememberImagePainter(data = message.user.image)

    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        Avatar(
            modifier = Modifier
                .size(24.dp),
            painter = painter
        )

        Spacer(modifier = Modifier.size(8.dp))

        MessageBubble(
            shape = ChatTheme.shapes.otherMessageBubble, color = ChatTheme.colors.barsBackground,
            content = {
                Column {
                    MessageAttachmentsContent(
                        message = message,
                        onLongItemClick = {}
                    )

                    if (message.text.isNotEmpty()) {
                        MessageText(message = message)
                    }
                }
            }
        )
    }
}

/**
 * Component that shows that the message has been (soft) deleted.
 *
 * @param modifier Modifier for styling.
 */
@Composable
internal fun DeletedMessageContent(
    modifier: Modifier = Modifier,
) {
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

/**
 * Represents the content of an ephemeral giphy message.
 *
 * @param message The ephemeral giphy message.
 * @param modifier Modifier for styling.
 * @param onGiphyActionClick Handler when the user clicks on action button.
 */
@Composable
internal fun GiphyMessageContent(
    message: Message,
    modifier: Modifier = Modifier,
    onGiphyActionClick: (GiphyAction) -> Unit = {},
) {
    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = CenterVertically
        ) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = R.drawable.stream_compose_ic_giphy),
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(id = R.string.stream_compose_message_list_giphy_title),
                style = ChatTheme.typography.bodyBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = ChatTheme.colors.textHighEmphasis,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = message.text,
                style = ChatTheme.typography.body,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = ChatTheme.colors.textLowEmphasis,
            )
        }

        MessageAttachmentsContent(
            message = message,
            onLongItemClick = { },
            onImagePreviewResult = { },
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(color = ChatTheme.colors.borders)
        )

        Row(
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth(),
            verticalAlignment = CenterVertically
        ) {
            GiphyButton(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                text = stringResource(id = R.string.stream_compose_message_list_giphy_cancel),
                textColor = ChatTheme.colors.textLowEmphasis,
                onClick = { onGiphyActionClick(CancelGiphy(message)) }
            )

            Spacer(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(color = ChatTheme.colors.borders)
            )

            GiphyButton(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                text = stringResource(id = R.string.stream_compose_message_list_giphy_shuffle),
                textColor = ChatTheme.colors.textLowEmphasis,
                onClick = { onGiphyActionClick(ShuffleGiphy(message)) }
            )

            Spacer(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(color = ChatTheme.colors.borders)
            )

            GiphyButton(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                text = stringResource(id = R.string.stream_compose_message_list_giphy_send),
                textColor = ChatTheme.colors.primaryAccent,
                onClick = { onGiphyActionClick(SendGiphy(message)) }
            )
        }
    }
}

/**
 * Represents an action button in the ephemeral giphy message.
 *
 * @param text The text displayed on the button.
 * @param textColor The color applied to the text.
 * @param onClick Handler when the user clicks on action button.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun GiphyButton(
    text: String,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clickable(
                onClick = onClick,
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        Text(
            modifier = Modifier.align(Center),
            text = text,
            style = ChatTheme.typography.bodyBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * A footer indicating the current upload progress - how many items have been uploaded and what the total number of items
 * is.
 *
 * @param message The message to show the content of.
 * @param modifier Modifier for styling.
 */
@Composable
public fun UploadingFooter(
    message: Message,
    modifier: Modifier = Modifier,
) {
    val uploadedCount = message.attachments.count { it.uploadState is Attachment.UploadState.Success }
    val totalCount = message.attachments.size

    Column(
        modifier = modifier,
        horizontalAlignment = End
    ) {
        OwnedMessageVisibilityContent(message = message)

        Text(
            text = stringResource(id = R.string.stream_compose_upload_file_count, uploadedCount + 1, totalCount),
            style = ChatTheme.typography.body,
            textAlign = TextAlign.End
        )
    }
}

/**
 * Default message footer, which contains either [ThreadParticipants] or the default footer, which
 * holds the sender name and the timestamp.
 *
 * @param messageItem Message to show.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun MessageFooter(
    messageItem: MessageItemState,
    modifier: Modifier = Modifier,
) {
    val (message, position) = messageItem
    val hasThread = message.threadParticipants.isNotEmpty()

    if (hasThread && !messageItem.isInThread) {
        val replyCount = message.replyCount
        ThreadParticipants(
            modifier = modifier,
            participants = message.threadParticipants,
            text = LocalContext.current.resources.getQuantityString(
                R.plurals.stream_compose_message_list_thread_footnote,
                replyCount,
                replyCount
            )
        )
    } else if (!hasThread && (position == Bottom || position == None)) {
        Row(
            modifier = modifier.padding(top = 4.dp),
            verticalAlignment = CenterVertically
        ) {
            if (!messageItem.isMine) {
                Text(
                    modifier = Modifier.padding(end = 8.dp),
                    text = message.user.name,
                    style = ChatTheme.typography.footnote,
                    color = ChatTheme.colors.textLowEmphasis
                )
            }

            Timestamp(date = message.updatedAt ?: message.createdAt)
        }
    }
}

/**
 * Shows the content that lets the user know that only they can see the message.
 *
 * @param message Message to show.
 * @param modifier Modifier for styling.
 */
@Composable
private fun OwnedMessageVisibilityContent(
    message: Message,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp), verticalAlignment = CenterVertically) {
        Icon(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(12.dp),
            imageVector = Icons.Default.RemoveRedEye,
            contentDescription = null
        )

        Text(
            text = stringResource(id = R.string.stream_compose_only_visible_to_you),
            style = ChatTheme.typography.footnote,
            color = ChatTheme.colors.textHighEmphasis
        )

        Timestamp(
            modifier = Modifier.padding(8.dp),
            date = message.updatedAt ?: message.createdAt ?: Date()
        )
    }
}

/**
 * Represents the horizontal alignment of messages in the message list.
 *
 * @param itemAlignment The alignment of the message item.
 * @param contentAlignment The alignment of the inner content.
 */
public enum class MessageAlignment(
    public val itemAlignment: Alignment,
    public val contentAlignment: Alignment.Horizontal,
) {
    /**
     * Represents the alignment at the start of the screen, by default for other people's messages.
     */
    Start(CenterStart, Alignment.Start),

    /**
     * Represents the alignment at the end of the screen, by default for owned messages.
     */
    End(CenterEnd, Alignment.End),
}

@Preview
@Composable
private fun OneMessageReactionPreview() {
    ChatTheme {
        MessageReactions(options = PreviewReactionData.oneReaction())
    }
}

@Preview
@Composable
private fun ManyMessageReactionsPreview() {
    ChatTheme {
        MessageReactions(options = PreviewReactionData.manyReactions())
    }
}
