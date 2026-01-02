/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import io.getstream.chat.android.compose.state.messages.MessageAlignment
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.utils.extensions.initials

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
    participantsLimit: Int = DefaultParticipantsLimit,
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
            val itemPadding = Modifier.padding(start = (index * (itemSize.value / 2)).dp)

            /**
             * Calculates the visual position of the item to define its zIndex. If we're aligned to the start of the
             * screen, the first item should be the upper most.
             *
             * If we're aligned to the end, then the last item should be the upper most.
             */
            val itemPosition = if (alignment == MessageAlignment.Start) {
                participantsLimit - index
            } else {
                index + 1
            }.toFloat()

            ChatTheme.componentFactory.Avatar(
                modifier = itemPadding
                    .zIndex(itemPosition)
                    .size(itemSize)
                    .border(border = borderStroke, shape = ChatTheme.shapes.avatar)
                    .testTag("Stream_ThreadParticipantAvatar"),
                imageUrl = user.image,
                initials = user.initials,
                shape = ChatTheme.shapes.avatar,
                textStyle = ChatTheme.typography.captionBold.copy(fontSize = 7.sp),
                placeholderPainter = null,
                errorPlaceholderPainter = null,
                contentDescription = user.name,
                initialsAvatarOffset = DpOffset.Zero,
                onClick = null,
            )
        }
    }
}

/**
 * The max limit of how many users are shown as participants in a thread.
 */
private const val DefaultParticipantsLimit = 4
