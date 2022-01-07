package io.getstream.chat.android.compose.ui.components.reactionpicker

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
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
import io.getstream.chat.android.compose.previewdata.PreviewMessageData
import io.getstream.chat.android.compose.ui.components.SimpleMenu
import io.getstream.chat.android.compose.ui.components.reactionoptions.ExtendedReactionsOptions
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Displays all of the available reactions the user can set on a message.
 *
 * @param message The selected message.
 * @param onMessageAction Handler that propagates click events on each item.
 * @param modifier Modifier for styling.
 * @param shape Changes the shape of [ReactionsPicker].
 * @param overlayColor The color applied to the overlay.
 * @param cells Describes the way cells are formed inside [ExtendedReactionsOptions].
 * @param onDismiss Handler called when the menu is dismissed.
 * @param reactionTypes The available reactions.
 * @param headerContent The content shown on the top of [ReactionsPicker]. By default empty.
 * @param centerContent The content shown at the center of [ReactionsPicker].
 * By default displays all available reactions.
 */
@ExperimentalFoundationApi
@Composable
public fun ReactionsPicker(
    message: Message,
    onMessageAction: (MessageAction) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.bottomSheet,
    overlayColor: Color = ChatTheme.colors.overlay,
    cells: GridCells = GridCells.Fixed(5),
    onDismiss: () -> Unit = {},
    reactionTypes: Map<String, Int> = ChatTheme.reactionTypes,
    headerContent: @Composable ColumnScope.() -> Unit = {},
    centerContent: @Composable ColumnScope.() -> Unit = {
        DefaultReactionsPickerCenterContent(
            message = message,
            onMessageAction = onMessageAction,
            cells = cells,
            reactionTypes = reactionTypes
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

/**
 * The Default center content for the [ReactionsPicker]. Shows all available reactions.
 *
 * @param message The selected message.
 * @param onMessageAction Handler that propagates click events on each item.
 * @param cells Describes the way cells are formed inside [ExtendedReactionsOptions].
 * @param reactionTypes The available reactions.
 */
@ExperimentalFoundationApi
@Composable
internal fun DefaultReactionsPickerCenterContent(
    message: Message,
    onMessageAction: (MessageAction) -> Unit,
    cells: GridCells = GridCells.Fixed(5),
    reactionTypes: Map<String, Int> = ChatTheme.reactionTypes
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
        },
        cells = cells
    )
}

/**
 * Preview of [ReactionsPicker] with a reaction selected.
 */
@ExperimentalFoundationApi
@Preview(showBackground = true, name = "ReactionPicker Preview")
@Composable
internal fun ReactionPickerPreview() {
    ChatTheme {
        ReactionsPicker(
            message = PreviewMessageData.messageWithOwnReaction,
            onMessageAction = {}
        )
    }
}
