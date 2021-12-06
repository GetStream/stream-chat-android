package io.getstream.chat.android.compose.ui.messages.overlay

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.state.MessageAction
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.common.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * The message reactions overlays that's shown when the user taps on message reactions.
 *
 * It shows the list of users reacted to this message, as well as reactions, that the user can take.
 * It also shows the currently selected message in the middle of these options.
 *
 * @param message Selected message.
 * @param currentUser The currently logged in user.
 * @param onMessageAction Handler for any of the available message actions (options + reactions).
 * @param onDismiss Handler when the user dismisses the UI.
 */
@Composable
public fun MessageReactionsOverlay(
    message: Message,
    currentUser: User?,
    onMessageAction: (MessageAction) -> Unit,
    onDismiss: () -> Unit,
) {
    val isMine = message.user.id == currentUser?.id

    MessageOverlay(
        message = message,
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
        onDismiss = onDismiss,
        headerContent = {
            DefaultMessageOverlayHeaderContent(
                message = message,
                onMessageAction = onMessageAction
            )
        },
        centerContent = {
            DefaultMessageOverlayCenterContent(
                message = message,
                currentUser = currentUser
            )
        },
        footerContent = {
            DefaultMessageReactionsOverlayFooterContent(
                message = message,
                currentUser = currentUser
            )
        }
    )
}

/**
 * Represent the default footer content of the message reactions overlay.
 *
 * @param message The message that contains the reactions.
 * @param currentUser The currently logged in user.
 */
@Composable
private fun DefaultMessageReactionsOverlayFooterContent(
    message: Message,
    currentUser: User?,
) {
    Spacer(modifier = Modifier.size(8.dp))

    ReactionsInfo(
        message = message,
        currentUser = currentUser
    )
}

/**
 *
 * Represent a section with a list of reactions left for the message.
 *
 * @param message The message that contains the reactions.
 * @param currentUser The currently logged in user.
 * @param modifier Modifier for styling.
 * @param maxColumns The maximum number of columns in the user reactions grid.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun ReactionsInfo(
    message: Message,
    currentUser: User?,
    modifier: Modifier = Modifier,
    maxColumns: Int = 4,
) {
    val reactions = message.latestReactions
    val reactionCount = reactions.size

    val title = LocalContext.current.resources.getQuantityString(
        R.plurals.stream_compose_message_reactions,
        reactionCount,
        reactionCount
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = ChatTheme.dimens.messageReactionsMaxHeight),
        shape = RoundedCornerShape(ChatTheme.dimens.messageReactionsRoundedCorners),
        color = ChatTheme.colors.barsBackground,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Text(
                text = title,
                style = ChatTheme.typography.title3Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = ChatTheme.colors.textHighEmphasis
            )

            Spacer(modifier = Modifier.height(16.dp))

            val columns = reactionCount.coerceAtMost(maxColumns)
            val reactionItemWidth = ChatTheme.dimens.messageReactionItemWidth
            val reactionGridWidth = reactionItemWidth * columns

            LazyVerticalGrid(
                modifier = Modifier.width(reactionGridWidth),
                cells = GridCells.Fixed(reactionCount.coerceAtMost(columns))
            ) {
                items(reactions) { reaction ->
                    UserReactionItem(
                        modifier = Modifier
                            .width(reactionItemWidth)
                            .padding(8.dp),
                        reaction = reaction,
                        currentUser = currentUser
                    )
                }
            }
        }
    }
}

/**
 * Represent a reaction with the user who left it.
 *
 * @param reaction The user reaction to display.
 * @param currentUser The currently logged in user.
 * @param modifier Modifier for styling.
 */
@Composable
public fun UserReactionItem(
    reaction: Reaction,
    currentUser: User?,
    modifier: Modifier = Modifier,
) {
    val user = reaction.user!!

    val isCurrentUser = currentUser?.id == user.id

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(modifier = Modifier.width(64.dp)) {
            UserAvatar(
                user = user,
                showOnlineIndicator = false,
                modifier = Modifier.size(64.dp)
            )

            Icon(
                modifier = Modifier
                    .background(shape = RoundedCornerShape(16.dp), color = ChatTheme.colors.barsBackground)
                    .size(24.dp)
                    .padding(4.dp)
                    .align(Alignment.BottomEnd),
                painter = painterResource(requireNotNull(ChatTheme.reactionTypes[reaction.type])),
                contentDescription = reaction.type,
                tint = if (isCurrentUser) ChatTheme.colors.primaryAccent else ChatTheme.colors.textLowEmphasis,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = user.name,
            style = ChatTheme.typography.footnoteBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = ChatTheme.colors.textHighEmphasis,
            textAlign = TextAlign.Center,
        )
    }
}
