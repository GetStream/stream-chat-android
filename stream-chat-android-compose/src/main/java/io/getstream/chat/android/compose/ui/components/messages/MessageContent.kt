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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.client.utils.message.isGiphyEphemeral
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.ui.attachments.content.MessageAttachmentsContent
import io.getstream.chat.android.compose.ui.messages.list.DefaultMessageTextContent
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.state.messages.list.GiphyAction

/**
 * Represents the default message content within the bubble that can show different UI based on the message state.
 *
 * @param message The message to show.
 * @param currentUser The currently logged in user.
 * @param modifier Modifier for styling.
 * @param onLongItemClick Handler when the item is long clicked.
 * @param onGiphyActionClick Handler for Giphy actions.
 * @param onQuotedMessageClick Handler for quoted message click action.
 * @param onMediaGalleryPreviewResult Handler when the user selects an option in the Media Gallery Preview screen.
 * @param giphyEphemeralContent Composable that represents the default Giphy message content.
 * @param deletedMessageContent Composable that represents the default content of a deleted message.
 * @param regularMessageContent Composable that represents the default regular message content, such as attachments and
 * text.
 */
@Composable
public fun MessageContent(
    message: Message,
    currentUser: User?,
    modifier: Modifier = Modifier,
    onLongItemClick: (Message) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit = {},
    onQuotedMessageClick: (Message) -> Unit = {},
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    giphyEphemeralContent: @Composable () -> Unit = {
        DefaultMessageGiphyContent(
            message = message,
            onGiphyActionClick = onGiphyActionClick,
        )
    },
    deletedMessageContent: @Composable () -> Unit = {
        DefaultMessageDeletedContent(modifier = modifier)
    },
    regularMessageContent: @Composable () -> Unit = {
        DefaultMessageContent(
            message = message,
            currentUser = currentUser,
            onLongItemClick = onLongItemClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
            onQuotedMessageClick = onQuotedMessageClick,
        )
    },
) {
    when {
        message.isGiphyEphemeral() -> giphyEphemeralContent()
        message.isDeleted() -> deletedMessageContent()
        else -> regularMessageContent()
    }
}

/**
 * Represents the default ephemeral Giphy message content.
 *
 * @param message The message to show.
 * @param onGiphyActionClick Handler for Giphy actions.
 */
@Composable
internal fun DefaultMessageGiphyContent(
    message: Message,
    onGiphyActionClick: (GiphyAction) -> Unit,
) {
    GiphyMessageContent(
        message = message,
        onGiphyActionClick = onGiphyActionClick,
    )
}

/**
 * Represents the default deleted message content.
 *
 * @param modifier Modifier for styling.
 */
@Composable
internal fun DefaultMessageDeletedContent(
    modifier: Modifier,
) {
    Text(
        modifier = modifier
            .padding(
                start = 12.dp,
                end = 12.dp,
                top = 8.dp,
                bottom = 8.dp,
            ),
        text = stringResource(id = R.string.stream_compose_message_deleted),
        color = ChatTheme.colors.textLowEmphasis,
        style = ChatTheme.typography.footnoteItalic,
    )
}

/**
 * Represents the default regular message content that can contain attachments and text.
 *
 * @param message The message to show.
 * @param onLongItemClick Handler when the item is long clicked.
 * @param onMediaGalleryPreviewResult Handler when the user selects an option in the Media Gallery Preview screen.
 * @param onQuotedMessageClick Handler for quoted message click action.
 */
@Composable
internal fun DefaultMessageContent(
    message: Message,
    currentUser: User?,
    onLongItemClick: (Message) -> Unit,
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    onQuotedMessageClick: (Message) -> Unit,
) {
    Column {
        MessageAttachmentsContent(
            message = message,
            onLongItemClick = onLongItemClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
        )

        if (message.text.isNotEmpty()) {
            DefaultMessageTextContent(
                message = message,
                currentUser = currentUser,
                onLongItemClick = onLongItemClick,
                onQuotedMessageClick = onQuotedMessageClick,
            )
        }
    }
}
