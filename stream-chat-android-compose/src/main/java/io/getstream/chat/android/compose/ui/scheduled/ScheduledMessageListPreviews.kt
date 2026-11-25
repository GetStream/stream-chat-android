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

package io.getstream.chat.android.compose.ui.scheduled

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.ui.messages.composer.LocalScheduledMessageEditMode
import io.getstream.chat.android.compose.ui.messages.composer.MessageComposer
import io.getstream.chat.android.compose.ui.messages.composer.ScheduledMessageEditMode
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageFailure
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.previewdata.PreviewChannelData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.scheduled.ScheduledMessageItemState
import io.getstream.chat.android.ui.common.state.scheduled.ScheduledMessageListState
import java.io.File
import java.util.Date

@Preview(showBackground = true)
@Composable
private fun ScheduledMessageItemsPreview() {
    ChatTheme {
        ScheduledMessageItems()
    }
}

/** Rows of scheduled messages: a plain one, a thread reply and a quote. */
@Composable
internal fun ScheduledMessageItems() {
    PreviewItems(PreviewScheduledMessageItems[0])
}

@Preview(showBackground = true)
@Composable
private fun ScheduledMessageItemStatusesPreview() {
    ChatTheme {
        ScheduledMessageItemStatuses()
    }
}

/** Rows with a status: quote deleted, sending and uploading. */
@Composable
internal fun ScheduledMessageItemStatuses() {
    PreviewItems(PreviewScheduledMessageItems[1])
}

@Preview(showBackground = true)
@Composable
private fun ScheduledMessageItemFailuresPreview() {
    ChatTheme {
        ScheduledMessageItemFailures()
    }
}

/** Rows that failed: the server could not send one, and the channel of the other was deleted. */
@Composable
internal fun ScheduledMessageItemFailures() {
    PreviewItems(PreviewScheduledMessageItems[2])
}

@Composable
private fun PreviewItems(items: List<ScheduledMessageItemState>) {
    Column(modifier = Modifier.background(ChatTheme.colors.backgroundCoreApp)) {
        items.forEach { item ->
            ScheduledMessageItem(item = item, currentUser = PreviewUserData.user1, onClick = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScheduledMessageListLoadingPreview() {
    ChatTheme {
        ScheduledMessageListLoading()
    }
}

@Composable
internal fun ScheduledMessageListLoading() {
    ScheduledMessageListContent(
        state = ScheduledMessageListState(),
        currentUser = null,
        onItemClick = {},
        onRetryClick = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun ScheduledMessageListEmptyPreview() {
    ChatTheme {
        ScheduledMessageListEmpty()
    }
}

@Composable
internal fun ScheduledMessageListEmpty() {
    ScheduledMessageListContent(
        state = ScheduledMessageListState(isLoading = false),
        currentUser = null,
        onItemClick = {},
        onRetryClick = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun ScheduledMessageListLoadFailedPreview() {
    ChatTheme {
        ScheduledMessageListLoadFailed()
    }
}

@Composable
internal fun ScheduledMessageListLoadFailed() {
    ScheduledMessageListContent(
        state = ScheduledMessageListState(isLoading = false, isLoadFailed = true),
        currentUser = null,
        onItemClick = {},
        onRetryClick = {},
    )
}

/** The preview rows, in groups that fit one snapshot each. */
internal val PreviewScheduledMessageItems: List<List<ScheduledMessageItemState>>
    get() {
        val channel = PreviewChannelData.channelWithImage.copy(name = "Amsterdam Office")
        val directChannel = PreviewChannelData.channelWithOnlineUser
        val quoted = Message(id = "quoted", text = "Kickoff notes", user = PreviewUserData.user2)
        val date = Date(PreviewDateMillis)
        fun item(
            text: String,
            channel: Channel = directChannel,
            status: ScheduledMessageStatus = ScheduledMessageStatus.Scheduled,
            failure: ScheduledMessageFailure? = null,
            parentId: String? = null,
            quotedMessage: Message? = null,
            attachments: List<Attachment> = emptyList(),
        ): ScheduledMessageItemState {
            val message = Message(
                id = text,
                cid = channel.cid,
                text = text,
                parentId = parentId,
                replyMessageId = quotedMessage?.id,
                attachments = attachments,
            )
            val scheduledMessage = ScheduledMessage(
                id = text,
                cid = channel.cid,
                channel = null,
                message = message,
                scheduledAt = date,
                status = status,
                failure = failure,
                createdAt = date,
                updatedAt = date,
            )
            return ScheduledMessageItemState(scheduledMessage, channel, quotedMessage, emptyList())
        }
        return listOf(
            item("Heads up: the launch moved to Monday."),
            item("Can someone review the copy?", channel = channel, parentId = "parent"),
            item("Looks good to me", quotedMessage = quoted),
            item("Agreed, let's go with that one.", quotedMessage = quoted.copy(deletedAt = date)),
            item("Booking confirmed for the 14th.", status = ScheduledMessageStatus.Sending),
            item(
                text = "",
                channel = channel,
                status = ScheduledMessageStatus.PreparingToSchedule,
                attachments = listOf(Attachment(type = "image", upload = File("photo.jpg"))),
            ),
            item(
                text = "We shipped the fix for the login issue.",
                status = ScheduledMessageStatus.Failed,
                failure = ScheduledMessageFailure.ServerFailedPublishing(
                    ScheduledMessageFailure.ServerFailedPublishing.Reason.InternalError,
                ),
            ),
            item(
                text = "Booking confirmed for the 14th. See you there.",
                channel = channel,
                status = ScheduledMessageStatus.Failed,
                failure = ScheduledMessageFailure.ServerFailedPublishing(
                    ScheduledMessageFailure.ServerFailedPublishing.Reason.ChannelDeleted,
                ),
            ),
        ).chunked(PreviewItemsPerSnapshot)
    }

/** 23 Oct 2026, 09:00 UTC. */
private const val PreviewDateMillis = 1_792_746_000_000

private const val PreviewItemsPerSnapshot = 3

@Preview(showBackground = true)
@Composable
private fun ScheduledMessageEditComposerPreview() {
    ChatTheme {
        ScheduledMessageEditComposerContent()
    }
}

@Composable
internal fun ScheduledMessageEditComposerContent() {
    ScheduledMessageEditComposerContent(canSave = true, isQuoteDeleted = false)
}

@Preview(showBackground = true)
@Composable
private fun ScheduledMessageEditComposerQuoteDeletedPreview() {
    ChatTheme {
        ScheduledMessageEditComposerQuoteDeleted()
    }
}

@Composable
internal fun ScheduledMessageEditComposerQuoteDeleted() {
    ScheduledMessageEditComposerContent(canSave = false, isQuoteDeleted = true)
}

@Composable
private fun ScheduledMessageEditComposerContent(canSave: Boolean, isQuoteDeleted: Boolean) {
    val edited = Message(id = "1", text = "Heads up: the launch moved to Monday.")
    Column(modifier = Modifier.background(ChatTheme.colors.backgroundCoreApp)) {
        if (isQuoteDeleted) {
            ScheduledMessageQuoteDeletedBanner(onRemoveQuoteClick = {})
        }
        CompositionLocalProvider(LocalScheduledMessageEditMode provides ScheduledMessageEditMode(canSave)) {
            MessageComposer(
                messageComposerState = MessageComposerState(
                    inputValue = "Heads up: launch moved to Monday.",
                    action = Edit(edited),
                    ownCapabilities = setOf(ChannelCapabilities.SEND_MESSAGE),
                ),
                onSendMessage = { _, _ -> },
            )
        }
    }
}
