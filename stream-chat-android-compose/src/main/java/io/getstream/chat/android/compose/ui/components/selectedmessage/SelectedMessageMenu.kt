package io.getstream.chat.android.compose.ui.components.selectedmessage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.state.MessageAction
import io.getstream.chat.android.common.state.React
import io.getstream.chat.android.compose.handlers.SystemBackPressedHandler
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
 * @param shape Changes the shape of [SelectedMessageMenu].
 * @param overlayColor The color applied to the overlay.
 * @param reactionTypes The available reactions within the menu.
 * @param onDismiss Handler called when the menu is dismissed.
 * @param headerContent Leading vertical Composable that allows the user to customize the content shown in [SelectedMessageOptions].
 * By default [ReactionOptions].
 * @param bodyContent Trailing vertical Composable that allows the user to customize the content shown in [SelectedMessageOptions].
 * By Default [MessageOptions].
 */
@Composable
public fun SelectedMessageMenu(
    message: Message,
    messageOptions: List<MessageOptionItemState>,
    onMessageAction: (MessageAction) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.bottomSheet,
    overlayColor: Color = ChatTheme.colors.overlay,
    reactionTypes: Map<String, Int> = ChatTheme.reactionTypes,
    onDismiss: () -> Unit = {},
    headerContent: @Composable ColumnScope.() -> Unit = {
        ReactionOptions(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
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
    Box(
        modifier = Modifier
            .background(overlayColor)
            .fillMaxSize()
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        Card(
            modifier = modifier
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            shape = shape,
            backgroundColor = ChatTheme.colors.barsBackground
        ) {
            SelectedMessageOptions(
                modifier = Modifier
                    .padding(top = 12.dp),
                message = message,
                reactionTypes = reactionTypes,
                messageOptions = messageOptions,
                onMessageAction = onMessageAction,
                headerContent = headerContent,
                bodyContent = bodyContent
            )
        }
    }

    SystemBackPressedHandler(isEnabled = true) {
        onDismiss()
    }
}

/**
 * Preview of [SelectedMessageMenu].
 */
@Preview(showBackground = true, name = "SelectedMessageMenu Preview")
@Composable
private fun SelectedMessageMenuPreview() {
    ChatTheme {
        val messageOptionsStateList = defaultMessageOptionsState(
            selectedMessage = Message(),
            currentUser = User(),
            isInThread = false
        )

        SelectedMessageMenu(message = Message(), messageOptions = messageOptionsStateList, onMessageAction = {})
    }
}
