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
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.state.messages.list.MessageOptionState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Displays all available selections of [MessageOptionItem] in an internal [Column].
 *
 * @param messageOptionStateList List of available options the user can click on.
 * @param onMessageItemOptionClicked Handler that propagates click events that occur on an individual [MessageOptionItem].
 * @param modifier Compose UI [Modifier] that is applied to the internally used [Column].
 * @param itemContent Composable slot that represents an individual item inside the internally used [Column].
 */
@Composable
public fun MessageOptions(
    messageOptionStateList: List<MessageOptionState>,
    onMessageItemOptionClicked: (MessageOptionState) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable ColumnScope.(MessageOptionState) -> Unit = { messageOptionsState ->
        MessageOptionItem(
            modifier = Modifier
                .fillMaxWidth()
                .height(ChatTheme.dimens.messageOverlayActionItemHeight)
                .clickable(
                    onClick = { onMessageItemOptionClicked(messageOptionsState) },
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple()
                ),
            option = messageOptionsState
        )
    },
) {
    Column(modifier = modifier) {
        messageOptionStateList.forEach { messageOptionsState ->
            itemContent(messageOptionsState)
        }
    }
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

        MessageOptions(messageOptionStateList = messageOptionsStateList, onMessageItemOptionClicked = {})
    }
}
