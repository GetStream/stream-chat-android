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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.PollPickerMode
import io.getstream.chat.android.compose.ui.messages.composer.LocalScheduledMessageEditMode
import io.getstream.chat.android.compose.ui.messages.composer.MessageComposer
import io.getstream.chat.android.compose.ui.messages.composer.ScheduledMessageEditMode
import io.getstream.chat.android.compose.ui.theme.AttachmentPickerMenuParams
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.LocalChatUiConfig
import io.getstream.chat.android.compose.ui.theme.ScheduleMessageTimeSheetParams
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.ChannelViewModelFactory
import io.getstream.chat.android.compose.viewmodel.messages.ComposerOptions
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.scheduled.ScheduledMessageListViewModel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.feature.messages.composer.scheduled.scheduleMessageQuickDates
import io.getstream.chat.android.ui.common.helper.internal.AttachmentStorageHelper.Companion.EXTRA_SOURCE_URI
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.composer.ScheduleMessageError
import io.getstream.chat.android.ui.common.state.messages.composer.ScheduleMessageState
import io.getstream.chat.android.ui.common.state.scheduled.ScheduledMessageAction
import io.getstream.chat.android.ui.common.state.scheduled.ScheduledMessageItemState
import io.getstream.chat.android.ui.common.utils.typing.TypingUpdatesBuffer
import io.getstream.result.Result
import java.util.Date

/**
 * The scheduled message edited in the composer of the list, if any. Kept by [ScheduledMessageListViewModel], so the
 * edit and its save in flight survive a configuration change. After a process restart, no message is edited.
 */
internal class ScheduledMessageEdit {

    var session by mutableStateOf<ScheduledMessageEditSession?>(null)
        private set

    /**
     * The IDs of the scheduled messages whose edit is being saved, so a message is not saved twice, also after its edit
     * was cancelled and opened again.
     */
    var savingIds by mutableStateOf(emptySet<String>())
        private set

    private var lastToken = 0

    /** Starts editing [scheduledMessage], in place of the message edited so far. */
    fun start(scheduledMessage: ScheduledMessage) {
        if (session?.id == scheduledMessage.id) return
        session = ScheduledMessageEditSession(
            token = ++lastToken,
            id = scheduledMessage.id,
            cid = scheduledMessage.cid,
            parentId = scheduledMessage.message.parentId,
        )
    }

    /** Ends the edit with [token], unless another edit started since. */
    fun end(token: Int) {
        if (session?.token == token) session = null
    }

    fun removeQuote() {
        session = session?.copy(isQuoteRemoved = true)
    }

    fun onFilled(token: Int) {
        update(token) { it.copy(isFilled = true) }
    }

    fun setSaving(id: String, isSaving: Boolean) {
        savingIds = if (isSaving) savingIds + id else savingIds - id
    }

    private fun update(token: Int, transform: (ScheduledMessageEditSession) -> ScheduledMessageEditSession) {
        session = session?.let { if (it.token == token) transform(it) else it }
    }
}

/**
 * The edit of the scheduled message with [id], in the channel [cid] and the thread [parentId].
 *
 * @param token Tells this edit apart from a later edit of the same message.
 * @param isQuoteRemoved Whether the user removed the quote of the message.
 * @param isFilled Whether the composer was filled with the message, so it keeps the changes of the user.
 */
internal data class ScheduledMessageEditSession(
    val token: Int,
    val id: String,
    val cid: String,
    val parentId: String?,
    val isQuoteRemoved: Boolean = false,
    val isFilled: Boolean = false,
)

/**
 * The composer that edits the scheduled message of [edit]: the check button saves it and keeps its date, or, for a
 * message the server failed to send, picks a date and schedules it again as a new message. Saving is disabled while
 * the message is no longer scheduled, can no longer be edited (for example once the user can no longer schedule
 * messages in its channel), quotes a deleted message, or starts with a command. The composer shows once it is filled
 * with the message.
 *
 * @param edit The scheduled message edited.
 * @param item The scheduled message in the list, or null when it left the list or is not loaded yet.
 * @param isQuoteDeleted Whether the message quotes a deleted message, and the user did not remove the quote yet.
 * @param currentUser The current user, for the name of the channel.
 * @param viewModel The view model of the list, which saves the message.
 */
