package io.getstream.chat.android.compose.ui.messages.list

import android.content.Intent
import android.net.Uri
import android.util.Patterns
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.getstream.sdk.chat.utils.UiUtils
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.image
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.components.Avatar
import io.getstream.chat.android.compose.ui.components.MessageBubble
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import java.text.SimpleDateFormat
import java.util.*

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
 * @param message - The message to show.
 * @param onLongItemClick - Handler when the user selects a message, on long tap.
 * @param currentUser - Current user info, required for various states.
 * @param modifier - Modifier for styling.
 * @param onThreadClick - Handler for thread clicks, if this message has a thread going.
 * */
@InternalStreamChatApi
@ExperimentalFoundationApi
@Composable
internal fun DefaultMessageContainer(
    message: Message,
    onLongItemClick: (Message) -> Unit,
    currentUser: User?,
    modifier: Modifier = Modifier,
    onThreadClick: (Message) -> Unit = {}
) {
    val attachmentFactory = ChatTheme.attachmentFactories.firstOrNull { it.canHandle(message) }
    val isDeleted = message.deletedAt != null
    val hasThread = message.threadParticipants.isNotEmpty()
    val ownsMessage = message.user.id == currentUser?.id

    val messageCardColor =
        if (ownsMessage) ChatTheme.colors.cardBackground else ChatTheme.colors.cardAltBackground

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

    Row(
        modifier
            .widthIn(max = 300.dp)
            .then(clickModifier)
    ) {
        val authorImage = rememberImagePainter(data = message.user.image)

        Avatar(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)
                .size(24.dp)
                .align(Bottom),
            painter = authorImage
        )

        Column(modifier) {
            // reactions
            val reactionTypes = UiUtils.getReactionTypes()

            val reactions = message.reactionCounts
                .mapKeys { (type, _) -> reactionTypes[type] ?: "" }

            if (reactions.isNotEmpty()) {
                MessageReactions(modifier = Modifier.padding(4.dp), reactions)
            }

            // content
            MessageBubble(color = messageCardColor, content = {
                if (message.deletedAt != null) {
                    DeletedMessageContent()
                } else {
                    Column {
                        attachmentFactory?.factory?.invoke(
                            AttachmentState(
                                modifier = Modifier.padding(4.dp),
                                message = message,
                                onLongItemClick = onLongItemClick
                            )
                        )

                        if (message.text.isNotEmpty()) {
                            DefaultMessageContent(message = message)
                        }
                    }
                }
            })

            // footer
            if (isDeleted && ownsMessage) {
                DeletedMessageFooter(
                    modifier = Modifier,
                    message = message
                )
            } else if (!isDeleted) {
                MessageFooter(message)
            }
        }
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
    modifier: Modifier = Modifier,
    reactions: Map<String, Int>
) {
    Row(
        modifier = modifier
            .background(shape = RoundedCornerShape(16.dp), color = ChatTheme.colors.cardBackground)
            .padding(4.dp),
        verticalAlignment = CenterVertically
    ) {
        for ((emoji, count) in reactions) {
            Row(verticalAlignment = CenterVertically) {
                Text(
                    modifier = Modifier.padding(2.dp),
                    style = ChatTheme.typography.footnote,
                    color = ChatTheme.colors.textMidEmphasis,
                    text = count.toString()
                )

                Text(
                    modifier = Modifier.padding(2.dp),
                    text = emoji
                )
            }
        }
    }
}

/**
 * The default text message content. It holds the quoted message in case there is one.
 *
 * @param message - The message to show.
 * @param modifier - Modifier for styling.
 * */
