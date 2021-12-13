package io.getstream.chat.android.compose.ui.components.selectedmessage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.state.MessageAction
import io.getstream.chat.android.common.state.React
import io.getstream.chat.android.compose.state.messages.list.MessageOptionState
import io.getstream.chat.android.compose.ui.components.messageoptions.MessageOptions
import io.getstream.chat.android.compose.ui.components.messageoptions.defaultMessageOptionsState
import io.getstream.chat.android.compose.ui.components.reactionoptions.ReactionOptions
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Uses slots [headerContent] and [bodyContent] to display it's content.
 * By default shows [ReactionOptions] as header content and [MessageOptions] as body content.
 *
 * @param message Used to gather the necessary message information (such as ownership, id, etc.) in order to properly display [SelectedMessageOptions].
 * @param modifier Compose UI [Modifier] that is applied to the internally used [Column].
 * @param reactionTypes By default used to display all the available reaction options inside of [headerContent].
 * @param messageOptions By default used to display all available message options inside of [bodyContent].
 * @param onMessageAction By default a Handler used to propagate click events on individual reaction and message option elements
 * inside of the header and body content.
 * @param headerContent Leading vertical Composable slot
 * @param bodyContent Trailing vertical Composable slot.
 */
@Composable
public fun SelectedMessageOptions(
    message: Message,
    modifier: Modifier = Modifier,
    reactionTypes: Map<String, Int> = ChatTheme.reactionTypes,
    messageOptions: List<MessageOptionState>,
    onMessageAction: (MessageAction) -> Unit,
    headerContent: @Composable ColumnScope.() -> Unit = {
        ReactionOptions(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
            reactionTypes = reactionTypes,
            onReactionOptionClicked = {
                onMessageAction(
                    React(
                        Reaction(
                            messageId = message.id,
                            type = it.type,
                        ),
                        message
                    )
                )
            },
            ownReactions = message.ownReactions)
    },
    bodyContent: @Composable ColumnScope.() -> Unit = {
        MessageOptions(
            messageOptionStateList = messageOptions,
            onMessageItemOptionClicked = {
                onMessageAction(it.action)
            })
    },
) {
    Column(modifier = modifier) {
        headerContent()
        bodyContent()
    }
}

/**
 * Preview of [SelectedMessageOptions].
 */
@Preview(showBackground = true, name = "SelectedMessageOptions Preview")
@Composable
private fun SelectedMessageOptionsPreview() {
    ChatTheme {
        val defaultMessageOptionsState =
            defaultMessageOptionsState(selectedMessage = Message(), currentUser = User(), isInThread = false)

        SelectedMessageOptions(message = Message(), messageOptions = defaultMessageOptionsState, onMessageAction = {})
    }
}