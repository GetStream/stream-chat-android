package io.getstream.chat.android.compose.ui.components.messageoptions

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.state.messages.list.MessageOptionState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Displays all available selections of [MessageOptionItem] in an internal [Column]
 *
 * @param messageOptionStateList list of available options the user can click on
 * @param onMessageItemOptionClicked Handler that propagates click events that occur on an individual [MessageOptionItem]
 * @param modifier Compose UI [Modifier] that is applied to the internally used [Column]
 */
@Composable
public fun MessageOptions(
    messageOptionStateList: List<MessageOptionState>,
    onMessageItemOptionClicked: (MessageOptionState) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        messageOptionStateList.forEach {
            MessageOptionItem(
                option = it,
                onMessageOptionClick = onMessageItemOptionClicked)
        }
    }
}

/**
 * Preview of [MessageOptions]
 * */
@Preview(showBackground = true, name = "MessageOptions Preview")
@Composable
private fun MessageOptionsPreview() {
    ChatTheme {
        val messageOptionsStateList = defaultMessageOptionsState(selectedMessage = Message(),
            currentUser = User(),
            isInThread = false)

        MessageOptions(messageOptionStateList = messageOptionsStateList, onMessageItemOptionClicked = {})
    }
}
