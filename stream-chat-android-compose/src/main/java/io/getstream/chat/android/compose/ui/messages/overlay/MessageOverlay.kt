package io.getstream.chat.android.compose.ui.messages.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.state.MessageAction
import io.getstream.chat.android.common.state.React
import io.getstream.chat.android.compose.state.messages.reaction.ReactionOptionItemState
import io.getstream.chat.android.compose.ui.attachments.content.MessageAttachmentsContent
import io.getstream.chat.android.compose.ui.common.MessageBubble
import io.getstream.chat.android.compose.ui.common.avatar.Avatar
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageContent
import io.getstream.chat.android.compose.ui.messages.list.DeletedMessageContent
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme.reactionTypes

/**
 * The overlays that's shown for a message on the message list screen.
 *
 * @param message The message we are showing the overlay for.
 * @param horizontalAlignment The horizontal alignment of the composable slots.
 * @param onDismiss Handler when the user dismisses the UI.
 * @param headerContent The content shown at the top of a message overlay.
 * @param centerContent The content shown at the center of a message overlay.
 * @param footerContent The content shown at the bottom of a message overlay.
 */
@Composable
public fun MessageOverlay(
    message: Message,
    horizontalAlignment: Alignment.Horizontal,
    onDismiss: () -> Unit,
    headerContent: @Composable ColumnScope.(Message) -> Unit,
    centerContent: @Composable ColumnScope.(Message) -> Unit,
    footerContent: @Composable ColumnScope.(Message) -> Unit,
) {
    val boxScrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatTheme.colors.overlay)
            .verticalScroll(boxScrollState)
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = horizontalAlignment
        ) {
            headerContent(message)

            centerContent(message)

            footerContent(message)
        }
    }
}

/**
 * Represents the default header content of the message overlay.
 *
 * @param message The message we are showing the overlay for.
 * @param onMessageAction Handler for reaction actions.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun DefaultMessageOverlayHeaderContent(
    message: Message,
    onMessageAction: (MessageAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ownReactions = message.ownReactions

    val reactionOptions = reactionTypes.entries
        .map { (type, drawable) ->
            ReactionOptionItemState(
                painter = painterResource(drawable),
                isSelected = ownReactions.any { it.type == type },
                type = type
            )
        }

    ReactionOptions(
        modifier = modifier,
        options = reactionOptions,
        onReactionClick = {
            onMessageAction(
                React(
                    Reaction(
                        messageId = message.id,
                        type = it.type,
                    ),
                    message
                )
            )
        }
    )

    Spacer(modifier = Modifier.size(8.dp))
}

/**
 * Represent the default center content of the message overlay.
 *
 * @param message The message we are showing the overlay for.
 * @param currentUser The currently logged in user.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun DefaultMessageOverlayCenterContent(
    message: Message,
    currentUser: User?,
    modifier: Modifier = Modifier,
) {
    val isMine = message.user.id == currentUser?.id

    SelectedMessage(
        modifier = modifier,
        message = message,
        isMine = isMine
    )
}

/**
 * A row of selectable reactions on a message. Users can provide their own, or use the default.
 *
 * @param options The options to show.
 * @param onReactionClick Handler when the user clicks on any reaction.
 * @param modifier Modifier for styling.
 */
@Composable
private fun ReactionOptions(
    options: List<ReactionOptionItemState>,
    onReactionClick: (ReactionOptionItemState) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ChatTheme.colors.barsBackground,
    ) {
        LazyRow(
            modifier = modifier
                .wrapContentWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.width(2.dp)) }
            items(options) { option ->
                ReactionOptionItem(option = option, onReactionClick = onReactionClick)
            }

            item { Spacer(modifier = Modifier.width(2.dp)) }
        }
    }
}

/**
 * Each reaction item in the row of reactions.
 *
 * @param option The reaction to show.
 * @param onReactionClick Handler when the user clicks on the reaction.
 */
@Composable
private fun ReactionOptionItem(
    option: ReactionOptionItemState,
    onReactionClick: (ReactionOptionItemState) -> Unit,
) {
    Icon(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false),
                onClick = { onReactionClick(option) }
            )
            .padding(2.dp),
        painter = option.painter,
        contentDescription = option.type,
        tint = if (option.isSelected) ChatTheme.colors.primaryAccent else ChatTheme.colors.textLowEmphasis
    )
}

/**
 * The UI for a basic text message that's selected. It doesn't have all the components as a message
 * in the list, as those are not as important.
 *
 * @param message The message to show.
 * @param isMine If the message is owned by the current user.
 * @param modifier Modifier for styling.
 */
@Composable
private fun SelectedMessage(
    message: Message,
    isMine: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.widthIn(max = 300.dp)
    ) {
        val authorImage = rememberImagePainter(data = message.user.image)

        if (!isMine) {
            Avatar(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .size(24.dp)
                    .align(Alignment.Bottom),
                painter = authorImage
            )
        }

        val messageBubbleShape = if (isMine) ChatTheme.shapes.myMessageBubble else ChatTheme.shapes.otherMessageBubble
        val messageBubbleColor =
            if (isMine) ChatTheme.colors.ownMessagesBackground else ChatTheme.colors.otherMessagesBackground

        MessageBubble(
            shape = messageBubbleShape,
            color = messageBubbleColor,
            content = {
                if (message.deletedAt != null) {
                    DeletedMessageContent()
                } else {
                    Column {
                        MessageAttachmentsContent(message = message, onLongItemClick = {})

                        if (message.text.isNotEmpty()) {
                            DefaultMessageContent(message = message)
                        }
                    }
                }
            }
        )

        if (isMine) {
            Avatar(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .size(24.dp)
                    .align(Alignment.Bottom),
                painter = authorImage
            )
        }
    }
}
