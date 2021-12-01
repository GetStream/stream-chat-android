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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
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
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Alignment.Companion.Start
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.state.Copy
import io.getstream.chat.android.common.state.Delete
import io.getstream.chat.android.common.state.Edit
import io.getstream.chat.android.common.state.MessageAction
import io.getstream.chat.android.common.state.MuteUser
import io.getstream.chat.android.common.state.Pin
import io.getstream.chat.android.common.state.React
import io.getstream.chat.android.common.state.Reply
import io.getstream.chat.android.common.state.ThreadReply
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.list.MessageOptionState
import io.getstream.chat.android.compose.state.messages.reaction.ReactionOptionItemState
import io.getstream.chat.android.compose.ui.attachments.content.MessageAttachmentsContent
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
 * @param reactionTypes The types of reactions the user can use to react to messages.
 * @param messageOptions The [MessageOptionState] that represents actions the user can trigger on the message.
 * @param message Selected message.
 * @param onMessageAction Handler for any of the available message actions (options + reactions).
 * @param onDismiss Handler when the user dismisses the UI.
 */
@Composable
public fun SelectedMessageOverlay(
    reactionTypes: Map<String, Int> = ChatTheme.reactionTypes,
    messageOptions: List<MessageOptionState>,
    message: Message,
    currentUser: User?,
    onMessageAction: (MessageAction) -> Unit,
    onDismiss: () -> Unit,
) {
    val ownReactions = message.ownReactions

    val reactionOptions = reactionTypes.entries
        .map { (type, drawable) ->
            ReactionOptionItemState(
                painter = painterResource(drawable),
                isSelected = ownReactions.any { it.type == type },
                type = type
            )
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatTheme.colors.overlay)
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        val isMine = message.user.id == currentUser?.id

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = if (isMine) End else Start
        ) {
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

            SelectedMessage(
                message = message,
                isMine = isMine
            )

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
 * @param options The options to show.
 * @param onReactionClick Handler when the user clicks on any reaction.
 */
@Composable
public fun ReactionOptions(
    options: List<ReactionOptionItemState>,
    onReactionClick: (ReactionOptionItemState) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ChatTheme.colors.barsBackground,
    ) {
        LazyRow(
            modifier = modifier
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
 * @param option The reaction to show.
 * @param onReactionClick Handler when the user clicks on the reaction.
 */
@Composable
internal fun ReactionOptionItem(
    option: ReactionOptionItemState,
    onReactionClick: (ReactionOptionItemState) -> Unit,
) {
    Icon(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false),
                onClick = { onReactionClick(option) }
            )
            .padding(2.dp),
        painter = option.painter,
        contentDescription = option.type,
        tint = if (option.isSelected) ChatTheme.colors.primaryAccent else ChatTheme.colors.textLowEmphasis
    )
}

/**
 * The UI for a basic text message that's selected. It doesn't have all the components as a message
 * in the list, as those are not as important.
 *
 * @param message Message to show.
 * @param isMine If the message is owned by the current user.
 */
@Composable
private fun SelectedMessage(
    message: Message,
    isMine: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.widthIn(max = 300.dp)
    ) {
        val authorImage = rememberImagePainter(data = message.user.image)

        if (!isMine) {
            Avatar(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .size(24.dp)
                    .align(Alignment.Bottom),
                painter = authorImage
            )
        }

        val messageBubbleShape = if (isMine) ChatTheme.shapes.myMessageBubble else ChatTheme.shapes.otherMessageBubble
        val messageBubbleColor =
            if (isMine) ChatTheme.colors.ownMessagesBackground else ChatTheme.colors.otherMessagesBackground

        MessageBubble(
            shape = messageBubbleShape,
            color = messageBubbleColor,
            content = {
                if (message.deletedAt != null) {
                    DeletedMessageContent()
                } else {
                    Column {
                        MessageAttachmentsContent(message = message, onLongItemClick = {})

                        if (message.text.isNotEmpty()) {
                            DefaultMessageContent(message = message)
                        }
                    }
                }
            }
        )

        if (isMine) {
            Avatar(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .size(24.dp)
                    .align(Alignment.Bottom),
                painter = authorImage
            )
        }
    }
}

/**
 * List of options the user can choose from, when selecting a message.
 *
 * @param options The options to show, default or user-provided.
 * @param onMessageAction Handler when the user selects an action.
 * @param modifier Modifier for styling.
 */
