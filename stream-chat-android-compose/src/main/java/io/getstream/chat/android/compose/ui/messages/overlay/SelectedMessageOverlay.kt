package io.getstream.chat.android.compose.ui.messages.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.image
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.state.messages.items.MessageItem
import io.getstream.chat.android.compose.state.messages.items.None
import io.getstream.chat.android.compose.state.messages.list.Copy
import io.getstream.chat.android.compose.state.messages.list.Delete
import io.getstream.chat.android.compose.state.messages.list.Edit
import io.getstream.chat.android.compose.state.messages.list.MessageAction
import io.getstream.chat.android.compose.state.messages.list.MessageOption
import io.getstream.chat.android.compose.state.messages.list.MuteUser
import io.getstream.chat.android.compose.state.messages.list.React
import io.getstream.chat.android.compose.state.messages.list.Reply
import io.getstream.chat.android.compose.state.messages.list.ThreadReply
import io.getstream.chat.android.compose.state.messages.list.buildMessageOption
import io.getstream.chat.android.compose.state.messages.reaction.ReactionOption
import io.getstream.chat.android.compose.ui.common.MessageBubble
import io.getstream.chat.android.compose.ui.common.avatar.Avatar
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageContent
import io.getstream.chat.android.compose.ui.messages.list.DeletedMessageContent
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * The overlays that's shown when the user selects a message, in the ConversationScreen.
 *
 * It shows various message options, as well as reactions, that the user can take. It also shows the
 * currently selected message in the middle of these options.
 *
 * @param reactionTypes - The types of reactions the user can use to react to messages.
 * @param messageOptions - The [buildMessageOption] the user can select to trigger on the message.
 * @param message - Selected message.
 * @param onMessageAction - Handler for any of the available message actions (options + reactions).
 * @param onDismiss - Handler when the user dismisses the UI.
 * */
@Composable
public fun SelectedMessageOverlay(
    reactionTypes: Map<String, Int> = ChatTheme.reactionTypes,
    messageOptions: List<MessageOption>,
    message: Message,
    onMessageAction: (MessageAction) -> Unit,
    onDismiss: () -> Unit,
) {
    val ownReactions = message.ownReactions

    val reactionOptions = reactionTypes.entries
        .map { (type, drawable) ->
            ReactionOption(
                drawable = painterResource(drawable),
                isSelected = ownReactions.any { it.type == type },
                type = type
            )
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray.copy(alpha = 0.7f))
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(Modifier.padding(16.dp)) {
            ReactionOptions(
                options = reactionOptions,
                onReactionClick = {
                    onMessageAction(
                        React(
                            Reaction(
                                messageId = message.id,
                                type = it.type,
                            ),
                            message
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.size(8.dp))

            SelectedMessage(message)

            Spacer(modifier = Modifier.size(8.dp))

            MessageOptions(
                options = messageOptions,
                onMessageAction = onMessageAction
            )
        }
    }
}

/**
 * A row of selectable reactions on a message. Users can provide their own, or use the default.
 *
 * @param options - The options to show.
 * @param onReactionClick - Handler when the user clicks on any reaction.
 * */
@Composable
internal fun ReactionOptions(
    options: List<ReactionOption>,
    onReactionClick: (ReactionOption) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ChatTheme.colors.barsBackground,
    ) {
        LazyRow(
            modifier = Modifier
                .wrapContentWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.width(2.dp)) }
            items(options) { option ->
                ReactionOptionItem(option = option, onReactionClick = onReactionClick)
            }

            item { Spacer(modifier = Modifier.width(2.dp)) }
        }
    }
}

/**
 * Each reaction item in the row of reactions.
 *
 * @param option - The reaction to show.
 * @param onReactionClick - Handler when the user clicks on the reaction.
 * */
@Composable
internal fun ReactionOptionItem(
    option: ReactionOption,
    onReactionClick: (ReactionOption) -> Unit,
) {
    Icon(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false),
                onClick = { onReactionClick(option) }
            )
            .padding(2.dp),
        painter = option.drawable,
        contentDescription = option.type,
        tint = if (option.isSelected) ChatTheme.colors.primaryAccent else ChatTheme.colors.textLowEmphasis
    )
}

/**
 * The UI for a basic text message that's selected. It doesn't have all the components as a message
 * in the list, as those are not as important.
 *
 * @param message - Message to show.
 * */
