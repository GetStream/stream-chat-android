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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.state.messages.MessageAlignment
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatarStack
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.applyIf
import io.getstream.chat.android.models.User
import kotlin.math.round

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
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
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

private val ConnectorWidth = 12.dp
private val ConnectorHeight = 36.dp
private val ConnectorExtension = 10.dp

@Composable
@Suppress("MagicNumber")
private fun ThreadConnector(alignment: MessageAlignment) {
    val mirrored = alignment == MessageAlignment.End
    val color = if (mirrored) {
        ChatTheme.colors.chatThreadConnectorOutgoing
    } else {
        ChatTheme.colors.chatThreadConnectorIncoming
    }

    Canvas(
        modifier = Modifier
            .size(width = ConnectorWidth, height = ConnectorHeight)
            .graphicsLayer { translationY = -StreamTokens.spacingXs.toPx() }
            .applyIf(mirrored) { graphicsLayer(scaleX = -1f) },
    ) {
        val strokeWidth = round(1.dp.toPx())
        val extension = ConnectorExtension.toPx()
        val w = size.width
        val h = size.height

        // Make sure the line sits on pixel boundaries to avoid blurriness
        val isOddStroke = strokeWidth.toInt() % 2 != 0
        val lineX = round(w * 0.03f) + if (isOddStroke) 0.5f else 0f

        val path = Path().apply {
            moveTo(lineX, -extension)
            lineTo(lineX, h * 0.43f)
            cubicTo(
                x1 = lineX,
                y1 = h * 0.6f,
                x2 = w * 0.47f,
                y2 = h * 0.75f,
                x3 = w,
                y3 = h * 0.75f,
            )
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

@Composable
private fun ThreadParticipants(participants: List<User>) {
    UserAvatarStack(
        modifier = Modifier.testTag("Stream_ThreadParticipantAvatar"),
        overlap = StreamTokens.spacingXs,
        users = participants.take(MaxThreadParticipants),
        avatarSize = AvatarSize.Small,
        showBorder = true,
    )
}

private const val MaxThreadParticipants = 3
