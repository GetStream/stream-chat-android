/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.attachments.content.MessageAttachmentsContent
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.ui.common.state.messages.list.CancelGiphy
import io.getstream.chat.android.ui.common.state.messages.list.GiphyAction
import io.getstream.chat.android.ui.common.state.messages.list.SendGiphy
import io.getstream.chat.android.ui.common.state.messages.list.ShuffleGiphy

/**
 * Represents the content of an ephemeral giphy message.
 *
 * @param message The ephemeral giphy message.
 * @param currentUser The current user that's logged in.
 * @param modifier Modifier for styling.
 * @param onGiphyActionClick Handler when the user clicks on action button.
 */
@Suppress("LongMethod")
@Composable
public fun GiphyMessageContent(
    message: Message,
    currentUser: User?,
    modifier: Modifier = Modifier,
    onGiphyActionClick: (GiphyAction) -> Unit = {},
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = R.drawable.stream_compose_ic_giphy),
                contentDescription = null,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(id = R.string.stream_compose_message_list_giphy_title),
                style = ChatTheme.typography.bodyBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = ChatTheme.colors.textHighEmphasis,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = message.text,
                style = ChatTheme.typography.body,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = ChatTheme.colors.textLowEmphasis,
            )
        }

        MessageAttachmentsContent(
            message = message,
            currentUser = currentUser,
            onLongItemClick = {},
            onMediaGalleryPreviewResult = {},
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(color = ChatTheme.colors.borders),
        )

        Row(
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GiphyButton(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                text = stringResource(id = R.string.stream_compose_message_list_giphy_cancel),
                textColor = ChatTheme.colors.textLowEmphasis,
                onClick = { onGiphyActionClick(CancelGiphy(message)) },
            )

            Spacer(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(color = ChatTheme.colors.borders),
            )

            GiphyButton(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                text = stringResource(id = R.string.stream_compose_message_list_giphy_shuffle),
                textColor = ChatTheme.colors.textLowEmphasis,
                onClick = { onGiphyActionClick(ShuffleGiphy(message)) },
            )

            Spacer(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(color = ChatTheme.colors.borders),
            )

            GiphyButton(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                text = stringResource(id = R.string.stream_compose_message_list_giphy_send),
                textColor = ChatTheme.colors.primaryAccent,
                onClick = { onGiphyActionClick(SendGiphy(message)) },
            )
        }
    }
}

/**
 * Represents an action button in the ephemeral giphy message.
 *
 * @param text The text displayed on the button.
 * @param textColor The color applied to the text.
 * @param onClick Handler when the user clicks on action button.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun GiphyButton(
    text: String,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.Center)
                .testTag("Stream_GiphyButton_$text"),
            text = text,
            style = ChatTheme.typography.bodyBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = textColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun GiphyMessageContentPreview() {
    ChatTheme {
        GiphyMessageContent(
            modifier = Modifier.size(600.dp),
            message = PreviewMessageData.message1,
            currentUser = PreviewMessageData.message1.user,
            onGiphyActionClick = {},
        )
    }
}
