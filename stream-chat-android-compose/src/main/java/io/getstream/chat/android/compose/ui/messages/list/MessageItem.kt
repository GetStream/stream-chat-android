package io.getstream.chat.android.compose.ui.messages.list

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.util.PatternsCompat
import coil.compose.rememberImagePainter
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.image
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.state.messages.items.Bottom
import io.getstream.chat.android.compose.state.messages.items.MessageItem
import io.getstream.chat.android.compose.state.messages.items.MessageItemGroupPosition
import io.getstream.chat.android.compose.state.messages.items.Middle
import io.getstream.chat.android.compose.state.messages.items.None
import io.getstream.chat.android.compose.state.messages.items.Top
import io.getstream.chat.android.compose.ui.common.MessageBubble
import io.getstream.chat.android.compose.ui.common.avatar.Avatar
import io.getstream.chat.android.compose.ui.common.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import java.text.SimpleDateFormat
import java.util.Date

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
 * @param messageItem - The message item to show, which holds the message and the group position, if the message is in
 * a group of messages from the same user.
 * @param onLongItemClick - Handler when the user selects a message, on long tap.
 * @param modifier - Modifier for styling.
 * @param onThreadClick - Handler for thread clicks, if this message has a thread going.
 * */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun DefaultMessageContainer(
    messageItem: MessageItem,
    onLongItemClick: (Message) -> Unit,
    modifier: Modifier = Modifier,
    onThreadClick: (Message) -> Unit = {},
) {
    val (message, position, parentMessageId) = messageItem

    val attachmentFactory = ChatTheme.attachmentFactories.firstOrNull { it.canHandle(message) }
    val isDeleted = message.deletedAt != null
    val hasThread = message.threadParticipants.isNotEmpty()
    val ownsMessage = messageItem.isMine

    val messageCardColor =
        if (ownsMessage) ChatTheme.colors.borders else ChatTheme.colors.barsBackground

    val clickModifier = if (isDeleted) {
        Modifier
    } else {
        Modifier.combinedClickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {
                if (hasThread) {
                    onThreadClick(message)
                }
            },
            onLongClick = { onLongItemClick(message) }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = if (ownsMessage) CenterEnd else CenterStart
    ) {
        Row(
            modifier
                .widthIn(max = 300.dp)
                .then(clickModifier)
        ) {

            if (!ownsMessage) {
                MessageAvatar(position, message.user)
            }

            Column(horizontalAlignment = if (ownsMessage) End else Start) {
                val ownReactions = message.ownReactions
                val supportedReactions = ChatTheme.reactionTypes

                if (!isDeleted) {
                    // reactions
                    val reactions = message.reactionCounts
                        .map { it.key }
                        .filter { supportedReactions[it] != null }
                        .map { type -> requireNotNull(supportedReactions[type]) to (type in ownReactions.map { it.type }) }

                    if (reactions.isNotEmpty()) {
                        MessageReactions(
                            modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 2.dp),
                            reactions = reactions
                        )
                    }
                }

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

                // content
                MessageBubble(
                    modifier = Modifier.widthIn(max = 250.dp),
                    shape = bubbleShape,
                    color = messageCardColor,
                    content = {
                        if (message.deletedAt != null) {
                            DeletedMessageContent()
                        } else {
                            Column {
                                attachmentFactory?.Content(
                                    AttachmentState(
                                        modifier = Modifier.padding(4.dp),
                                        message = messageItem,
                                        onLongItemClick = onLongItemClick
                                    )
                                )

                                if (message.text.isNotEmpty()) {
                                    DefaultMessageContent(message = message)
                                }
                            }
                        }
                    }
                )

                if (isDeleted && ownsMessage) {
                    DeletedMessageFooter(
                        modifier = Modifier,
                        message = message
                    )
                } else if (!isDeleted) {
                    MessageFooter(messageItem)
                }
            }

            if (ownsMessage) {
                MessageAvatar(position, message.user)
            }
        }
    }
}

/**
 * Represents the section of each message where the [Avatar] resides. In case we don't need to show an avatar, we instead
 * add a spacer, to keep messages aligned the same way.
 *
 * @param position - Position of the message in a group. This determines if we should show the user image or a spacer.
 * @param user - The user that owns the message.
 * */
@Composable
private fun RowScope.MessageAvatar(
    position: MessageItemGroupPosition,
    user: User,
) {
    if (position == Bottom || position == None) {
        UserAvatar(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)
                .size(24.dp)
                .align(Alignment.Bottom),
            user = user
        )
    } else {
        Spacer(modifier = Modifier.width(40.dp))
    }
}

/**
 * Container for all the reactions this message has.
 *
 * @param modifier - Modifier for styling.
 * @param reactions - Map of reactions and their count.
 * */
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
 * @param message - The message to show.
 * @param modifier - Modifier for styling.
 * */
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
 * @param message - Message to show.
 * @param modifier - Modifier for styling.
 * */
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
 * @param message - The message to extract the text from and style.
 *
 * @return - The annotated String, with clickable links, if applicable.
 * */
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
 * @param participants - List of users in the thread.
 * @param modifier - Modifier for styling.
 * */
@Composable
private fun ThreadParticipants(
    participants: List<User>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(start = 4.dp, end = 4.dp, top = 4.dp)
    ) {

        Text(
            modifier = Modifier.padding(end = 4.dp),
            text = stringResource(id = R.string.stream_compose_thread_footnote),
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
 * @param message - Message to show.
 * @param modifier - Modifier for styling.
 * */
@Composable
internal fun QuotedMessage(
    message: Message,
    modifier: Modifier = Modifier,
) {
    val painter = rememberImagePainter(data = message.user.image)
    val factory = ChatTheme.attachmentFactories.firstOrNull { it.canHandle(message) }

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
                    factory?.Content(
                        AttachmentState(
                            modifier = Modifier.padding(4.dp),
                            message = MessageItem(message, None),
                            onLongItemClick = { }
                        )
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
 * @param modifier - Modifier for styling.
 * */
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
 * Default message footer, which contains either [ThreadParticipants] or the default footer, which
 * holds the sender name and the timestamp.
 *
 * @param messageItem - Message to show.
 * @param modifier - Modifier for styling.
 * */
@Composable
internal fun MessageFooter(
    messageItem: MessageItem,
    modifier: Modifier = Modifier,
) {
    val (message, position) = messageItem
    val hasThread = message.threadParticipants.isNotEmpty()

    if (hasThread) {
        ThreadParticipants(modifier = modifier, participants = message.threadParticipants)
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

            Text(
                SimpleDateFormat.getTimeInstance().format(message.createdAt ?: Date()),
                style = ChatTheme.typography.footnote,
                color = ChatTheme.colors.textLowEmphasis
            )
        }
    }
}

/**
 * Shows the deleted message footer, which holds the timestamp, or
 *
 * @param message - Message to show.
 * @param modifier - Modifier for styling.
 * */
@Composable
private fun DeletedMessageFooter(
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

        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = SimpleDateFormat.getTimeInstance().format(message.deletedAt ?: Date()),
            style = ChatTheme.typography.footnote,
            color = ChatTheme.colors.textLowEmphasis
        )
    }
}
