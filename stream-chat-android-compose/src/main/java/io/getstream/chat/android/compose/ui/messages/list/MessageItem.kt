package io.getstream.chat.android.compose.ui.messages.list

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Alignment.Companion.Start
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.util.PatternsCompat
import coil.compose.rememberImagePainter
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResult
import io.getstream.chat.android.compose.state.messages.items.DateSeparator
import io.getstream.chat.android.compose.state.messages.items.MessageItem
import io.getstream.chat.android.compose.state.messages.items.MessageItemGroupPosition.Bottom
import io.getstream.chat.android.compose.state.messages.items.MessageItemGroupPosition.Middle
import io.getstream.chat.android.compose.state.messages.items.MessageItemGroupPosition.None
import io.getstream.chat.android.compose.state.messages.items.MessageItemGroupPosition.Top
import io.getstream.chat.android.compose.state.messages.items.MessageListItem
import io.getstream.chat.android.compose.ui.attachments.content.MessageAttachmentsContent
import io.getstream.chat.android.compose.ui.common.MessageBubble
import io.getstream.chat.android.compose.ui.common.Timestamp
import io.getstream.chat.android.compose.ui.common.avatar.Avatar
import io.getstream.chat.android.compose.ui.common.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.hasThread
import io.getstream.chat.android.compose.ui.util.isDeleted
import io.getstream.chat.android.compose.ui.util.isUploading
import java.util.Date

/**
 * Represents the time the highlight fade out transition will take.
 */
public const val HIGHLIGHT_FADE_OUT_DURATION_MILLIS: Int = 1000

/**
 * Represents the default message item that's shown for each item in the list.
 *
 * Detects if we're dealing with a [DateSeparator] or a [MessageItem] and shows the required UI.
 *
 * @param messageListItem The item that holds the data.
 * @param modifier Modifier for styling.
 * @param onLongItemClick Handler when the user long taps on an item.
 * @param onThreadClick Handler when the user taps on a thread in a message item.
 * @param onImagePreviewResult Handler when the user receives a result from previewing message attachments.
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
    messageListItem: MessageListItem,
    modifier: Modifier = Modifier,
    onLongItemClick: (Message) -> Unit = {},
    onThreadClick: (Message) -> Unit = {},
    onImagePreviewResult: (ImagePreviewResult?) -> Unit = {},
    leadingContent: @Composable RowScope.(MessageItem) -> Unit = {
        DefaultMessageItemLeadingContent(
            messageItem = it,
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)
                .size(24.dp)
                .align(Alignment.Bottom)
        )
    },
    headerContent: @Composable ColumnScope.(MessageItem) -> Unit = {
        DefaultMessageItemHeaderContent(
            messageItem = it,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 2.dp)
        )
    },
    footerContent: @Composable ColumnScope.(MessageItem) -> Unit = {
        DefaultMessageItemFooterContent(
            messageItem = it,
        )
    },
    trailingContent: @Composable RowScope.(MessageItem) -> Unit = {
        DefaultMessageItemTrailingContent(
            messageItem = it,
            modifier = Modifier.width(8.dp)
        )
    },
    content: @Composable ColumnScope.(MessageItem) -> Unit = {
        DefaultMessageItemContent(
            messageItem = it,
            onLongItemClick = onLongItemClick,
            onImagePreviewResult = onImagePreviewResult,
            modifier = Modifier.widthIn(max = 250.dp)
        )
    },
) {
    when (messageListItem) {
        is DateSeparator -> MessageDateSeparator(messageListItem)
        is MessageItem -> DefaultMessageContainer(
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
 * Represents a date separator item that shows whenever messages are too far apart in time.
 *
 * @param dateSeparator The data used to show the separator text.
 */
