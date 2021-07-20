package io.getstream.chat.android.compose.ui.messages.overlay

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.image
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.state.messages.list.MessageAction
import io.getstream.chat.android.compose.state.messages.list.MessageOption
import io.getstream.chat.android.compose.state.messages.list.React
import io.getstream.chat.android.compose.state.messages.reaction.ReactionOption
import io.getstream.chat.android.compose.ui.components.Avatar
import io.getstream.chat.android.compose.ui.components.MessageBubble
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageContainer
import io.getstream.chat.android.compose.ui.messages.list.MessageText
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * The overlays that's shown when the user selects a message, in the ConversationScreen.
 *
 * It shows various message options, as well as reactions, that the user can take. It also shows the
 * currently selected message in the middle of these options.
 *
 * @param reactionOptions - The options the user can send, to react to messages.
 * @param messageOptions - The [MessageOption] the user can select to trigger on the message.
 * @param message - Selected message.
 * @param onMessageAction - Handler for any of the available message actions (options + reactions).
 * @param onDismiss - Handler when the user dismisses the UI.
 * */
@ExperimentalFoundationApi
@Composable
fun SelectedMessageOverlay(
    reactionOptions: List<ReactionOption>,
    messageOptions: List<MessageOption>,
    message: Message,
    onMessageAction: (MessageAction) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray.copy(alpha = 0.7f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) { onDismiss() },
        contentAlignment = Alignment.CenterStart
    ) {
        Column(Modifier.padding(16.dp)) {
            ReactionOptions(
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

            DefaultMessageContainer(message = message, {}, null)

            Spacer(modifier = Modifier.size(8.dp))

            MessageOptions(
                options = messageOptions,
                onMessageAction = onMessageAction
            )
        }
    }
}

/**
 * The UI for a basic text message that's selected. It doesn't have all the components as a message
 * in the list, as those are not as important.
 *
 * @param message - Message to show.
 * */
@ExperimentalFoundationApi
@Composable
private fun SelectedTextMessage(message: Message) {
    Row(
        Modifier.widthIn(max = 300.dp)
    ) {
        val authorImage = rememberImagePainter(data = message.user.image)

        Avatar(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)
                .size(24.dp)
                .align(Alignment.Bottom),
            painter = authorImage
        )

        Column(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(start = 4.dp)
        ) {
            MessageBubble(content = { MessageText(message = message) })
        }
    }
}

/**
 * A row of selectable reactions on a message. Users can provide their own, or use the default.
 *
 * @param options - The options to show.
 * @param onReactionClick - Handler when the user clicks on any reaction.
 * */
@Composable
internal fun ReactionOptions(
    options: List<ReactionOption>,
    onReactionClick: (ReactionOption) -> Unit
) {
    Surface(shape = RoundedCornerShape(16.dp)) {
        LazyRow(
            modifier = Modifier
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
 * @param option - The reaction to show.
 * @param onReactionClick - Handler when the user clicks on the reaction.
 * */
@Composable
internal fun ReactionOptionItem(
    option: ReactionOption,
    onReactionClick: (ReactionOption) -> Unit,
) {
    Text(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false),
                onClick = { onReactionClick(option) }
            )
            .padding(2.dp),
        text = option.emoji
    )
}

/**
 * List of options the user can choose from, when selecting a message.
 *
 * @param options - The options to show, default or user-provided.
 * @param onMessageAction - Handler when the user selects an action.
 * @param modifier - Modifier for styling.
 * */
@Composable
internal fun MessageOptions(
    options: List<MessageOption>,
    onMessageAction: (MessageAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(shape = RoundedCornerShape(16.dp)) {
        LazyColumn(modifier) {
            items(options) { option ->
                MessageOptionItem(
                    option = option,
                    onMessageOptionClick = { onMessageAction(it.action) })
            }
        }
    }
}

/**
 * Each option item in the column of options.
 *
 * @param option - The option to show.
 * @param onMessageOptionClick - Handler when the user selects the option.
 * */
@Composable
internal fun MessageOptionItem(
    option: MessageOption,
    onMessageOptionClick: (MessageOption) -> Unit
) {
    val title = stringResource(id = option.title)

    Row(
        Modifier
            .fillMaxWidth()
            .clickable(
                onClick = { onMessageOptionClick(option) },
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple()
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = option.icon,
            tint = option.iconColor,
            contentDescription = title
        )

        Text(
            modifier = Modifier.padding(start = 12.dp),
            fontSize = 12.sp,
            text = title,
            fontWeight = FontWeight.Bold,
            color = option.titleColor
        )
    }
}