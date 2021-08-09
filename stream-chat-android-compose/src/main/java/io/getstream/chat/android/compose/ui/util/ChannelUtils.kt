package io.getstream.chat.android.compose.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.getstream.sdk.chat.utils.extensions.getUsers
import com.getstream.sdk.chat.viewmodel.messages.getCreatedAtOrThrow
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Generates the display name for a channel based on its data.
 *
 * @return - The display name of the channel.
 * */
@Composable
@ReadOnlyComposable
public fun Channel.getDisplayName(): String {
    return name.takeIf { it.isNotEmpty() }
        ?: getUsers()
            .joinToString { it.name }
            .takeIf { it.isNotEmpty() }
        ?: stringResource(id = R.string.stream_compose_channel_list_untitled_channel)
}

/**
 * Returns channel's last regular or system message if exists.
 * Deleted and silent messages, as well as messages from shadow-banned users, are not taken into account.
 *
 * @return Last message from the channel or null if it doesn't exist
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
 * @param currentUser - The current user in the app.
 * @return - The formatted preview text for the channel item.
 * */
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
                    append(messageText)

                    addStyle(
                        SpanStyle(
                            fontStyle = ChatTheme.typography.bodyBold.fontStyle
                        ),
                        start = startIndex,
                        end = startIndex + messageText.length
                    )
                }

                val attachmentText = message.attachments
                    .takeIf { it.isNotEmpty() }
                    ?.mapNotNull { attachment ->
                        attachment.title ?: attachment.name
                    }
                    ?.joinToString()

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
