package io.getstream.chat.android.compose.ui.components.selectedmessage

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
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
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messageoptions.MessageOptionItemState
import io.getstream.chat.android.compose.ui.components.SimpleMenu
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
 * @param onShowMoreReactionsSelected Handler that propagates clicks on the show more reactions button.
 * @param modifier Modifier for styling.
 * @param shape Changes the shape of [SelectedMessageMenu].
 * @param overlayColor The color applied to the overlay.
 * @param reactionTypes The available reactions within the menu.
 * @param showMoreReactionsIcon Drawable resource used for the show more button.
 * @param onDismiss Handler called when the menu is dismissed.
 * @param headerContent The content shown at the top of the [SelectedMessageMenu] dialog. By default [ReactionOptions].
 * @param centerContent The content shown at the center of the [SelectedMessageMenu] dialog. By Default [MessageOptions].
 */
@Composable
public fun SelectedMessageMenu(
    message: Message,
    messageOptions: List<MessageOptionItemState>,
    onMessageAction: (MessageAction) -> Unit,
    onShowMoreReactionsSelected: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.bottomSheet,
    overlayColor: Color = ChatTheme.colors.overlay,
    reactionTypes: Map<String, Int> = ChatTheme.reactionTypes,
    @DrawableRes showMoreReactionsIcon: Int = R.drawable.stream_compose_ic_more,
    onDismiss: () -> Unit = {},
    headerContent: @Composable ColumnScope.() -> Unit = {
        DefaultSelectedMessageReactionOptions(
            message = message,
            reactionTypes = reactionTypes,
            showMoreReactionsDrawableRes = showMoreReactionsIcon,
            onMessageAction = onMessageAction,
            showMoreReactionsIcon = onShowMoreReactionsSelected
        )
    },
    centerContent: @Composable ColumnScope.() -> Unit = {
        DefaultSelectedMessageOptions(
            messageOptions = messageOptions,
            onMessageAction = onMessageAction
        )
    },
) {
    SimpleMenu(
        modifier = modifier,
        shape = shape,
        overlayColor = overlayColor,
        onDismiss = onDismiss,
        headerContent = headerContent,
        centerContent = centerContent
    )
}

/**
 * Default reaction options for the selected message.
 *
 * @param message The selected message.
 * @param reactionTypes Available reactions.
 * @param showMoreReactionsDrawableRes Drawable resource used for the show more button.
 * @param onMessageAction Handler when the user selects a reaction.
 * @param showMoreReactionsIcon Handler that propagates clicks on the show more button.
 */
@Composable
internal fun DefaultSelectedMessageReactionOptions(
    message: Message,
    reactionTypes: Map<String, Int>,
    @DrawableRes showMoreReactionsDrawableRes: Int = R.drawable.stream_compose_ic_more,
    onMessageAction: (MessageAction) -> Unit,
    showMoreReactionsIcon: () -> Unit,
) {
    ReactionOptions(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 20.dp),
        reactionTypes = reactionTypes,
        showMoreReactionsIcon = showMoreReactionsDrawableRes,
        onReactionOptionSelected = {
            onMessageAction(
                React(
                    reaction = Reaction(messageId = message.id, type = it.type),
                    message = message
                )
            )
        },
        onShowMoreReactionsSelected = showMoreReactionsIcon,
        ownReactions = message.ownReactions,
    )
}

/**
 * Default selected message options.
 *
 * @param messageOptions The available options.
 * @param onMessageAction Handler when the user selects an option.
 */
@Composable
internal fun DefaultSelectedMessageOptions(
    messageOptions: List<MessageOptionItemState>,
    onMessageAction: (MessageAction) -> Unit,
) {
    MessageOptions(
        options = messageOptions,
        onMessageOptionSelected = {
            onMessageAction(it.action)
        }
    )
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

        SelectedMessageMenu(
            message = Message(),
            messageOptions = messageOptionsStateList,
            onMessageAction = {},
            onShowMoreReactionsSelected = {}
        )
    }
}
