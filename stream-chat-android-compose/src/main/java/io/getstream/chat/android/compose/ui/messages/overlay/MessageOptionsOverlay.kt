package io.getstream.chat.android.compose.ui.messages.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Alignment.Companion.Start
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.state.Copy
import io.getstream.chat.android.common.state.Delete
import io.getstream.chat.android.common.state.Edit
import io.getstream.chat.android.common.state.MessageAction
import io.getstream.chat.android.common.state.MuteUser
import io.getstream.chat.android.common.state.Pin
import io.getstream.chat.android.common.state.Reply
import io.getstream.chat.android.common.state.ThreadReply
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.list.MessageOptionState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * The message options overlays that's shown when the user selects a message.
 *
 * It shows various message options, as well as reactions, that the user can take. It also shows the
 * currently selected message in the middle of these options.
 *
 * @param messageOptions Actions the user can trigger on the message.
 * @param message The selected message.
 * @param currentUser The currently logged in user.
 * @param onMessageAction Handler for any of the available message actions (options + reactions).
 * @param onDismiss Handler when the user dismisses the UI.
 */
@Composable
public fun MessageOptionsOverlay(
    messageOptions: List<MessageOptionState>,
    message: Message,
    currentUser: User?,
    onMessageAction: (MessageAction) -> Unit,
    onDismiss: () -> Unit,
) {
    val isMine = message.user.id == currentUser?.id

    MessageOverlay(
        message = message,
        horizontalAlignment = if (isMine) End else Start,
        onDismiss = onDismiss,
        headerContent = {
            DefaultMessageOverlayHeaderContent(
                message = message,
                onMessageAction = onMessageAction
            )
        },
        centerContent = {
            DefaultMessageOverlayCenterContent(
                message = message,
                currentUser = currentUser
            )
        },
        footerContent = {
            DefaultMessageOptionsOverlayFooterContent(
                options = messageOptions,
                onMessageAction = onMessageAction
            )
        }
    )
}

/**
 * Represent the default footer content of the message options overlay.
 *
 * @param options The options to show.
 * @param onMessageAction Handler when the user selects an action.
 */
@Composable
private fun DefaultMessageOptionsOverlayFooterContent(
    options: List<MessageOptionState>,
    onMessageAction: (MessageAction) -> Unit,
) {
    Spacer(modifier = Modifier.size(8.dp))

    MessageOptions(
        options = options,
        onMessageAction = onMessageAction
    )
}

/**
 * List of options the user can choose from, when selecting a message.
 *
 * @param options The options to show.
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
        Column(modifier) {
            options.forEach { option ->
                key(option) {
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
