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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.utils.attachment.isAudio
import io.getstream.chat.android.client.utils.attachment.isAudioRecording
import io.getstream.chat.android.client.utils.attachment.isFile
import io.getstream.chat.android.client.utils.attachment.isGiphy
import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.client.utils.message.isGiphyEphemeral
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.attachments.content.FileUploadContent
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageStyling
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.shouldBeDisplayedAsFullSizeAttachment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.state.messages.list.GiphyAction
import io.getstream.chat.android.ui.common.utils.extensions.hasLink
import io.getstream.chat.android.ui.common.utils.extensions.isUploading

/**
 * Represents the default message content within the bubble that can show different UI based on the message state.
 *
 * @param message The message to show.
 * @param currentUser The currently logged in user.
 * @param modifier Modifier for styling.
 * @param onLongItemClick Handler when the item is long clicked.
 * @param onGiphyActionClick Handler for Giphy actions.
 * @param onQuotedMessageClick Handler for quoted message click action.
 * @param onLinkClick Handler for clicking on a link in the message.
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
    onUserMentionClick: (User) -> Unit = {},
    onLinkClick: ((Message, String) -> Unit)? = null,
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    giphyEphemeralContent: @Composable () -> Unit = {
        ChatTheme.componentFactory.MessageGiphyContent(
            message = message,
            currentUser = currentUser,
            onGiphyActionClick = onGiphyActionClick,
        )
    },
    deletedMessageContent: @Composable () -> Unit = {
        ChatTheme.componentFactory.MessageDeletedContent(
            message = message,
            currentUser = currentUser,
            modifier = modifier,
        )
    },
    regularMessageContent: @Composable () -> Unit = {
        ChatTheme.componentFactory.MessageRegularContent(
            message = message,
            currentUser = currentUser,
            onLongItemClick = onLongItemClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
            onQuotedMessageClick = onQuotedMessageClick,
            onLinkClick = onLinkClick,
            onUserMentionClick = onUserMentionClick,
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
 * Represents the default deleted message content.
 *
 * @param modifier Modifier for styling.
 */
@Composable
internal fun DefaultMessageDeletedContent(
    message: Message,
    currentUser: User?,
    modifier: Modifier,
) {
    val contentColor = MessageStyling.textColor(outgoing = currentUser?.id == message.user.id)
    Row(
        modifier = modifier.padding(MessageStyling.contentPadding),
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.stream_compose_ic_block),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(16.dp),
        )
        Text(
            modifier = Modifier.testTag("Stream_MessageDeleted"),
            text = stringResource(id = R.string.stream_compose_message_deleted),
            color = contentColor,
            style = ChatTheme.typography.bodyDefault,
        )
    }
}

/**
 * Represents the default regular message content that can contain attachments and text.
 *
 * @param message The message to show.
 * @param onLongItemClick Handler when the item is long clicked.
 * @param onMediaGalleryPreviewResult Handler when the user selects an option in the Media Gallery Preview screen.
 * @param onQuotedMessageClick Handler for quoted message click action.
 * @param onLinkClick Handler for clicking on a link in the message.
 */
@Composable
@Suppress("LongMethod")
internal fun DefaultMessageContent(
    message: Message,
    currentUser: User?,
    onLongItemClick: (Message) -> Unit,
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
    onQuotedMessageClick: (Message) -> Unit,
    onUserMentionClick: (User) -> Unit = {},
    onLinkClick: ((Message, String) -> Unit)? = null,
) {
    val componentFactory = ChatTheme.componentFactory

    Column {
        val quotedMessage = message.replyTo
        if (quotedMessage != null) {
            componentFactory.MessageQuotedContent(
                modifier = Modifier,
                message = quotedMessage,
                currentUser = currentUser,
                replyMessage = message,
                onLongItemClick = onLongItemClick,
                onQuotedMessageClick = onQuotedMessageClick,
            )
        }

        val attachmentState = AttachmentState(
            message = message,
            isMine = message.user.id == currentUser?.id,
            onLongItemClick = onLongItemClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
        )
        val info = message.rememberMessageInfo()

        if (info.hasUploads) {
            FileUploadContent(
                modifier = Modifier,
                attachmentState = attachmentState,
            )
        } else {
            if (info.hasMedia) {
                componentFactory.MediaAttachmentContent(
                    modifier = Modifier,
                    state = attachmentState,
                )
            }

            if (info.hasGiphys) {
                componentFactory.GiphyAttachmentContent(
                    modifier = Modifier,
                    state = attachmentState,
                )
            }

            if (info.hasLinks) {
                componentFactory.LinkAttachmentContent(
                    modifier = Modifier,
                    state = attachmentState,
                )
            }

            if (info.hasFiles) {
                componentFactory.FileAttachmentContent(
                    modifier = Modifier,
                    state = attachmentState,
                )
            }

            if (info.hasRecordings) {
                componentFactory.AudioRecordAttachmentContent(
                    modifier = Modifier,
                    state = attachmentState,
                )
            }
        }

        if (info.hasUnknown) {
            componentFactory.CustomAttachmentContent(
                modifier = Modifier,
                state = attachmentState,
            )
        }

        if (message.text.isNotEmpty()) {
            componentFactory.MessageTextContent(
                modifier = Modifier,
                message = message,
                currentUser = currentUser,
                onLongItemClick = onLongItemClick,
                onLinkClick = onLinkClick,
                onUserMentionClick = onUserMentionClick,
            )
        }

        if (!info.displaysFullSizeAttachment && !info.hasUploads) {
            Spacer(Modifier.height(MessageStyling.contentPadding))
        }
    }
}

@Composable
private fun Message.rememberMessageInfo(): MessageContentInfo {
    return remember(this) {
        var hasFiles = false
        var hasRecordings = false
        var hasLinks = false
        var hasMedia = false
        var hasGiphys = false
        var hasUnknown = false
        var hasUploads = false

        attachments.forEach {
            hasUploads = hasUploads || it.isUploading()
            when {
                it.isFile() || it.isAudio() -> hasFiles = true
                it.isAudioRecording() -> hasRecordings = true
                it.hasLink() && !it.isGiphy() -> hasLinks = true
                it.isGiphy() -> hasGiphys = true
                it.isImage() || it.isVideo() -> hasMedia = true
                else -> hasUnknown = true
            }
        }

        MessageContentInfo(
            hasFiles = hasFiles,
            hasRecordings = hasRecordings,
            hasLinks = hasLinks,
            hasMedia = hasMedia,
            hasGiphys = hasGiphys,
            hasUnknown = hasUnknown,
            hasUploads = hasUploads,
            displaysFullSizeAttachment = shouldBeDisplayedAsFullSizeAttachment(),
        )
    }
}

private data class MessageContentInfo(
    val hasFiles: Boolean,
    val hasRecordings: Boolean,
    val hasLinks: Boolean,
    val hasMedia: Boolean,
    val hasGiphys: Boolean,
    val hasUnknown: Boolean,
    val hasUploads: Boolean,
    val displaysFullSizeAttachment: Boolean,
)