@Composable
public fun MessageOptions(
    options: List<MessageOptionState>,
    onMessageAction: (MessageAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = Modifier.sizeIn(
            maxHeight = ChatTheme.dimens.messageOptionsMaxHeight,
            maxWidth = ChatTheme.dimens.messageOptionsMaxWidth
        ),
        shape = RoundedCornerShape(ChatTheme.dimens.messageOptionsRoundedCorners),
        color = ChatTheme.colors.barsBackground,
    ) {
        LazyColumn(modifier) {
            items(options) { option ->
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(color = ChatTheme.colors.borders)
                )

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
 * @param option The option to show.
 * @param onMessageOptionClick Handler when the user selects the option.
 */
@Composable
internal fun MessageOptionItem(
    option: MessageOptionState,
    onMessageOptionClick: (MessageOptionState) -> Unit,
) {
    val title = stringResource(id = option.title)

    Row(
        Modifier
            .fillMaxWidth()
            .height(ChatTheme.dimens.messageOverlayActionItemHeight)
            .clickable(
                onClick = { onMessageOptionClick(option) },
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple()
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            modifier = Modifier.padding(horizontal = 16.dp),
            painter = option.iconPainter,
            tint = option.iconColor,
            contentDescription = title,
        )

        Text(
            text = title,
            style = ChatTheme.typography.body,
            color = option.titleColor
        )
    }
}

/**
 * Builds the default message options we show to our users.
 *
 * @param selectedMessage Currently selected message, used to callbacks.
 * @param currentUser Current user, used to expose different states for messages.
 * @param isInThread If the message is in a thread or not, to block off some options.
 */
@Composable
public fun defaultMessageOptionsState(
    selectedMessage: Message,
    currentUser: User?,
    isInThread: Boolean,
): List<MessageOptionState> {
    val isTextOnlyMessage = selectedMessage.text.isNotEmpty() && selectedMessage.attachments.isEmpty()
    val isOwnMessage = selectedMessage.user.id == currentUser?.id

    return listOfNotNull(
        MessageOptionState(
            title = R.string.stream_compose_reply,
            iconPainter = painterResource(R.drawable.stream_compose_ic_reply),
            action = Reply(selectedMessage),
            titleColor = ChatTheme.colors.textHighEmphasis,
            iconColor = ChatTheme.colors.textLowEmphasis,
        ),
        if (!isInThread) {
            MessageOptionState(
                title = R.string.stream_compose_thread_reply,
                iconPainter = painterResource(R.drawable.stream_compose_ic_thread_reply),
                action = ThreadReply(selectedMessage),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconColor = ChatTheme.colors.textLowEmphasis,
            )
        } else null,
        if (isTextOnlyMessage) {
            MessageOptionState(
                title = R.string.stream_compose_copy_message,
                iconPainter = painterResource(R.drawable.stream_compose_ic_copy),
                action = Copy(selectedMessage),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconColor = ChatTheme.colors.textLowEmphasis,
            )
        } else null,
        if (isOwnMessage) {
            MessageOptionState(
                title = R.string.stream_compose_edit_message,
                iconPainter = painterResource(R.drawable.stream_compose_ic_edit),
                action = Edit(selectedMessage),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconColor = ChatTheme.colors.textLowEmphasis,
            )
        } else null,
        MessageOptionState(
            title = if (selectedMessage.pinned) R.string.stream_compose_unpin_message else R.string.stream_compose_pin_message,
            action = Pin(selectedMessage),
            iconPainter = painterResource(id = if (selectedMessage.pinned) R.drawable.stream_compose_ic_unpin_message else R.drawable.stream_compose_ic_pin_message),
            iconColor = ChatTheme.colors.textLowEmphasis,
            titleColor = ChatTheme.colors.textHighEmphasis
        ),
        if (isOwnMessage) {
            MessageOptionState(
                title = R.string.stream_compose_delete_message,
                iconPainter = painterResource(R.drawable.stream_compose_ic_delete),
                action = Delete(selectedMessage),
                iconColor = ChatTheme.colors.errorAccent,
                titleColor = ChatTheme.colors.errorAccent
            )
        } else null,
        if (!isOwnMessage) {
            MessageOptionState(
                title = R.string.stream_compose_mute_user,
                iconPainter = painterResource(R.drawable.stream_compose_ic_mute),
                action = MuteUser(selectedMessage),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconColor = ChatTheme.colors.textLowEmphasis,
            )
        } else null
    )
}
