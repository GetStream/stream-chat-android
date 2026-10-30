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

package io.getstream.chat.android.compose.sample.feature.scheduled

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.sample.ui.component.AppToolbar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.compose.R as ComposeR

/**
 * The current user's scheduled messages: the failed ones first, then the ones waiting for the connection, then the
 * others by day. A tap on a message opens the actions its status allows.
 *
 * @param viewModel The view model of the screen.
 * @param onEditClick Called to edit a scheduled message in the composer of its channel or thread. When
 * `isScheduledAgain` is true, the message failed to send and is scheduled again as a new message.
 * @param onBack Called when the user goes back.
 * @param modifier The modifier of the screen.
 */
@Suppress("LongMethod")
@Composable
internal fun ScheduledMessagesScreen(
    viewModel: ScheduledMessagesViewModel,
    onEditClick: (scheduledMessage: ScheduledMessage, isScheduledAgain: Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedItem by remember { mutableStateOf<ScheduledMessageItemState?>(null) }
    var messageToReschedule by remember { mutableStateOf<ScheduledMessage?>(null) }
    var messageToSendNow by remember { mutableStateOf<ScheduledMessage?>(null) }
    var itemToDelete by remember { mutableStateOf<ScheduledMessageItemState?>(null) }
    val onActionClick: (ScheduledMessageItemState, ScheduledMessageAction) -> Unit = { item, action ->
        val scheduledMessage = item.scheduledMessage
        when (action) {
            ScheduledMessageAction.Edit -> onEditClick(scheduledMessage, false)
            ScheduledMessageAction.ScheduleAgain -> onEditClick(scheduledMessage, true)
            ScheduledMessageAction.Reschedule -> messageToReschedule = scheduledMessage
            ScheduledMessageAction.SendNow -> messageToSendNow = scheduledMessage
            ScheduledMessageAction.Retry -> viewModel.onRetryClick(scheduledMessage.id)
            ScheduledMessageAction.CopyText -> context.copyText(scheduledMessage.message.text)
            ScheduledMessageAction.Delete -> itemToDelete = item
        }
    }
    Scaffold(
        modifier = modifier,
        topBar = { AppToolbar(stringResource(R.string.scheduled_messages_title), onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ChatTheme.colors.backgroundCoreApp,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.isLoading -> ScheduledMessagesLoading()
                state.isLoadFailed -> ScheduledMessagesLoadFailed(onRetryClick = viewModel::onRetryLoadClick)
                state.groups.isEmpty() -> ScheduledMessagesEmpty()
                else -> ScheduledMessagesList(
                    groups = state.groups,
                    currentUser = currentUser,
                    onItemClick = { item ->
                        if (item.actions.isEmpty()) viewModel.onSendingMessageClick() else selectedItem = item
                    },
                )
            }
        }
    }
    selectedItem?.let { item ->
        ScheduledMessageActionsSheet(
            actions = item.actions,
            onActionClick = { onActionClick(item, it) },
            onDismiss = { selectedItem = null },
        )
    }
    messageToReschedule?.let { scheduledMessage ->
        ScheduleTimeSheet(
            title = stringResource(R.string.scheduled_messages_reschedule_title),
            confirmLabel = stringResource(R.string.scheduled_messages_reschedule),
            previewTitle = stringResource(
                R.string.scheduled_messages_scheduled_at,
                context.formatScheduledDate(scheduledMessage.scheduledAt),
            ),
            previewText = scheduledMessage.message.text,
            initialDate = scheduledMessage.scheduledAt,
            note = null,
            isOffline = state.isOffline,
            onConfirm = { viewModel.onRescheduleClick(scheduledMessage.id, it) },
            onDismiss = { messageToReschedule = null },
        )
    }
    messageToSendNow?.let { scheduledMessage ->
        SendNowDialog(
            onConfirm = {
                viewModel.onSendNowClick(scheduledMessage.id)
                messageToSendNow = null
            },
            onDismiss = { messageToSendNow = null },
        )
    }
    itemToDelete?.let { item ->
        DeleteDialog(
            channelName = item.channelName(currentUser),
            onConfirm = {
                viewModel.onDeleteClick(item.scheduledMessage.id)
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null },
        )
    }
    ScheduledMessagesSnackbars(events = viewModel.events, snackbarHostState = snackbarHostState)
}

@Composable
private fun ScheduledMessagesList(
    groups: List<ScheduledMessagesGroup>,
    currentUser: User?,
    onItemClick: (ScheduledMessageItemState) -> Unit,
) {
    val context = LocalContext.current
    LazyColumn(modifier = Modifier.testTag("Stream_ScheduledMessagesList")) {
        groups.forEach { group ->
            item(key = group.key) {
                val title = when (group) {
                    is ScheduledMessagesGroup.Failed -> stringResource(R.string.scheduled_messages_failed_header)
                    is ScheduledMessagesGroup.Waiting -> stringResource(R.string.scheduled_messages_waiting_header)
                    is ScheduledMessagesGroup.Day -> context.formatScheduledDay(group.date)
                }
                GroupHeader(title)
            }
            items(group.items, key = { it.scheduledMessage.id }) { item ->
                ScheduledMessageItem(
                    item = item,
                    channelName = item.channelName(currentUser),
                    isQueued = group is ScheduledMessagesGroup.Waiting,
                    onClick = { onItemClick(item) },
                )
            }
        }
    }
}

@Composable
private fun GroupHeader(title: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatTheme.colors.backgroundCoreSurfaceDefault)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        text = title,
        style = ChatTheme.typography.captionEmphasis,
        color = ChatTheme.colors.textSecondary,
    )
}

