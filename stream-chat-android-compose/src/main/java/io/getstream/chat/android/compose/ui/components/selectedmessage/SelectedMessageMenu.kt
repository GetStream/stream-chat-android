package io.getstream.chat.android.compose.ui.components.selectedmessage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
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
import io.getstream.chat.android.compose.state.messages.list.MessageOptionState
import io.getstream.chat.android.compose.ui.components.messageoptions.MessageOptions
import io.getstream.chat.android.compose.ui.components.messageoptions.defaultMessageOptionsState
import io.getstream.chat.android.compose.ui.components.reactionoptions.ReactionOptions
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * A Composable function that turns [SelectedMessageOptions] into a menu by wrapping it inside of [Surface]
 *
 * @param message used to gather the necessary message information (such as ownership, id, etc.) in order to properly display [SelectedMessageOptions]
 * @param modifier Compose UI [Modifier] that is applied to the internally used [Surface]
 * @param shape the shape applied to the internally used [Surface]
 * @param overlayColor the color applied to the internally used [Box] that wraps [Surface]
 * @param reactionTypes by default used to display all the available reaction options inside of [headerContent]
 * @param messageOptions by default used to display all available message options inside of [bodyContent]
 * @param onMessageAction by default a Handler used to propagate click events on individual reaction and message option elements
 * inside of the header and body content
 * @param onDismiss Handler called when the menu is dismissed
 * @param headerContent leading vertical Composable slot
 * @param bodyContent trailing vertical Composable slot
 */
@Composable
public fun SelectedMessageMenu(
    message: Message,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.bottomSheet,
    overlayColor: Color = ChatTheme.colors.overlay,
    reactionTypes: Map<String, Int> = ChatTheme.reactionTypes,
    messageOptions: List<MessageOptionState>,
    onMessageAction: (MessageAction) -> Unit,
    onDismiss: () -> Unit = {},
    headerContent: @Composable ColumnScope.() -> Unit = {
        ReactionOptions(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
    Box(modifier = Modifier
        .background(overlayColor)
        .fillMaxSize()
        .clickable(
            onClick = onDismiss,
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        )) {
        Surface(
            modifier = modifier
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            shape = shape) {
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
}

/**
 * Preview of [SelectedMessageMenu]
 */
@Preview(showBackground = true, name = "SelectedMessageMenu Preview")
@Composable
private fun SelectedMessageMenuPreview() {
    ChatTheme {
        val messageOptionsStateList = defaultMessageOptionsState(selectedMessage = Message(),
            currentUser = User(),
            isInThread = false)

        SelectedMessageMenu(message = Message(), messageOptions = messageOptionsStateList, onMessageAction = {})
    }
}