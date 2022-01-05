package io.getstream.chat.android.compose.ui.components.selectedmessage

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.state.MessageAction
import io.getstream.chat.android.common.state.React
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.previewdata.PreviewReactionData
import io.getstream.chat.android.compose.previewdata.PreviewUserData
import io.getstream.chat.android.compose.state.userreactions.UserReactionItemState
import io.getstream.chat.android.compose.ui.components.SimpleMenu
import io.getstream.chat.android.compose.ui.components.reactionoptions.ReactionOptions
import io.getstream.chat.android.compose.ui.components.userreactions.UserReactions
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents the list of user reactions.
 *
 * @param message The selected message.
 * @param currentUser The currently logged in user.
 * @param onMessageAction Handler that propagates click events on each item.
 * @param onShowMoreReactionsSelected Handler that propagates clicks on the show more reactions button.
 * @param modifier Modifier for styling.
 * @param shape Changes the shape of [SelectedReactionsMenu].
 * @param overlayColor The color applied to the overlay.
 * @param reactionTypes The available reactions within the menu.
 * @param showMoreReactionsDrawableRes Drawable resource used for the show more button.
 * @param onDismiss Handler called when the menu is dismissed.
 * @param headerContent The content shown at the top of the [SelectedReactionsMenu] dialog. By default [ReactionOptions].
 * @param centerContent The content shown in the [SelectedReactionsMenu] dialog. By Default [UserReactions].
 */
@Composable
public fun SelectedReactionsMenu(
    message: Message,
    currentUser: User?,
    onMessageAction: (MessageAction) -> Unit,
    onShowMoreReactionsSelected: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.bottomSheet,
    overlayColor: Color = ChatTheme.colors.overlay,
    reactionTypes: Map<String, Int> = ChatTheme.reactionTypes,
    @DrawableRes showMoreReactionsDrawableRes: Int = R.drawable.stream_compose_ic_more,
    onDismiss: () -> Unit = {},
    headerContent: @Composable ColumnScope.() -> Unit = {
        DefaultSelectedReactionsHeaderContent(
            message = message,
            reactionTypes = reactionTypes,
            showMoreReactionsDrawableRes = showMoreReactionsDrawableRes,
            onMessageAction = onMessageAction,
            onShowMoreReactionsSelected = onShowMoreReactionsSelected
        )
    },
    centerContent: @Composable ColumnScope.() -> Unit = {
        DefaultSelectedReactionsCenterContent(
            message = message,
            currentUser = currentUser
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
 * Default header content for the selected reactions menu.
 *
 * @param message The selected message.
 * @param reactionTypes Available reactions.
 * @param showMoreReactionsDrawableRes Drawable resource used for the show more button.
 * @param onMessageAction Handler when the user selects a reaction.
 * @param onShowMoreReactionsSelected Handler that propagates clicks on the show more button.
 */
@Composable
internal fun DefaultSelectedReactionsHeaderContent(
    message: Message,
    reactionTypes: Map<String, Int>,
    @DrawableRes showMoreReactionsDrawableRes: Int = R.drawable.stream_compose_ic_more,
    onMessageAction: (MessageAction) -> Unit,
    onShowMoreReactionsSelected: () -> Unit,
) {
    ReactionOptions(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 20.dp),
        reactionTypes = reactionTypes,
        showMoreReactionsDrawableRes = showMoreReactionsDrawableRes,
        onReactionOptionSelected = {
            onMessageAction(
                React(
                    reaction = Reaction(messageId = message.id, type = it.type),
                    message = message
                )
            )
        },
        onShowMoreReactionsSelected = onShowMoreReactionsSelected,
        ownReactions = message.ownReactions
    )
}

/**
 * Default center content for the selected reactions menu.
 *
 * @param message The selected message.
 * @param currentUser The currently logged in user.
 */
@Composable
internal fun DefaultSelectedReactionsCenterContent(
    message: Message,
    currentUser: User?,
) {
    UserReactions(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = ChatTheme.dimens.userReactionsMaxHeight)
            .padding(vertical = 16.dp),
        items = buildUserReactionItems(
            message = message,
            currentUser = currentUser
        )
    )
}

/**
 * Builds a list of user reactions, based on the current user and the selected message.
 *
 * @param message The message the reactions were left for.
 * @param currentUser The currently logged in user.
 * @param reactionTypes The available reactions within the menu.
 */
@Composable
private fun buildUserReactionItems(
    message: Message,
    currentUser: User?,
    reactionTypes: Map<String, Int> = ChatTheme.reactionTypes,
): List<UserReactionItemState> {
    return message.latestReactions
        .filter { it.user != null && reactionTypes.contains(it.type) }
        .map {
            val user = requireNotNull(it.user)
            val type = it.type
            val resId = requireNotNull(reactionTypes[type])

            UserReactionItemState(
                user = user,
                painter = painterResource(resId),
                isMine = currentUser?.id == user.id,
                type = type
            )
        }
}

/**
 * Preview of the [SelectedReactionsMenu] component with 1 reaction.
 */
@Preview
@Composable
private fun OneSelectedReactionMenuPreview() {
    ChatTheme {
        val message = Message(latestReactions = PreviewReactionData.oneReaction.toMutableList())

        SelectedReactionsMenu(
            message = message,
            currentUser = PreviewUserData.user1,
            onMessageAction = {},
            onShowMoreReactionsSelected = {}
        )
    }
}

/**
 * Preview of the [SelectedReactionsMenu] component with many reactions.
 */
@Preview
@Composable
private fun ManySelectedReactionsMenuPreview() {
    ChatTheme {
        val message = Message(latestReactions = PreviewReactionData.manyReaction.toMutableList())

        SelectedReactionsMenu(
            message = message,
            currentUser = PreviewUserData.user1,
            onMessageAction = {},
            onShowMoreReactionsSelected = {}
        )
    }
}
