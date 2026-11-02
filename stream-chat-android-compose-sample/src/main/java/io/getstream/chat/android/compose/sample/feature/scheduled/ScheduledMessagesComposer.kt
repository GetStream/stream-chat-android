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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.ui.theme.ChannelHeaderParams
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.LocalComponentFactory
import io.getstream.chat.android.compose.ui.theme.MessageComposerParams
import io.getstream.chat.android.compose.ui.theme.MessageComposerSendButtonParams
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.Reply
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Date
import io.getstream.chat.android.compose.R as ComposeR

/**
 * Adds scheduled messages to the channel screen in [content]: a long press on send schedules the content of the
 * composer, a banner under the header tells about the scheduled messages of the channel or the thread, and a
 * scheduled message can be edited in the composer. Shown only when the user has the
 * [ChannelCapabilities.SCHEDULE_MESSAGE] capability in the channel.
 *
 * @param composerViewModel The view model of the composer of [content].
 * @param attachmentsPickerViewModel The view model of the attachment picker of [content].
 * @param viewModel Schedules and edits the scheduled messages.
 * @param channelName The name of the channel, for the preview of the message to schedule.
 * @param onViewClick Called to show all the scheduled messages.
 * @param content The channel screen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Suppress("LongMethod", "LongParameterList")
@Composable
internal fun ScheduledMessagesComposer(
    composerViewModel: MessageComposerViewModel,
    attachmentsPickerViewModel: AttachmentsPickerViewModel,
    viewModel: ChannelScheduledMessagesViewModel,
    channelName: String,
    onViewClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    var sheet by rememberSaveable { mutableStateOf<ScheduleSheet?>(null) }
    // A sheet asked for while the keyboard is open, shown once the keyboard is hidden, so they don't animate together.
    var requestedSheet by remember { mutableStateOf<ScheduleSheet?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val isImeVisible by rememberUpdatedState(WindowInsets.isImeVisible)
    LaunchedEffect(requestedSheet) {
        val requested = requestedSheet ?: return@LaunchedEffect
        keyboardController?.hide()
        // Bounded, as the keyboard insets reach this screen only on some API levels, and stay "visible" on the others.
        withTimeoutOrNull(KeyboardHideTimeoutMs) { snapshotFlow { isImeVisible }.first { !it } }
        sheet = requested
        requestedSheet = null
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val base = LocalComponentFactory.current
    // Remembered here, not with CompoundComponentFactory, so typing in the composer does not rebuild it.
    val componentFactory = remember(base, composerViewModel, viewModel) {
        ScheduledMessagesComponentFactory(
            delegate = base,
            composerViewModel = composerViewModel,
            viewModel = viewModel,
            onViewClick = onViewClick,
            onSendLongClick = {
                requestedSheet = when {
                    viewModel.editedMessage.value == null -> ScheduleSheet.Quick
                    viewModel.isScheduledAgain -> ScheduleSheet.ScheduleAgain
                    else -> ScheduleSheet.Reschedule
                }
            },
            onScheduleAgainClick = { requestedSheet = ScheduleSheet.ScheduleAgain },
        )
    }
    ThreadTracker(composerViewModel, viewModel)

    Box(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalComponentFactory provides componentFactory, content = content)
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = SnackbarBottomPadding),
        )
    }

    sheet?.let { currentSheet ->
        ScheduleSheetHost(
            sheet = currentSheet,
            composerViewModel = composerViewModel,
            viewModel = viewModel,
            channelName = channelName,
            onSheetChange = { sheet = it },
        )
    }

    ScheduledMessagesSnackbars(
        events = viewModel.events,
        snackbarHostState = snackbarHostState,
        onViewClick = onViewClick,
    ) { event ->
        when (event) {
            ScheduledMessagesEvent.ClearComposer -> {
                composerViewModel.clearData()
                attachmentsPickerViewModel.setPickerVisible(visible = false)
            }
            is ScheduledMessagesEvent.EditInComposer -> {
                val message = event.scheduledMessage.message
                composerViewModel.setMessageInput(message.text)
                composerViewModel.addAttachments(message.attachments)
                composerViewModel.setAlsoSendToChannel(message.showInChannel)
                event.quotedMessage?.let { composerViewModel.performMessageAction(Reply(it)) }
            }
            else -> Unit
        }
    }
}

/**
 * Tells [viewModel] the thread of the composer, for its banner.
 */
