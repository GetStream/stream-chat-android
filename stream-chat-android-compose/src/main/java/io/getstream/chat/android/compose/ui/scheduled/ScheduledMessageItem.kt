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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.theme.ChannelAvatarParams
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ScheduledMessageFailure
import io.getstream.chat.android.models.ScheduledMessageFailure.LocalDeviceFailedScheduling
import io.getstream.chat.android.models.ScheduledMessageFailure.ServerFailedPublishing
import io.getstream.chat.android.models.ScheduledMessageFailure.ServerRejectedScheduling
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.feature.scheduled.isUploading
import io.getstream.chat.android.ui.common.state.scheduled.ScheduledMessageItemState

/**
 * The default item of the scheduled message list: the date the message is sent at, the avatar and the name of its
 * channel, a preview of the message, and its status while it is sending, uploading or failed.
 *
 * @param item The scheduled message to show.
 * @param currentUser The currently logged in user.
 * @param onClick Called when the item is clicked.
 */
@Composable
internal fun ScheduledMessageItem(
    item: ScheduledMessageItemState,
    currentUser: User?,
    onClick: (ScheduledMessageItemState) -> Unit,
) {
    val scheduledMessage = item.scheduledMessage
    val status = item.status()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("Stream_ScheduledMessageItem")
            .clickable { onClick(item) },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = StreamTokens.spacing2xs + StreamTokens.spacingMd)
                .padding(vertical = StreamTokens.spacingMd),
            verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
        ) {
            Text(
                modifier = Modifier.testTag("Stream_ScheduledMessageDate"),
                text = stringResource(
                    R.string.stream_compose_scheduled_message_list_send_at,
                    ChatTheme.dateFormatter.formatScheduledDate(scheduledMessage.scheduledAt),
                ),
                style = ChatTheme.typography.captionDefault,
                color = if (status is ItemStatus.Failed) {
                    ChatTheme.colors.textDisabled
                } else {
                    ChatTheme.colors.textTertiary
                },
                maxLines = 1,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingSm)) {
                ChatTheme.componentFactory.ChannelAvatar(
                    params = ChannelAvatarParams(
                        channel = item.channel ?: item.placeholderChannel(),
                        modifier = Modifier.size(AvatarSize.ExtraLarge),
                        currentUser = currentUser,
                    ),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
                ) {
                    ScheduledMessageItemContent(item = item, currentUser = currentUser)
                    status?.let { ScheduledMessageItemStatus(it) }
                }
            }
        }
        HorizontalDivider(thickness = 1.dp, color = ChatTheme.colors.borderCoreSubtle)
    }
}

/**
 * The name of the channel and the preview of the message.
 */
@Composable
private fun ScheduledMessageItemContent(item: ScheduledMessageItemState, currentUser: User?) {
    Column(
        modifier = Modifier.padding(vertical = StreamTokens.spacing3xs),
        verticalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
    ) {
        Text(
            modifier = Modifier.testTag("Stream_ScheduledMessageChannelName"),
            text = item.channelName(currentUser),
            style = ChatTheme.typography.captionEmphasis,
            color = ChatTheme.colors.textTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            modifier = Modifier.testTag("Stream_ScheduledMessageText"),
            text = item.preview(currentUser),
            style = ChatTheme.typography.bodyDefault,
            color = ChatTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            inlineContent = ChatTheme.messagePreviewIconFactory.createPreviewIcons(),
        )
    }
}

@Composable
private fun ScheduledMessageItemStatus(status: ItemStatus) {
    val isFailed = status is ItemStatus.Failed
    Row(
        modifier = Modifier.testTag("Stream_ScheduledMessageStatus"),
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = StreamTokens.spacing3xs)
                .size(StatusIconSize),
            contentAlignment = Alignment.Center,
        ) {
            if (isFailed) {
                Icon(
                    painter = painterResource(R.drawable.stream_design_ic_exclamation_triangle_fill),
                    contentDescription = null,
                    tint = ChatTheme.colors.accentError,
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(SpinnerSize),
                    strokeWidth = SpinnerStrokeWidth,
                    color = ChatTheme.colors.accentPrimary,
                )
            }
        }
        Text(
            text = when (status) {
                ItemStatus.Sending -> stringResource(R.string.stream_compose_scheduled_message_list_sending)
                ItemStatus.Uploading -> stringResource(R.string.stream_compose_scheduled_message_list_uploading)
                is ItemStatus.Failed -> stringResource(status.description)
            },
            style = ChatTheme.typography.captionDefault,
            color = if (isFailed) ChatTheme.colors.accentError else ChatTheme.colors.textTertiary,
        )
    }
}

/**
 * The preview of the message: a thread reply or a quote says so before the content, which names the attachments.
 */