@Composable
public fun MessageDateSeparator(
    dateSeparator: DateSeparator,
) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Center) {
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
    messageItem: MessageItem,
    onLongItemClick: (Message) -> Unit,
    modifier: Modifier = Modifier,
    onThreadClick: (Message) -> Unit = {},
    onImagePreviewResult: (ImagePreviewResult?) -> Unit = {},
    leadingContent: @Composable RowScope.(MessageItem) -> Unit = {
        DefaultMessageItemLeadingContent(
            messageItem = it,
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)
                .size(24.dp)
                .align(Alignment.Bottom)
        )
    },
    headerContent: @Composable ColumnScope.(MessageItem) -> Unit = {
        DefaultMessageItemHeaderContent(
            messageItem = it,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 2.dp)
        )
    },
    footerContent: @Composable ColumnScope.(MessageItem) -> Unit = {
        DefaultMessageItemFooterContent(
            messageItem = it,
        )
    },
    trailingContent: @Composable RowScope.(MessageItem) -> Unit = {
        DefaultMessageItemTrailingContent(
            messageItem = it,
            modifier = Modifier.width(8.dp)
        )
    },
    content: @Composable ColumnScope.(MessageItem) -> Unit = {
        DefaultMessageItemContent(
            messageItem = it,
            onLongItemClick = onLongItemClick,
            onImagePreviewResult = onImagePreviewResult,
            modifier = Modifier.widthIn(max = 250.dp)
        )
    },
) {
    val (message, _, _, ownsMessage, isFocused) = messageItem

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

    val backgroundColor by animateColorAsState(
        targetValue = if (isFocused) ChatTheme.colors.highlight else ChatTheme.colors.appBackground,
        animationSpec = tween(
            durationMillis = if (isFocused) {
                AnimationConstants.DefaultDurationMillis
            } else {
                HIGHLIGHT_FADE_OUT_DURATION_MILLIS
            }
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(color = backgroundColor),
        contentAlignment = if (ownsMessage) CenterEnd else CenterStart
    ) {
        Row(
            modifier
                .widthIn(max = 300.dp)
                .then(clickModifier)
        ) {

            leadingContent(messageItem)

            Column(horizontalAlignment = if (ownsMessage) End else Start) {
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
    messageItem: MessageItem,
    modifier: Modifier = Modifier,
) {
    if (!messageItem.isMine) {
        val position = messageItem.groupPosition
        if (position == Bottom || position == None) {
            UserAvatar(
                modifier = modifier,
                user = messageItem.message.user,
                showOnlineIndicator = false
            )
        } else {
            Spacer(modifier = modifier)
        }
    }
}

/**
 * Represents the default content shown at the top of the message list item.
 *
 * By default, we show a list of reactions for the message.
 *
 * @param messageItem The message item to show the content for.
 * @param modifier Modifier for styling.
 */
@Composable
public fun DefaultMessageItemHeaderContent(
    messageItem: MessageItem,
    modifier: Modifier,
) {
    val message = messageItem.message
    val ownReactions = message.ownReactions
    val supportedReactions = ChatTheme.reactionTypes

    if (!message.isDeleted()) {
        val reactions = message.reactionCounts
            .map { it.key }
            .filter { supportedReactions[it] != null }
            .map { type -> requireNotNull(supportedReactions[type]) to (type in ownReactions.map { it.type }) }

        if (reactions.isNotEmpty()) {
            MessageReactions(
                modifier = modifier,
                reactions = reactions
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
    messageItem: MessageItem,
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
    messageItem: MessageItem,
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
 */
@Composable
public fun DefaultMessageItemContent(
    messageItem: MessageItem,
    modifier: Modifier = Modifier,
    onLongItemClick: (Message) -> Unit = {},
    onImagePreviewResult: (ImagePreviewResult?) -> Unit = {},
) {
    val (message, position, parentMessageId, ownsMessage, _) = messageItem

    val bubbleShape = if (message.id == parentMessageId) {
        ChatTheme.shapes.myMessageBubble
    } else {
        when (position) {
            Top, Middle -> RoundedCornerShape(16.dp)
            else -> {
                if (ownsMessage) ChatTheme.shapes.myMessageBubble else ChatTheme.shapes.otherMessageBubble
            }
        }
    }

    val messageCardColor = if (ownsMessage) ChatTheme.colors.borders else ChatTheme.colors.barsBackground

    MessageBubble(
        modifier = modifier,
        shape = bubbleShape,
        color = messageCardColor,
        content = {
            if (message.isDeleted()) {
                DeletedMessageContent()
            } else {
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
    )
}

/**
 * Container for all the reactions this message has.
 *
 * @param modifier Modifier for styling.
 * @param reactions Map of reactions and their count.
 */
@Composable
private fun MessageReactions(
    reactions: List<Pair<Int, Boolean>>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(shape = RoundedCornerShape(16.dp), color = ChatTheme.colors.barsBackground)
            .padding(4.dp),
        verticalAlignment = CenterVertically
    ) {
        for ((icon, ownReaction) in reactions) {
            Icon(
                modifier = Modifier
                    .size(20.dp)
                    .padding(2.dp)
                    .align(CenterVertically),
                painter = painterResource(icon),
                tint = if (ownReaction) ChatTheme.colors.primaryAccent else ChatTheme.colors.textLowEmphasis,
                contentDescription = null
            )
        }
    }
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
    messageItem: MessageItem,
    modifier: Modifier = Modifier,
) {
    val (message, position) = messageItem
    val hasThread = message.threadParticipants.isNotEmpty()

    if (hasThread) {
        ThreadParticipants(
            modifier = modifier,
            participants = message.threadParticipants,
            text = stringResource(id = R.string.stream_compose_thread_footnote)
        )
    } else if (!hasThread && (position == Bottom || position == None)) {
        Row(
            modifier = modifier.padding(top = 4.dp),
            verticalAlignment = CenterVertically
        ) {
            Text(
                modifier = Modifier.padding(end = 8.dp),
                text = message.user.name,
                style = ChatTheme.typography.footnote,
                color = ChatTheme.colors.textLowEmphasis
            )

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
