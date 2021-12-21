package io.getstream.chat.android.compose.ui.components.messageoptions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
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
import io.getstream.chat.android.compose.state.messageoptions.MessageOptionItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Displays all available [MessageOptionItem]s.
 *
 * @param options List of available options.
 * @param onMessageOptionSelected Handler that propagates click events on each item.
 * @param modifier Modifier for styling.
 * @param itemContent Composable that allows the user to customize the individual items shown in [MessageOptions].
 * By default shows individual message items.
 */
@Composable
public fun MessageOptions(
    options: List<MessageOptionItemState>,
    onMessageOptionSelected: (MessageOptionItemState) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable ColumnScope.(MessageOptionItemState) -> Unit = { option ->
        DefaultMessageOptionItem(
            option = option,
            onMessageOptionSelected = onMessageOptionSelected
        )
    },
) {
    Column(modifier = modifier) {
        options.forEach { option ->
            itemContent(option)
        }
    }
}

/**
 * The default message option item.
 *
 * @param option The represented option.
 * @param onMessageOptionSelected Handler when the user selects the option.
 */
@Composable
internal fun DefaultMessageOptionItem(
    option: MessageOptionItemState,
    onMessageOptionSelected: (MessageOptionItemState) -> Unit,
) {
    MessageOptionItem(
        modifier = Modifier
            .fillMaxWidth()
            .height(ChatTheme.dimens.messageOverlayActionItemHeight)
            .clickable(
                onClick = { onMessageOptionSelected(option) },
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple()
            ),
        option = option
    )
}

/**
 * Preview of [MessageOptions].
 * */
@Preview(showBackground = true, name = "MessageOptions Preview")
@Composable
private fun MessageOptionsPreview() {
    ChatTheme {
        val messageOptionsStateList = defaultMessageOptionsState(
            selectedMessage = Message(),
            currentUser = User(),
            isInThread = false
        )

        MessageOptions(options = messageOptionsStateList, onMessageOptionSelected = {})
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
): List<MessageOptionItemState> {
    val selectedMessageUserId = selectedMessage.user.id

    val isTextOnlyMessage = selectedMessage.text.isNotEmpty() && selectedMessage.attachments.isEmpty()
    val isOwnMessage = selectedMessageUserId == currentUser?.id
    val isUserMuted = currentUser?.mutes?.any { it.target.id == selectedMessageUserId } ?: false

    return listOfNotNull(
        MessageOptionItemState(
            title = R.string.stream_compose_reply,
            iconPainter = painterResource(R.drawable.stream_compose_ic_reply),
            action = Reply(selectedMessage),
            titleColor = ChatTheme.colors.textHighEmphasis,
            iconColor = ChatTheme.colors.textLowEmphasis,
        ),
        if (!isInThread) {
            MessageOptionItemState(
                title = R.string.stream_compose_thread_reply,
                iconPainter = painterResource(R.drawable.stream_compose_ic_thread),
                action = ThreadReply(selectedMessage),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconColor = ChatTheme.colors.textLowEmphasis,
            )
        } else null,
        if (isTextOnlyMessage) {
            MessageOptionItemState(
                title = R.string.stream_compose_copy_message,
                iconPainter = painterResource(R.drawable.stream_compose_ic_copy),
                action = Copy(selectedMessage),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconColor = ChatTheme.colors.textLowEmphasis,
            )
        } else null,
        if (isOwnMessage) {
            MessageOptionItemState(
                title = R.string.stream_compose_edit_message,
                iconPainter = painterResource(R.drawable.stream_compose_ic_edit),
                action = Edit(selectedMessage),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconColor = ChatTheme.colors.textLowEmphasis,
            )
        } else null,
        if (!isOwnMessage) {
            MessageOptionItemState(
                title = R.string.stream_compose_flag_message,
                iconPainter = painterResource(R.drawable.stream_compose_ic_flag),
                action = Flag(selectedMessage),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconColor = ChatTheme.colors.textLowEmphasis,
            )
        } else null,
        MessageOptionItemState(
            title = if (selectedMessage.pinned) R.string.stream_compose_unpin_message else R.string.stream_compose_pin_message,
            action = Pin(selectedMessage),
            iconPainter = painterResource(id = if (selectedMessage.pinned) R.drawable.stream_compose_ic_unpin_message else R.drawable.stream_compose_ic_pin_message),
            iconColor = ChatTheme.colors.textLowEmphasis,
            titleColor = ChatTheme.colors.textHighEmphasis
        ),
        if (isOwnMessage) {
            MessageOptionItemState(
                title = R.string.stream_compose_delete_message,
                iconPainter = painterResource(R.drawable.stream_compose_ic_delete),
                action = Delete(selectedMessage),
                iconColor = ChatTheme.colors.errorAccent,
                titleColor = ChatTheme.colors.errorAccent
            )
        } else null,
        if (!isOwnMessage) {
            MessageOptionItemState(
                title = if (isUserMuted) R.string.stream_compose_unmute_user else R.string.stream_compose_mute_user,
                iconPainter = painterResource(id = if (isUserMuted) R.drawable.stream_compose_ic_unmute else R.drawable.stream_compose_ic_mute),
                action = MuteUser(selectedMessage),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconColor = ChatTheme.colors.textLowEmphasis,
            )
        } else null
    )
}
