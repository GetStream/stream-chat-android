package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.rememberImagePainter
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.state.messages.MessageAlignment
import io.getstream.chat.android.compose.ui.components.avatar.Avatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents a number of participants in the thread.
 *
 * @param participants The list of participants in the thread.
 * @param alignment The alignment of the parent message.
 * @param modifier Modifier for styling.
 * @param borderStroke The border of user avatars, for visibility.
 * @param participantsLimit The limit of the number of participants shown in the component.
 */
@Composable
public fun ThreadParticipants(
    participants: List<User>,
    alignment: MessageAlignment,
    modifier: Modifier = Modifier,
    borderStroke: BorderStroke = BorderStroke(width = 1.dp, color = ChatTheme.colors.appBackground),
    participantsLimit: Int = DEFAULT_PARTICIPANTS_LIMIT,
) {
    Box(modifier) {
        /**
         * If we're aligning the message to the start, we just show items as they are, if we are showing them from the
         * end, then we need to reverse the order.
         */
        val participantsToShow = participants.take(participantsLimit).let {
            if (alignment == MessageAlignment.End) {
                it.reversed()
            } else {
                it
            }
        }
        val itemSize = ChatTheme.dimens.threadParticipantItemSize

        participantsToShow.forEachIndexed { index, user ->
            val painter = rememberImagePainter(data = user.image)
            val itemPadding = Modifier.padding(start = (index * (itemSize.value / 2)).dp)

            /**
             * Calculates the visual position of the item to define its zIndex. If we're aligned to the start of the screen,
             * the first item should be the upper most.
             *
             * If we're aligned to the end, then the last item should be the upper most.
             */
            val itemPosition = if (alignment == MessageAlignment.Start) {
                participantsLimit - index
            } else {
                index + 1
            }.toFloat()

            Avatar(
                modifier = itemPadding
                    .zIndex(itemPosition)
                    .size(itemSize)
                    .border(border = borderStroke, shape = ChatTheme.shapes.avatar),
                painter = painter
            )
        }
    }
}

private const val DEFAULT_PARTICIPANTS_LIMIT = 4
