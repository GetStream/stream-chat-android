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

package io.getstream.chat.android.compose.ui.components.composer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.ComposerCancelIcon
import io.getstream.chat.android.compose.ui.components.messages.QuotedMessageBody
import io.getstream.chat.android.compose.ui.components.messages.rememberBodyBuilder
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.models.Message

@Composable
internal fun MessageComposerEditIndicator(
    message: Message,
    modifier: Modifier = Modifier,
    onCancelClick: () -> Unit = {},
) {
    val bodyBuilder = rememberBodyBuilder()
    val body = remember(bodyBuilder, message) { bodyBuilder.build(message, null) }

    Box(modifier) {
        EditIndicatorCard(body)

        ComposerCancelIcon(
            modifier = Modifier
                .testTag("Stream_ComposerCancelEditButton")
                .align(Alignment.TopEnd)
                .offset(StreamTokens.spacing2xs, -StreamTokens.spacing2xs),
            onClick = onCancelClick,
        )
    }
}

@Composable
private fun EditIndicatorCard(body: QuotedMessageBody) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = ChatTheme.colors.chatBgOutgoing,
                shape = RoundedCornerShape(StreamTokens.radiusLg),
            )
            .padding(StreamTokens.spacingXs)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
    ) {
        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = StreamTokens.spacing3xs),
            thickness = 2.dp,
            color = ChatTheme.colors.chatReplyIndicatorOutgoing,
        )

        Column(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .defaultMinSize(minHeight = 40.dp)
                .weight(1f)
                .padding(end = StreamTokens.spacingMd),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.stream_compose_edit_message),
                style = ChatTheme.typography.metadataEmphasis,
                color = ChatTheme.colors.chatTextIncoming,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            EditIndicatorSubtitle(body)
        }
    }
}

@Composable
private fun EditIndicatorSubtitle(body: QuotedMessageBody) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        body.iconId?.let { iconId ->
            Icon(
                painter = painterResource(iconId),
                contentDescription = null,
                tint = ChatTheme.colors.chatTextIncoming,
            )
        }

        Text(
            modifier = Modifier
                .weight(1f)
                .testTag("Stream_EditMessagePreview"),
            text = body.text,
            style = ChatTheme.typography.metadataDefault,
            color = ChatTheme.colors.chatTextIncoming,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview
@Composable
private fun MessageComposerEditIndicatorPreview() {
    ChatTheme {
        MessageComposerEditIndicator(
            message = Message(text = "I think this could work"),
        )
    }
}