@Composable
private fun SelectedMessage(message: Message) {
    val attachmentFactory = ChatTheme.attachmentFactories.firstOrNull { it.canHandle(message) }

    Row(
        Modifier.widthIn(max = 300.dp)
    ) {
        val authorImage = rememberImagePainter(data = message.user.image)

        Avatar(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)
                .size(24.dp)
                .align(Alignment.Bottom),
            painter = authorImage
        )

        MessageBubble(
            shape = ChatTheme.shapes.otherMessageBubble, color = ChatTheme.colors.barsBackground,
            content = {
                if (message.deletedAt != null) {
                    DeletedMessageContent()
                } else {
                    Column {
                        attachmentFactory?.factory?.invoke(
                            AttachmentState(
                                modifier = Modifier.padding(4.dp),
                                message = MessageItem(message, None)
                            ) {}
                        )

                        if (message.text.isNotEmpty()) {
                            DefaultMessageContent(message = message)
                        }
                    }
                }
            }
        )
    }
}

/**
 * List of options the user can choose from, when selecting a message.
 *
 * @param options - The options to show, default or user-provided.
 * @param onMessageAction - Handler when the user selects an action.
 * @param modifier - Modifier for styling.
 * */
@Composable
internal fun MessageOptions(
    options: List<MessageOption>,
    onMessageAction: (MessageAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ChatTheme.colors.barsBackground,
    ) {
        LazyColumn(modifier) {
            items(options) { option ->
                MessageOptionItem(
                    option = option,
                    onMessageOptionClick = { onMessageAction(it.action) }
                )
            }
        }
    }
}

/**
 * Each option item in the column of options.
 *
 * @param option - The option to show.
 * @param onMessageOptionClick - Handler when the user selects the option.
 * */
@Composable
internal fun MessageOptionItem(
    option: MessageOption,
    onMessageOptionClick: (MessageOption) -> Unit,
) {
    val title = stringResource(id = option.title)

    Row(
        Modifier
            .fillMaxWidth()
            .clickable(
                onClick = { onMessageOptionClick(option) },
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple()
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = option.icon,
            tint = option.iconColor,
            contentDescription = title,
        )

        Text(
            modifier = Modifier.padding(start = 12.dp),
            text = title,
            style = ChatTheme.typography.footnoteBold,
            color = option.titleColor
        )
    }
}

/**
 * Builds the default message options we show to our users.
 *
 * @param selectedMessage - Currently selected message, used to callbacks.
 * @param user - Current user, used to expose different states for messages.
 * @param inThread - If the message is in a thread or not, to block off some options.
 * */
@Composable
public fun defaultMessageOptions(
    selectedMessage: Message,
    user: User?,
    inThread: Boolean,
): List<MessageOption> {
    val messageOptions = arrayListOf(
        buildMessageOption(
            title = R.string.stream_compose_reply,
            icon = Icons.Default.Reply,
            action = Reply(selectedMessage)
        )
    )

    if (selectedMessage.text.isNotEmpty() && selectedMessage.attachments.isEmpty()) {
        messageOptions.add(
            buildMessageOption(
                title = R.string.stream_compose_copy_message,
                icon = Icons.Default.FileCopy,
                action = Copy(selectedMessage)
            )
        )
    }

    if (!inThread) {
        messageOptions.add(
            1,
            buildMessageOption(
                title = R.string.stream_compose_thread_reply,
                icon = Icons.Default.Chat,
                action = ThreadReply(selectedMessage)
            )
        )
    }

    if (selectedMessage.user.id == user?.id) {
        messageOptions.add(
            buildMessageOption(
                title = R.string.stream_compose_edit_message,
                icon = Icons.Default.Edit,
                action = Edit(selectedMessage)
            )
        )

        messageOptions.add(
            MessageOption(
                title = R.string.stream_compose_delete_message,
                icon = Icons.Default.Delete,
                action = Delete(selectedMessage),
                iconColor = ChatTheme.colors.errorAccent,
                titleColor = ChatTheme.colors.errorAccent
            )
        )
    } else {
        messageOptions.add(
            buildMessageOption(
                title = R.string.stream_compose_mute_user,
                icon = Icons.Default.VolumeMute,
                action = MuteUser(selectedMessage)
            )
        )
    }

    return messageOptions
}