@Composable
private fun ScheduledMessageItem(
    item: ScheduledMessageItemState,
    channelName: String,
    isQueued: Boolean,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val scheduledMessage = item.scheduledMessage
    val isFailed = scheduledMessage.status == ScheduledMessageStatus.Failed
    val isInProgress = !isQueued && scheduledMessage.status in InProgressStatuses
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("Stream_ScheduledMessageItem")
            .clickable(
                interactionSource = null,
                indication = ripple(),
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ScheduledMessageItemIcon(isFailed = isFailed, isInProgress = isInProgress)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = channelName,
                style = ChatTheme.typography.bodyEmphasis,
                color = ChatTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                modifier = Modifier.testTag("Stream_ScheduledMessageText"),
                text = if (isFailed) scheduledMessage.failure.text(context) else scheduledMessage.preview(),
                style = ChatTheme.typography.captionDefault,
                color = ChatTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            modifier = Modifier.testTag("Stream_ScheduledMessageStatus"),
            text = when {
                isQueued -> stringResource(R.string.scheduled_messages_queued)
                scheduledMessage.status == ScheduledMessageStatus.Sending ->
                    stringResource(R.string.scheduled_messages_sending)
                else -> DateFormat.getTimeFormat(context).format(scheduledMessage.scheduledAt)
            },
            style = ChatTheme.typography.captionDefault,
            color = if (isFailed) ChatTheme.colors.accentError else ChatTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun ScheduledMessageItemIcon(isFailed: Boolean, isInProgress: Boolean) {
    when {
        isFailed -> Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(ComposeR.drawable.stream_design_ic_exclamation_triangle_fill),
            contentDescription = stringResource(R.string.scheduled_messages_failed_header),
            tint = ChatTheme.colors.accentError,
        )
        isInProgress -> CircularProgressIndicator(
            modifier = Modifier
                .size(20.dp)
                .padding(2.dp)
                .testTag("Stream_ScheduledMessageInProgress"),
            strokeWidth = 2.dp,
            color = ChatTheme.colors.textSecondary,
        )
        else -> Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(ComposeR.drawable.stream_design_ic_clock),
            contentDescription = null,
            tint = ChatTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun ScheduledMessage.preview(): String {
    val attachments = message.attachments.size
    val content = message.text.takeIf(String::isNotBlank)
        ?: pluralStringResource(R.plurals.scheduled_messages_attachments, attachments, attachments)
    return if (message.parentId == null) content else stringResource(R.string.scheduled_messages_thread_reply, content)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduledMessageActionsSheet(
    actions: List<ScheduledMessageAction>,
    onActionClick: (ScheduledMessageAction) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        // The sheet renders in its own window, which the app-level `testTagsAsResourceId` does not reach.
        modifier = Modifier
            .testTag("Stream_ScheduledMessageActions")
            .semantics { testTagsAsResourceId = true },
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = ChatTheme.colors.backgroundCoreElevation1,
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            actions.forEach { action ->
                val color = if (action == ScheduledMessageAction.Delete) {
                    ChatTheme.colors.accentError
                } else {
                    ChatTheme.colors.textPrimary
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("Stream_ScheduledMessageAction${action.name}")
                        .clickable(
                            interactionSource = null,
                            indication = ripple(),
                        ) {
                            onDismiss()
                            onActionClick(action)
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(action.icon),
                        contentDescription = null,
                        tint = color,
                    )
                    Text(text = stringResource(action.label), style = ChatTheme.typography.bodyDefault, color = color)
                }
            }
        }
    }
}

@Composable
private fun ScheduledMessagesLoading() {
    Column(modifier = Modifier.testTag("Stream_ScheduledMessagesLoading")) {
        repeat(LoadingRows) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Placeholder(Modifier.size(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Placeholder(Modifier.size(width = 120.dp, height = 12.dp))
                    Placeholder(Modifier.size(width = 220.dp, height = 10.dp))
                }
            }
        }
    }
}

@Composable
private fun Placeholder(modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(ChatTheme.colors.backgroundCoreSurfaceDefault),
    )
}

@Composable
private fun ScheduledMessagesEmpty() {
    CenteredMessage(
        modifier = Modifier.testTag("Stream_ScheduledMessagesEmpty"),
        icon = ComposeR.drawable.stream_design_ic_clock,
        title = stringResource(R.string.scheduled_messages_empty_title),
        description = stringResource(R.string.scheduled_messages_empty_description),
    )
}