@Composable
internal fun ScheduledMessageEditComposer(
    edit: ScheduledMessageEdit,
    item: ScheduledMessageItemState?,
    isQuoteDeleted: Boolean,
    currentUser: User?,
    viewModel: ScheduledMessageListViewModel,
) {
    val session = edit.session ?: return
    val (composerViewModel, pickerViewModel) = rememberEditViewModels(session, viewModel)
    val composerState by composerViewModel.messageComposerState.collectAsState()
    val scheduleState by composerViewModel.scheduleMessageState.collectAsState()
    val editAction = item?.actions?.firstOrNull { it in EditActions }
    var isTimeSheetShown by rememberSaveable(session.token) { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val preview = Message(
        text = composerState.inputValue.trim(),
        attachments = composerState.attachments,
        replyTo = item?.quotedMessage?.takeUnless { session.isQuoteRemoved },
    )
    val hasCommand = scheduleState.messageError(preview) == ScheduleMessageError.CommandNotAllowed
    val canSave = editAction != null && !isQuoteDeleted && !hasCommand && session.id !in edit.savingIds
    // The callbacks read the latest values, as the composer can keep the callbacks it got first.
    val latestEditAction by rememberUpdatedState(editAction)
    val latestCanSave by rememberUpdatedState(canSave)
    val actions = remember(edit, viewModel, composerViewModel, pickerViewModel) {
        ScheduledMessageEditActions(
            edit = edit,
            viewModel = viewModel,
            composerViewModel = composerViewModel,
            pickerViewModel = pickerViewModel,
            editAction = { latestEditAction },
            canSave = { latestCanSave },
        )
    }

    FillComposerOnStart(edit, session, viewModel, composerViewModel, pickerViewModel)

    val isFilled = (composerState.action as? Edit)?.message?.id == session.id
    if (isFilled) {
        CompositionLocalProvider(
            LocalScheduledMessageEditMode provides ScheduledMessageEditMode(canSave = canSave),
        ) {
            ScheduledMessageEditComposerContent(
                composerViewModel = composerViewModel,
                pickerViewModel = pickerViewModel,
                onCancel = actions::cancel,
                onSave = { message ->
                    if (actions.save(message)) {
                        keyboardController?.hide()
                        isTimeSheetShown = true
                    }
                },
            )
        }
    }

    if (isTimeSheetShown && item != null && editAction == ScheduledMessageAction.EditAndReschedule) {
        EditAndRescheduleSheet(
            preview = preview,
            channelName = item.channelName(currentUser),
            state = scheduleState,
            onConfirm = { date ->
                isTimeSheetShown = false
                actions.reschedule(date)
            },
            onDismiss = { isTimeSheetShown = false },
        )
    }
}

/**
 * Fills the composer with the scheduled message of [session] when its edit starts, in place of what it holds. A
 * composer kept across a configuration change keeps the changes of the user. Ends [edit] when the message is no longer
 * there.
 */
@Composable
private fun FillComposerOnStart(
    edit: ScheduledMessageEdit,
    session: ScheduledMessageEditSession,
    viewModel: ScheduledMessageListViewModel,
    composerViewModel: MessageComposerViewModel,
    pickerViewModel: AttachmentsPickerViewModel,
) {
    LaunchedEffect(session.token) {
        if (session.isFilled) return@LaunchedEffect
        // This composer may still hold a message edited before in the same channel.
        composerViewModel.clearData()
        pickerViewModel.clearSelection()
        val message = viewModel.messageToEdit(session.id)
        if (message == null) {
            edit.end(session.token)
        } else {
            composerViewModel.performMessageAction(Edit(message))
            edit.onFilled(session.token)
        }
    }
}

/**
 * Saves, reschedules or cancels the edit of [edit] with the latest session and edit action, and ends it once it is
 * saved. A result that arrives after the user moved to another edit, of another message or of the same one, leaves
 * that edit alone.
 */
private class ScheduledMessageEditActions(
    private val edit: ScheduledMessageEdit,
    private val viewModel: ScheduledMessageListViewModel,
    private val composerViewModel: MessageComposerViewModel,
    private val pickerViewModel: AttachmentsPickerViewModel,
    private val editAction: () -> ScheduledMessageAction?,
    private val canSave: () -> Boolean,
) {

    /**
     * Saves [message] and keeps the date, or returns true when the message needs a new date first.
     */
    fun save(message: Message): Boolean {
        // Checked here too, as an app can replace the save button that is disabled otherwise.
        if (!canSave()) return false
        val session = edit.session ?: return false
        return when (editAction()) {
            ScheduledMessageAction.Edit -> {
                edit.setSaving(session.id, isSaving = true)
                viewModel.edit(
                    id = session.id,
                    message = message,
                    isQuoteRemoved = session.isQuoteRemoved,
                    resolveAttachments = composerViewModel::resolveAttachments,
                    onResult = { result -> onResult(session, result) },
                )
                false
            }
            ScheduledMessageAction.EditAndReschedule -> true
            else -> false
        }
    }

    /** Schedules the content of the composer at [date] as a new message, in place of the failed one. */
    fun reschedule(date: Date) {
        if (!canSave() || editAction() != ScheduledMessageAction.EditAndReschedule) return
        val session = edit.session ?: return
        val state = composerViewModel.messageComposerState.value
        edit.setSaving(session.id, isSaving = true)
        viewModel.editAndReschedule(
            id = session.id,
            message = composerViewModel.buildNewMessage(state.inputValue, state.attachments),
            isQuoteRemoved = session.isQuoteRemoved,
            scheduledAt = date,
            resolveAttachments = composerViewModel::resolveAttachments,
            onResult = { result -> onResult(session, result) },
        )
    }

    fun cancel() {
        edit.session?.let { end(it.token) }
    }

    private fun onResult(session: ScheduledMessageEditSession, result: Result<ScheduledMessage>) {
        edit.setSaving(session.id, isSaving = false)
        if (result is Result.Success) end(session.token)
    }

    private fun end(token: Int) {
        if (edit.session?.token != token) return
        pickerViewModel.setPickerVisible(visible = false)
        pickerViewModel.clearSelection()
        composerViewModel.clearData()
        edit.end(token)
    }
}

/**
 * The view models of the composer and of the attachment picker that edit a scheduled message of [session]'s channel
 * and thread. They are shared by the edits in the same channel and thread, and send no typing events.
 */
@Composable
private fun rememberEditViewModels(
    session: ScheduledMessageEditSession,
    viewModel: ScheduledMessageListViewModel,
): Pair<MessageComposerViewModel, AttachmentsPickerViewModel> {
    val context = LocalContext.current
    val linkPreviewEnabled = ChatTheme.config.composer.linkPreviewEnabled
    val factory = remember(session.cid, session.parentId) {
        ChannelViewModelFactory(
            context = context,
            channelId = session.cid,
            parentMessageId = session.parentId,
            chatClient = viewModel.chatClient,
            composerOptions = ComposerOptions(linkPreviewEnabled = linkPreviewEnabled, draftMessagesEnabled = false),
        )
    }
    // One key per class: a view model stored under the key of another class would replace and clear it.
    val key = "Stream_ScheduledMessageEdit_${session.cid}_${session.parentId}"
    val composerViewModel = viewModel(MessageComposerViewModel::class.java, key = "${key}_composer", factory = factory)
    val pickerViewModel = viewModel(AttachmentsPickerViewModel::class.java, key = "${key}_picker", factory = factory)
    // Editing a scheduled message must not tell the channel that the user is typing. Set before the edit starts.
    DisposableEffect(composerViewModel) {
        composerViewModel.setTypingUpdatesBuffer(NoTypingUpdates)
        onDispose {}
    }
    return composerViewModel to pickerViewModel
}

/**
 * The composer and its attachment picker. The picker has no polls, which are sent right away rather than with the
 * scheduled message.
 */
@Composable
private fun ScheduledMessageEditComposerContent(
    composerViewModel: MessageComposerViewModel,
    pickerViewModel: AttachmentsPickerViewModel,
    onCancel: () -> Unit,
    onSave: (Message) -> Unit,
) {
    val config = ChatTheme.config
    val pickerConfig = config.attachmentPicker
    val editConfig = remember(config) {
        config.copy(attachmentPicker = pickerConfig.copy(modes = pickerConfig.modes.filterNot { it is PollPickerMode }))
    }
    CompositionLocalProvider(LocalChatUiConfig provides editConfig) {
        // Above the keyboard, also when the screen does not apply the keyboard insets itself.
        Column(modifier = Modifier.imePadding()) {
            MessageComposer(
                modifier = Modifier.fillMaxWidth(),
                viewModel = composerViewModel,
                isAttachmentPickerVisible = pickerViewModel.isPickerVisible,
                onAttachmentsClick = pickerViewModel::togglePickerVisibility,
                onAttachmentRemoved = { attachment ->
                    (attachment.extraData[EXTRA_SOURCE_URI] as? String)?.let(pickerViewModel::removeFromSelection)
                    composerViewModel.removeAttachment(attachment)
                },
                onCancelAction = onCancel,
                onSendMessage = onSave,
            )
            ChatTheme.componentFactory.AttachmentPickerMenu(
                params = AttachmentPickerMenuParams(
                    attachmentsPickerViewModel = pickerViewModel,
                    composerViewModel = composerViewModel,
                ),
            )
        }
    }
}

/**
 * The sheet that picks the date of a message the server failed to send, to schedule it again as a new message.
 */
@Composable
private fun EditAndRescheduleSheet(
    preview: Message,
    channelName: String,
    state: ScheduleMessageState,
    onConfirm: (Date) -> Unit,
    onDismiss: () -> Unit,
) {
    ChatTheme.componentFactory.ScheduleMessageTimeSheet(
        params = ScheduleMessageTimeSheetParams(
            message = preview,
            channelName = channelName,
            initialDate = remember { scheduleMessageQuickDates(Date()).first() },
            state = state,
            error = state.messageError(preview),
            onConfirm = onConfirm,
            onDismiss = onDismiss,
        ),
    )
}

/**
 * The banner shown while the edited scheduled message quotes a deleted message, which can't be sent: the quote has
 * to be removed to save the message.
 *
 * @param onRemoveQuoteClick Called when the user removes the quote.
 */
@Composable
internal fun ScheduledMessageQuoteDeletedBanner(onRemoveQuoteClick: () -> Unit) {
    val color = ChatTheme.colors.chatTextSystem
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatTheme.colors.backgroundCoreSurfaceDefault)
            .padding(horizontal = StreamTokens.spacingSm)
            .testTag("Stream_ScheduledMessageQuoteDeletedBanner"),
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(R.drawable.stream_design_ic_exclamation_circle),
            contentDescription = null,
            tint = color,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.stream_compose_scheduled_message_edit_quote_deleted),
            style = ChatTheme.typography.metadataEmphasis,
            color = color,
        )
        Text(
            modifier = Modifier
                .testTag("Stream_ScheduledMessageRemoveQuote")
                .minimumInteractiveComponentSize()
                .clickable(role = Role.Button, onClick = onRemoveQuoteClick)
                .padding(horizontal = StreamTokens.spacingXs),
            text = stringResource(R.string.stream_compose_scheduled_message_edit_remove_quote),
            style = ChatTheme.typography.metadataEmphasis,
            color = ChatTheme.colors.accentPrimary,
        )
    }
}

private object NoTypingUpdates : TypingUpdatesBuffer {
    override fun onKeystroke(inputText: String) = Unit
    override fun clear() = Unit
}

internal val EditActions: Set<ScheduledMessageAction> =
    setOf(ScheduledMessageAction.Edit, ScheduledMessageAction.EditAndReschedule)