@Composable
private fun ScheduledMessageItemState.preview(currentUser: User?): AnnotatedString {
    val message = scheduledMessage.message
    val formatter = ChatTheme.messagePreviewFormatter
    val content = remember(message, currentUser, formatter) {
        formatter.formatMessagePreview(message = message, currentUser = currentUser, isDirectMessaging = false)
    }
    val prefix = when {
        message.parentId != null -> stringResource(R.string.stream_compose_scheduled_message_list_thread_reply)
        quotedMessage != null -> stringResource(
            R.string.stream_compose_scheduled_message_list_reply_to,
            quotedMessage?.user?.name?.takeIf(String::isNotBlank) ?: quotedMessage?.user?.id.orEmpty(),
        )
        else -> return content
    }
    return buildAnnotatedString {
        append(prefix)
        append(" ")
        append(content)
    }
}

/**
 * A channel with only the ID of the channel of the scheduled message, for the placeholder avatar when there is no
 * summary.
 */
private fun ScheduledMessageItemState.placeholderChannel(): Channel {
    val (type, id) = scheduledMessage.cid.split(':', limit = 2).let { it.first() to it.getOrElse(1) { "" } }
    return Channel(type = type, id = id)
}

/**
 * What the item shows below the preview.
 */
private sealed interface ItemStatus {
    data object Sending : ItemStatus
    data object Uploading : ItemStatus
    data class Failed(val description: Int) : ItemStatus
}

private fun ScheduledMessageItemState.status(): ItemStatus? = when {
    scheduledMessage.status == ScheduledMessageStatus.Sending -> ItemStatus.Sending
    scheduledMessage.isUploading -> ItemStatus.Uploading
    scheduledMessage.status == ScheduledMessageStatus.Failed -> ItemStatus.Failed(scheduledMessage.failure.description)
    isQuoteDeleted -> ItemStatus.Failed(R.string.stream_compose_scheduled_message_failure_quote_deleted)
    else -> null
}

/**
 * The text that says why the message failed, for each reason, without the codes of the server.
 */
private val ScheduledMessageFailure?.description: Int
    get() = when (this) {
        is ServerFailedPublishing -> reason?.description
        is ServerRejectedScheduling -> reason?.description
        is LocalDeviceFailedScheduling ->
            R.string.stream_compose_scheduled_message_failure_upload
                .takeIf { reason == LocalDeviceFailedScheduling.Reason.AttachmentUploadFailed }
        else -> null
    } ?: R.string.stream_compose_scheduled_message_failure_generic

private val ServerFailedPublishing.Reason.description: Int?
    get() = when (this) {
        ServerFailedPublishing.Reason.Banned -> R.string.stream_compose_scheduled_message_failure_banned
        ServerFailedPublishing.Reason.NoPermission -> R.string.stream_compose_scheduled_message_failure_no_permission
        ServerFailedPublishing.Reason.ChannelFrozen -> R.string.stream_compose_scheduled_message_failure_channel_frozen
        ServerFailedPublishing.Reason.ChannelDeleted ->
            R.string.stream_compose_scheduled_message_failure_channel_deleted
        ServerFailedPublishing.Reason.ModerationBlocked ->
            R.string.stream_compose_scheduled_message_failure_moderation_blocked
        ServerFailedPublishing.Reason.HookRejected -> R.string.stream_compose_scheduled_message_failure_hook_rejected
        ServerFailedPublishing.Reason.Expired -> R.string.stream_compose_scheduled_message_failure_expired
        ServerFailedPublishing.Reason.InvalidMessage ->
            R.string.stream_compose_scheduled_message_failure_invalid_message
        ServerFailedPublishing.Reason.PendingNotSupported ->
            R.string.stream_compose_scheduled_message_failure_pending_not_supported
        ServerFailedPublishing.Reason.InternalError -> R.string.stream_compose_scheduled_message_failure_internal_error
        else -> null
    }

private val ServerRejectedScheduling.Reason.description: Int?
    get() = when (this) {
        ServerRejectedScheduling.Reason.ScheduledLimitChannel ->
            R.string.stream_compose_scheduled_message_failure_limit_channel
        ServerRejectedScheduling.Reason.ScheduledLimitUser ->
            R.string.stream_compose_scheduled_message_failure_limit_user
        ServerRejectedScheduling.Reason.ScheduledAtTooSoon -> R.string.stream_compose_scheduled_message_failure_too_soon
        ServerRejectedScheduling.Reason.ScheduledAtTooFar -> R.string.stream_compose_scheduled_message_failure_too_far
        ServerRejectedScheduling.Reason.ScheduledMessagesDisabled ->
            R.string.stream_compose_scheduled_message_failure_disabled
        ServerRejectedScheduling.Reason.ScheduledMessageSending ->
            R.string.stream_compose_scheduled_message_failure_sending
        ServerRejectedScheduling.Reason.ScheduledMessageConflict ->
            R.string.stream_compose_scheduled_message_failure_conflict
        ServerRejectedScheduling.Reason.ScheduledMessageNotEditable ->
            R.string.stream_compose_scheduled_message_failure_not_editable
        else -> null
    }

private val StatusIconSize = 16.dp
private val SpinnerSize = 12.dp
private val SpinnerStrokeWidth = 2.dp