@Composable
private fun ThreadTracker(composerViewModel: MessageComposerViewModel, viewModel: ChannelScheduledMessagesViewModel) {
    val messageMode by remember(composerViewModel) {
        composerViewModel.messageComposerState.map { it.messageMode }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(MessageMode.Normal)
    val parentId = (messageMode as? MessageMode.MessageThread)?.parentMessage?.id
    LaunchedEffect(parentId) { viewModel.onThreadChanged(parentId) }
}

/**
 * Shows [sheet], and calls [onSheetChange] with the next sheet, or null when it is dismissed.
 */
@Suppress("LongMethod")
@Composable
private fun ScheduleSheetHost(
    sheet: ScheduleSheet,
    composerViewModel: MessageComposerViewModel,
    viewModel: ChannelScheduledMessagesViewModel,
    channelName: String,
    onSheetChange: (ScheduleSheet?) -> Unit,
) {
    val context = LocalContext.current
    val composerState by composerViewModel.messageComposerState.collectAsStateWithLifecycle()
    val editedMessage by viewModel.editedMessage.collectAsStateWithLifecycle()
    val banner by viewModel.banner.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    when (sheet) {
        ScheduleSheet.Quick -> ScheduleMessageSheet(
            onDateSelected = { viewModel.onScheduleClick(composerViewModel.buildScheduledMessage(), it) },
            onCustomTimeClick = { onSheetChange(ScheduleSheet.Custom) },
            onDismiss = { onSheetChange(null) },
        )
        ScheduleSheet.Custom,
        ScheduleSheet.ScheduleAgain,
        -> ScheduleTimeSheet(
            title = stringResource(R.string.scheduled_messages_schedule_title),
            confirmLabel = stringResource(R.string.scheduled_messages_schedule),
            previewTitle = stringResource(R.string.scheduled_messages_message_to, channelName),
            previewText = composerState.inputValue,
            initialDate = scheduleMessagePresets(Date()).first(),
            note = banner?.takeIf { it is ScheduledMessagesBanner.Pending }?.let {
                pluralStringResource(R.plurals.scheduled_messages_in_this_channel_count, it.count, it.count)
            },
            isOffline = isOffline,
            onConfirm = { viewModel.onScheduleClick(composerViewModel.buildScheduledMessage(), it) },
            onDismiss = { onSheetChange(null) },
        )
        ScheduleSheet.Reschedule -> editedMessage?.let { edited ->
            ScheduleTimeSheet(
                title = stringResource(R.string.scheduled_messages_reschedule_title),
                confirmLabel = stringResource(R.string.scheduled_messages_reschedule),
                previewTitle = stringResource(
                    R.string.scheduled_messages_scheduled_at,
                    context.formatScheduledDate(edited.scheduledAt),
                ),
                previewText = composerState.inputValue,
                initialDate = edited.scheduledAt,
                note = null,
                isOffline = isOffline,
                onConfirm = { viewModel.onRescheduleEditClick(composerViewModel.buildScheduledMessage(), it) },
                onDismiss = { onSheetChange(null) },
            )
        }
    }
}

/**
 * The sheet open to pick when the content of the composer is sent.
 */
private enum class ScheduleSheet {
    /** The quick times to schedule a new message. */
    Quick,

    /** A custom time to schedule a new message. */
    Custom,

    /** A new time for the edited message. */
    Reschedule,

    /** A time for the edited message, which failed to send, scheduled again as a new message. */
    ScheduleAgain,
}

/**
 * Builds the message to schedule from the content of the composer, like the composer does before sending it.
 */
private fun MessageComposerViewModel.buildScheduledMessage(): Message {
    val state = messageComposerState.value
    val isInThread = state.messageMode is MessageMode.MessageThread
    return buildNewMessage(attachments = state.attachments)
        .copy(showInChannel = isInThread && state.alsoSendToChannel)
}

@Suppress("LongParameterList")
private class ScheduledMessagesComponentFactory(
    private val delegate: ChatComponentFactory,
    private val composerViewModel: MessageComposerViewModel,
    private val viewModel: ChannelScheduledMessagesViewModel,
    private val onViewClick: () -> Unit,
    private val onSendLongClick: () -> Unit,
    private val onScheduleAgainClick: () -> Unit,
) : ChatComponentFactory by delegate {

    @Composable
    override fun ChannelHeader(params: ChannelHeaderParams) {
        val composerState by composerViewModel.messageComposerState.collectAsStateWithLifecycle()
        val banner by viewModel.banner.collectAsStateWithLifecycle()
        Column {
            delegate.ChannelHeader(params)
            banner?.takeIf { composerState.canSchedule }?.let {
                ScheduledMessagesBanner(
                    banner = it,
                    isInThread = params.messageMode is MessageMode.MessageThread,
                    onClick = onViewClick,
                )
            }
        }
    }

    @Composable
    override fun MessageComposer(params: MessageComposerParams) {
        val editedMessage by viewModel.editedMessage.collectAsStateWithLifecycle()
        Column {
            editedMessage?.let {
                ScheduledMessageEditHeader(
                    title = if (viewModel.isScheduledAgain) {
                        stringResource(R.string.scheduled_messages_schedule_again)
                    } else {
                        stringResource(R.string.scheduled_messages_edit_header)
                    },
                    text = it.message.text,
                    onCancelClick = viewModel::onCancelEditClick,
                )
            }
            delegate.MessageComposer(params)
        }
    }

    @Composable
    override fun MessageComposerSendButton(params: MessageComposerSendButtonParams) {
        val composerState by composerViewModel.messageComposerState.collectAsStateWithLifecycle()
        val editedMessage by viewModel.editedMessage.collectAsStateWithLifecycle()
        when {
            editedMessage != null -> ScheduleSendButton(
                isEditing = true,
                onClick = {
                    if (viewModel.isScheduledAgain) {
                        onScheduleAgainClick()
                    } else {
                        viewModel.onSaveEditClick(composerViewModel.buildScheduledMessage())
                    }
                },
                onLongClick = onSendLongClick,
            )
            composerState.canSchedule -> ScheduleSendButton(
                isEditing = false,
                onClick = params.onClick,
                onLongClick = onSendLongClick,
            )
            else -> delegate.MessageComposerSendButton(params)
        }
    }
}

private val MessageComposerState.canSchedule: Boolean
    get() = ChannelCapabilities.SCHEDULE_MESSAGE in ownCapabilities

/**
 * The send button, which also schedules on a long press. While a scheduled message is edited, a click saves the edit.
 */
@Composable
private fun ScheduleSendButton(
    isEditing: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val icon = if (isEditing) ComposeR.drawable.stream_design_ic_checkmark else ComposeR.drawable.stream_design_ic_send
    val description = if (isEditing) {
        stringResource(R.string.scheduled_messages_save)
    } else {
        stringResource(ComposeR.string.stream_compose_message_composer_send_button)
    }
    val clickLabel = if (isEditing) {
        stringResource(R.string.scheduled_messages_save)
    } else {
        stringResource(ComposeR.string.stream_compose_message_composer_send_action)
    }
    val longClickLabel = if (isEditing) {
        stringResource(R.string.scheduled_messages_reschedule_title)
    } else {
        stringResource(R.string.scheduled_messages_schedule_title)
    }
    Box(
        modifier = Modifier
            .size(48.dp)
            .padding(8.dp)
            .clip(CircleShape)
            .background(ChatTheme.colors.accentPrimary)
            .testTag(if (isEditing) "Stream_ScheduledMessageSaveButton" else "Stream_ComposerSendButton")
            .combinedClickable(
                interactionSource = null,
                indication = ripple(),
                onClickLabel = clickLabel,
                onLongClickLabel = longClickLabel,
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = description,
            tint = ChatTheme.colors.textOnAccent,
        )
    }
}

@Composable
private fun ScheduledMessagesBanner(
    banner: ScheduledMessagesBanner,
    isInThread: Boolean,
    onClick: () -> Unit,
) {
    val isFailed = banner is ScheduledMessagesBanner.Failed
    val accent = if (isFailed) ChatTheme.colors.accentError else ChatTheme.colors.accentPrimary
    val icon = if (isFailed) {
        ComposeR.drawable.stream_design_ic_exclamation_circle
    } else {
        ComposeR.drawable.stream_design_ic_clock
    }
    val text = when (banner) {
        is ScheduledMessagesBanner.Failed ->
            pluralStringResource(R.plurals.scheduled_messages_failed_count, banner.count, banner.count)
        is ScheduledMessagesBanner.Waiting ->
            pluralStringResource(R.plurals.scheduled_messages_waiting_count, banner.count, banner.count)
        is ScheduledMessagesBanner.Pending -> if (isInThread) {
            pluralStringResource(R.plurals.scheduled_messages_replies_count, banner.count, banner.count)
        } else {
            pluralStringResource(R.plurals.scheduled_messages_count, banner.count, banner.count)
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("Stream_ScheduledMessagesBanner")
            // Opaque, as the message list scrolls under the header.
            .background(ChatTheme.colors.backgroundCoreApp)
            .background(accent.copy(alpha = BannerBackgroundAlpha))
            .clickable(
                interactionSource = null,
                indication = ripple(),
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(modifier = Modifier.size(16.dp), painter = painterResource(icon), contentDescription = null, tint = accent)
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            style = ChatTheme.typography.captionEmphasis,
            color = ChatTheme.colors.textPrimary,
        )
        Text(
            text = stringResource(R.string.scheduled_messages_view),
            style = ChatTheme.typography.captionEmphasis,
            color = ChatTheme.colors.accentPrimary,
        )
    }
}

@Composable
private fun ScheduledMessageEditHeader(title: String, text: String, onCancelClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag("Stream_ScheduledMessageEditBanner"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MessagePreview(modifier = Modifier.weight(1f), title = title, text = text)
        IconButton(
            modifier = Modifier
                .size(32.dp)
                .testTag("Stream_ScheduledMessageEditCancel"),
            onClick = onCancelClick,
        ) {
            Icon(
                painter = painterResource(ComposeR.drawable.stream_design_ic_xmark),
                contentDescription = stringResource(R.string.scheduled_messages_cancel_edit),
                tint = ChatTheme.colors.textSecondary,
            )
        }
    }
}

private const val BannerBackgroundAlpha = 0.08f
private const val KeyboardHideTimeoutMs = 500L
private val SnackbarBottomPadding = 72.dp
