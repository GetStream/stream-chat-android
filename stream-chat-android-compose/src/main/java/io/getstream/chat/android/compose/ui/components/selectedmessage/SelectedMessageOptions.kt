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
import io.getstream.chat.android.compose.state.messageoptions.MessageOptionItemState
import io.getstream.chat.android.compose.ui.components.messageoptions.MessageOptions
import io.getstream.chat.android.compose.ui.components.messageoptions.defaultMessageOptionsState
import io.getstream.chat.android.compose.ui.components.reactionoptions.ReactionOptions
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents the options user can take after selecting a message.
 *
 * @param message The selected message.
 * @param messageOptions The available message options within the menu.
 * @param onMessageAction Handler that propagates click events on each item.
 * @param modifier Modifier for styling.
 * @param reactionTypes The available reactions within the menu.
 * @param headerContent Leading vertical Composable that allows the user to customize the content shown in [SelectedMessageOptions].
 * By default shows reaction options.
 * @param bodyContent Trailing vertical Composable that allows the user to customize the content shown in [SelectedMessageOptions].
 * By Default shows message options.
 */
@Composable
public fun SelectedMessageOptions(
    message: Message,
    messageOptions: List<MessageOptionItemState>,
    onMessageAction: (MessageAction) -> Unit,
    modifier: Modifier = Modifier,
    reactionTypes: Map<String, Int> = ChatTheme.reactionTypes,
    headerContent: @Composable ColumnScope.() -> Unit = {
        ReactionOptions(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            reactionTypes = reactionTypes,
            onReactionOptionSelected = {
                onMessageAction(
                    React(
                        reaction = Reaction(messageId = message.id, type = it.type),
                        message = message
                    )
                )
            },
            ownReactions = message.ownReactions
        )
    },
    bodyContent: @Composable ColumnScope.() -> Unit = {
        MessageOptions(
            options = messageOptions,
            onMessageOptionSelected = {
                onMessageAction(it.action)
            }
        )
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
