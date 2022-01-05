package io.getstream.chat.android.compose.ui.components.reactionpicker

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.common.state.MessageAction
import io.getstream.chat.android.common.state.React
import io.getstream.chat.android.compose.ui.components.SimpleMenu
import io.getstream.chat.android.compose.ui.components.reactionoptions.ExtendedReactionsOptions
import io.getstream.chat.android.compose.ui.theme.ChatTheme

@ExperimentalFoundationApi
@Composable
public fun ReactionsPicker(
    message: Message,
    onMessageAction: (MessageAction) -> Unit,
    modifier: Modifier = Modifier,
    reactionTypes: Map<String, Int> = ChatTheme.reactionTypes,
    shape: Shape = ChatTheme.shapes.bottomSheet,
    overlayColor: Color = ChatTheme.colors.overlay,
    onDismiss: () -> Unit = {},
    headerContent: @Composable ColumnScope.() -> Unit = {},
    centerContent: @Composable ColumnScope.() -> Unit = {
        DefaultReactionsPickerCenterContent(
            reactionTypes = reactionTypes,
            message = message,
            onMessageAction = onMessageAction
        )
    },
) {
    SimpleMenu(
        modifier = modifier,
        shape = shape,
        overlayColor = overlayColor,
        headerContent = headerContent,
        centerContent = centerContent,
        onDismiss = onDismiss
    )
}

@ExperimentalFoundationApi
@Composable
internal fun DefaultReactionsPickerCenterContent(
    reactionTypes: Map<String, Int> = ChatTheme.reactionTypes,
    message: Message,
    onMessageAction: (MessageAction) -> Unit,
) {
    ExtendedReactionsOptions(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        reactionTypes = reactionTypes,
        ownReactions = message.ownReactions,
        onReactionOptionSelected = { reactionOptionItemState ->
            onMessageAction(
                React(
                    reaction = Reaction(messageId = message.id, reactionOptionItemState.type),
                    message = message
                )
            )
        }
    )
}

@ExperimentalFoundationApi
@Preview(showBackground = true, name = "ReactionPicker Preview")
@Composable
internal fun ReactionPickerPreview() {
    ChatTheme {
        ReactionsPicker(
            message = Message(),
            onMessageAction = {}
        )
    }
}
