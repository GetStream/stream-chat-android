package io.getstream.chat.android.compose.ui.components.messageoptions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.state.Copy
import io.getstream.chat.android.common.state.Delete
import io.getstream.chat.android.common.state.Edit
import io.getstream.chat.android.common.state.Flag
import io.getstream.chat.android.common.state.MuteUser
import io.getstream.chat.android.common.state.Pin
import io.getstream.chat.android.common.state.Reply
import io.getstream.chat.android.common.state.ThreadReply
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.list.MessageOptionState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

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
 * Preview of [MessageOptionItem]
 * */
@Preview(showBackground = true, name = "MessageOptionItem Preview")
@Composable
private fun MessageOptionItemPreview() {
    ChatTheme {
        val messageOptionsState =
            defaultMessageOptionsState(selectedMessage = Message(),
                currentUser = User(),
                isInThread = false).firstOrNull()

        if (messageOptionsState != null)
            MessageOptionItem(option = messageOptionsState, onMessageOptionClick = {})
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
        if (!isOwnMessage) {
            MessageOptionState(
                title = R.string.stream_compose_flag_message,
                iconPainter = painterResource(R.drawable.stream_compose_ic_flag),
                action = Flag(selectedMessage),
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
