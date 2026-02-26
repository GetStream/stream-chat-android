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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.MessageAlignment
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatarStack
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.applyIf
import io.getstream.chat.android.models.User

/**
 * Shows a row of participants in the message thread, if they exist.
 *
 * @param participants List of users in the thread.
 * @param text Text of the label.
 * @param messageAlignment The alignment of the message, used for the content orientation.
 * @param modifier Modifier for styling.
 */
@Composable
public fun MessageThreadFooter(
    participants: List<User>,
    text: String,
    messageAlignment: MessageAlignment,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.clipToBounds(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs)
    ) {
        if (messageAlignment == MessageAlignment.Start) {
            ThreadConnector(messageAlignment)
            ThreadParticipants(participants)
        }

        Text(
            modifier = Modifier.testTag("Stream_ThreadRepliesLabel"),
            text = text,
            style = ChatTheme.typography.captionEmphasis,
            color = ChatTheme.colors.chatTextLink,
        )

        if (messageAlignment == MessageAlignment.End) {
            ThreadParticipants(participants)
            ThreadConnector(messageAlignment)
        }
    }
}

@Composable
private fun ThreadConnector(alignment: MessageAlignment) {
    val mirrored = alignment == MessageAlignment.End
    val tint = if (mirrored) {
        ChatTheme.colors.chatThreadConnectorOutgoing
    } else {
        ChatTheme.colors.chatThreadConnectorIncoming
    }
    Image(
        modifier = Modifier
            .offset(y = -StreamTokens.spacingXs)
            .applyIf(mirrored) { graphicsLayer(scaleX = -1f) },
        painter = painterResource(id = R.drawable.stream_compose_thread_connector),
        contentDescription = null,
        colorFilter = ColorFilter.tint(tint),
    )
}

@Composable
private fun ThreadParticipants(participants: List<User>) {
    UserAvatarStack(
        overlap = StreamTokens.spacingXs,
        users = participants.take(MaxThreadParticipants),
        avatarSize = AvatarSize.Small,
        showBorder = true,
    )
}

private const val MaxThreadParticipants = 3