@Composable
private fun ScheduledMessagesLoadFailed(onRetryClick: () -> Unit) {
    CenteredMessage(
        modifier = Modifier.testTag("Stream_ScheduledMessagesLoadFailed"),
        icon = ComposeR.drawable.stream_design_ic_exclamation_circle,
        title = stringResource(R.string.scheduled_messages_load_failed),
    ) {
        OutlinedButton(onClick = onRetryClick) {
            Text(text = stringResource(R.string.scheduled_messages_try_again), color = ChatTheme.colors.textPrimary)
        }
    }
}

@Composable
private fun CenteredMessage(
    icon: Int,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    action: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier = Modifier.size(32.dp),
            painter = painterResource(icon),
            contentDescription = null,
            tint = ChatTheme.colors.textSecondary,
        )
        Text(
            text = title,
            style = ChatTheme.typography.bodyEmphasis,
            color = ChatTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        description?.let {
            Text(
                text = it,
                style = ChatTheme.typography.captionDefault,
                color = ChatTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        action()
    }
}

/**
 * Asks before sending a scheduled message right away.
 */
@Composable
private fun SendNowDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    ConfirmDialog(
        modifier = Modifier.testTag("Stream_ScheduledMessageSendNowDialog"),
        title = stringResource(R.string.scheduled_messages_send_now_title),
        text = stringResource(R.string.scheduled_messages_send_now_description),
        confirmLabel = stringResource(R.string.scheduled_messages_send_now),
        confirmColor = ChatTheme.colors.accentPrimary,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
private fun DeleteDialog(channelName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    ConfirmDialog(
        modifier = Modifier.testTag("Stream_ScheduledMessageDeleteDialog"),
        title = stringResource(R.string.scheduled_messages_delete_title),
        text = stringResource(R.string.scheduled_messages_delete_description, channelName),
        confirmLabel = stringResource(R.string.scheduled_messages_delete),
        confirmColor = ChatTheme.colors.accentError,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Suppress("LongParameterList")
@Composable
private fun ConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String,
    confirmColor: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        // The dialog renders in its own window, which the app-level `testTagsAsResourceId` does not reach.
        modifier = modifier.semantics { testTagsAsResourceId = true },
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = text) },
        confirmButton = {
            TextButton(modifier = Modifier.testTag("Stream_ScheduledMessageDialogConfirm"), onClick = onConfirm) {
                Text(text = confirmLabel, color = confirmColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.scheduled_messages_cancel), color = ChatTheme.colors.accentPrimary)
            }
        },
        containerColor = ChatTheme.colors.backgroundCoreElevation1,
        titleContentColor = ChatTheme.colors.textPrimary,
        textContentColor = ChatTheme.colors.textSecondary,
    )
}

@Composable
private fun ScheduledMessageItemState.channelName(currentUser: User?): String =
    channel?.let { ChatTheme.channelNameFormatter.formatChannelName(it, currentUser) }
        ?: scheduledMessage.channel?.name?.takeIf(String::isNotBlank)
        ?: scheduledMessage.cid.substringAfter(':')

private fun Context.copyText(text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(text, text))
}

private val ScheduledMessagesGroup.key: String
    get() = when (this) {
        is ScheduledMessagesGroup.Failed -> "failed"
        is ScheduledMessagesGroup.Waiting -> "waiting"
        is ScheduledMessagesGroup.Day -> "day_${date.time}"
    }

private val ScheduledMessageAction.label: Int
    get() = when (this) {
        ScheduledMessageAction.Edit -> R.string.scheduled_messages_edit
        ScheduledMessageAction.Reschedule -> R.string.scheduled_messages_reschedule
        ScheduledMessageAction.SendNow -> R.string.scheduled_messages_send_now
        ScheduledMessageAction.Retry -> R.string.scheduled_messages_try_again
        ScheduledMessageAction.CopyText -> R.string.scheduled_messages_copy_text
        ScheduledMessageAction.ScheduleAgain -> R.string.scheduled_messages_schedule_again
        ScheduledMessageAction.Delete -> R.string.scheduled_messages_delete
    }

private val ScheduledMessageAction.icon: Int
    get() = when (this) {
        ScheduledMessageAction.Edit,
        ScheduledMessageAction.ScheduleAgain,
        -> ComposeR.drawable.stream_design_ic_edit
        ScheduledMessageAction.Reschedule -> ComposeR.drawable.stream_design_ic_clock
        ScheduledMessageAction.SendNow -> ComposeR.drawable.stream_design_ic_send
        ScheduledMessageAction.Retry -> ComposeR.drawable.stream_design_ic_retry
        ScheduledMessageAction.CopyText -> ComposeR.drawable.stream_design_ic_copy
        ScheduledMessageAction.Delete -> ComposeR.drawable.stream_design_ic_delete
    }

private val InProgressStatuses = setOf(ScheduledMessageStatus.Sending, ScheduledMessageStatus.PreparingToSchedule)
private const val LoadingRows = 6