@ExperimentalFoundationApi
@Composable
private fun DefaultMessageContent(
    message: Message,
    modifier: Modifier = Modifier
) {
    val quotedMessage = message.replyTo

    Column(
        modifier = modifier
    ) {
        if (quotedMessage != null) {
            Column {
                QuotedMessage(
                    modifier = Modifier.padding(8.dp),
                    message = quotedMessage
                )

                MessageText(message = message)
            }
        } else {
            MessageText(message = message)
        }
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
@ExperimentalFoundationApi
@Composable
internal fun MessageText(
    message: Message,
    modifier: Modifier = Modifier
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
    val linkAttachments = message.attachments.filter { it.ogUrl != null }
    val parts = text.split(" ")
    val links = parts.filter { Patterns.WEB_URL.matcher(it).matches() }

    return buildAnnotatedString {

        /**
         * First we add the whole text to the [AnnotatedString] and style it as a regular text.
         * */
        append(text)

        addStyle(
            SpanStyle(
                color = ChatTheme.colors.textHighEmphasis,
                fontSize = 14.sp
            ),
            start = 0,
            end = text.length
        )

        /**
         * Then for each available link in the text, we add a different style, to represent the links,
         * as well as add a String annotation to its start and end. This will give us the ability to
         * open the original URL for the link preview on clicks.
         * */
        links.forEachIndexed { index, link ->
            val start = text.indexOf(link)
            val end = start + link.length

            addStyle(
                style = SpanStyle(
                    color = ChatTheme.colors.primaryAccent,
                    textDecoration = TextDecoration.Underline,
                ), start = start, end = end
            )

            addStringAnnotation(
                tag = "URL",
                annotation = linkAttachments[index].ogUrl ?: "",
                start = start,
                end = end
            )
        }
    }
}

/**
 * Shows a row of participants in the message thread, if they exist.
 *
 * @param participants - List of users in the thread.
 * @param modifier - Modifier for styling.
 * */
@Composable
private fun ThreadParticipants(
    participants: List<User>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(4.dp)
    ) {

        Text(
            modifier = Modifier.padding(end = 4.dp),
            text = stringResource(id = R.string.thread_footnote),
            style = ChatTheme.typography.footnote,
            color = ChatTheme.colors.textHighEmphasis
        )

        for (user in participants) {
            val painter = rememberImagePainter(data = user.image)
            Avatar(
                modifier = Modifier
                    .padding(2.dp)
                    .size(16.dp), painter = painter
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
@ExperimentalFoundationApi
@Composable
internal fun QuotedMessage(
    message: Message,
    modifier: Modifier = Modifier
) {
    val painter = rememberImagePainter(data = message.user.image)

    Row(modifier = modifier, verticalAlignment = Bottom) {
        Avatar(
            modifier = Modifier
                .size(24.dp),
            painter = painter
        )

        Spacer(modifier = Modifier.size(8.dp))

        MessageBubble(color = Color.White, content = {
            MessageText(message = message)
        })
    }
}

/**
 * Component that shows that the message has been (soft) deleted.
 *
 * @param modifier - Modifier for styling.
 * */
@Composable
private fun DeletedMessageContent(
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier
            .padding(
                start = 12.dp,
                end = 12.dp,
                top = 8.dp,
                bottom = 8.dp
            ),
        text = stringResource(id = R.string.message_deleted),
        color = ChatTheme.colors.textLowEmphasis,
        style = ChatTheme.typography.bodyItalic,
        fontSize = 12.sp
    )
}

/**
 * Default message footer, which contains either [ThreadParticipants] or the default footer, which
 * holds the sender name and the timestamp.
 *
 * @param message - Message to show.
 * @param modifier - Modifier for styling.
 * */
@Composable
internal fun MessageFooter(
    message: Message,
    modifier: Modifier = Modifier
) {
    val hasThread = message.threadParticipants.isNotEmpty()

    if (hasThread) {
        ThreadParticipants(modifier = modifier, participants = message.threadParticipants)
    } else {
        Row(
            modifier = modifier.padding(top = 4.dp),
            verticalAlignment = CenterVertically
        ) {
            Text(
                modifier = Modifier.padding(end = 8.dp),
                text = message.user.name,
                fontSize = 12.sp,
                color = ChatTheme.colors.textMidEmphasis
            )

            Text(
                SimpleDateFormat.getTimeInstance().format(message.createdAt ?: Date()),
                style = ChatTheme.typography.footnote,
                fontSize = 12.sp,
                color = ChatTheme.colors.textMidEmphasis
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
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.padding(4.dp), verticalAlignment = CenterVertically) {
        Icon(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(12.dp),
            imageVector = Icons.Default.RemoveRedEye,
            contentDescription = null
        )

        Text(
            text = stringResource(id = R.string.only_visible_to_you),
            style = ChatTheme.typography.footnote,
            color = ChatTheme.colors.textHighEmphasis
        )

        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = SimpleDateFormat.getTimeInstance().format(message.deletedAt ?: Date()),
            style = ChatTheme.typography.footnote,
            fontSize = 12.sp,
            color = ChatTheme.colors.textMidEmphasis
        )
    }
}

/**
 *
 * @param message - Message to show.
 * @param modifier - Modifier for styling.
 * */
@InternalStreamChatApi
@ExperimentalFoundationApi
@Composable
internal fun ThreadHeaderItem(
    message: Message,
    modifier: Modifier = Modifier
) {
    // TODO we need to change the UI of this. Maybe pull it out of the list.
    Surface(modifier = modifier, color = ChatTheme.colors.appCanvas) {
        DefaultMessageContainer(message = message, onLongItemClick = {}, null)
    }
}