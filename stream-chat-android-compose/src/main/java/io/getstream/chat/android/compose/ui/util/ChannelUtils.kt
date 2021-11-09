package io.getstream.chat.android.compose.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.getstream.sdk.chat.viewmodel.messages.getCreatedAtOrThrow
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.channel.ChannelListViewModel
import java.util.Date

private const val EXTRA_CHANNEL_MUTED: String = "isMuted"

/**
 * Allows storing additional information if the channel is muted for the current user.
 *
 * @see [ChannelListViewModel.enrichMutedChannels]
 */
public var Channel.isMuted: Boolean
    get() = extraData[EXTRA_CHANNEL_MUTED] as Boolean? ?: false
    set(value) {
        extraData[EXTRA_CHANNEL_MUTED] = value
    }

/**
 * Returns channel's last regular or system message if exists.
 * Deleted and silent messages, as well as messages from shadow-banned users, are not taken into account.
 *
 * @return Last message from the channel or null if it doesn't exist.
 */
public fun Channel.getLastMessage(currentUser: User?): Message? =
    messages.asSequence()
        .filter { it.createdAt != null || it.createdLocallyAt != null }
        .filter { it.deletedAt == null }
        .filter { !it.silent }
        .filter { it.user.id == currentUser?.id || !it.shadowed }
        .filter { it.isRegular() || it.isSystem() }
        .maxByOrNull { it.getCreatedAtOrThrow() }

/**
 * Returns the preview text of the last message in a given [Channel].
 *
 * It formats the message based on if there are attachments or not and based on the sender.
 *
 * @param currentUser The current user in the app.
 * @return The formatted preview text for the channel item.
 */
@Composable
@ReadOnlyComposable
public fun Channel.getLastMessagePreviewText(
    currentUser: User?,
): AnnotatedString {
    val context = LocalContext.current

    return buildAnnotatedString {
        getLastMessage(currentUser)?.let { message ->
            val messageText = message.text.trim()

            if (message.isSystem()) {
                append(messageText)
            } else {
                val sender = message.getSenderDisplayName(context, currentUser)

                if (sender != null) {
                    append("$sender: ")

                    addStyle(
                        SpanStyle(
                            fontStyle = ChatTheme.typography.bodyBold.fontStyle
                        ),
                        start = 0,
                        end = sender.length
                    )
                }

                if (messageText.isNotEmpty()) {
                    val startIndex = this.length
                    append("$messageText ")

                    addStyle(
                        SpanStyle(
                            fontStyle = ChatTheme.typography.bodyBold.fontStyle
                        ),
                        start = startIndex,
                        end = startIndex + messageText.length
                    )
                }

                val attachmentText: String? = if (message.attachments.isNotEmpty()) {
                    val textFormatter = ChatTheme.attachmentFactories
                        .firstOrNull { it.canHandle(message.attachments) }
                        ?.textFormatter

                    message.attachments
                        .takeIf { it.isNotEmpty() }
                        ?.mapNotNull { attachment ->
                            textFormatter?.invoke(attachment)
                                ?.let { previewText ->
                                    if (previewText.isNotEmpty()) previewText else null
                                }
                        }?.joinToString()
                } else {
                    null
                }

                if (attachmentText != null) {
                    val startIndex = this.length
                    append(attachmentText)

                    addStyle(
                        SpanStyle(
                            fontStyle = ChatTheme.typography.bodyItalic.fontStyle
                        ),
                        start = startIndex,
                        end = startIndex + attachmentText.length
                    )
                }
            }
        }
    }
}

/**
 * Filters the read status of each person other than the target user.
 *
 * @param userToIgnore The user whose message it is.
 *
 * @return List of [Date] values that represent a read status for each other user in the channel.
 */
public fun Channel.getReadStatuses(userToIgnore: User?): List<Date> {
    return read.filter { it.user.id != userToIgnore?.id }
        .mapNotNull { it.lastRead }
}

/**
 * Checks if the channel is distinct.
 *
 * A distinct channel is a channel created without ID based on members. Internally
 * the server creates a CID which starts with "!members" prefix and is unique for
 * this particular group of users.
 *
 * @return True if the channel is distinct.
 */
public fun Channel.isDistinct(): Boolean = cid.contains("!members")
