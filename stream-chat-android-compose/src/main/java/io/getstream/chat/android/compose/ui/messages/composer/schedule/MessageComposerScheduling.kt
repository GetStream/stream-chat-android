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

package io.getstream.chat.android.compose.ui.messages.composer.schedule

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import io.getstream.chat.android.compose.ui.messages.composer.LocalMessageComposerScheduleAction
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.ScheduleMessageSheetParams
import io.getstream.chat.android.compose.ui.theme.ScheduleMessageTimeSheetParams
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.feature.messages.composer.scheduled.scheduleMessageQuickDates
import io.getstream.chat.android.ui.common.state.messages.composer.ScheduleMessageError
import io.getstream.chat.android.ui.common.state.messages.composer.ScheduleMessageState
import io.getstream.result.Result
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Date

/**
 * Called once the content of the composer is scheduled, so the screen can reset what it keeps about it, like the
 * selection of the attachment picker.
 */
internal val LocalOnMessageScheduled = staticCompositionLocalOf<() -> Unit> { {} }

/**
 * Lets the user schedule the content of the composer in [content]: a long press on the send button opens the schedule
 * sheet, from which the custom time sheet can be opened. Does nothing when the user can't schedule messages in the
 * channel, or when [io.getstream.chat.android.compose.ui.theme.ComposerConfig.scheduledMessagesEnabled] is false.
 *
 * @param viewModel The view model of the composer, which schedules its content.
 * @param content The composer.
 */
@Composable
internal fun MessageComposerScheduling(
    viewModel: MessageComposerViewModel,
    content: @Composable () -> Unit,
) {
    if (!ChatTheme.config.composer.scheduledMessagesEnabled) {
        content()
        return
    }
    val scheduleState by viewModel.scheduleMessageState.collectAsState()
    val host = remember(viewModel) { ScheduleMessageSheetHostState() }
    val scheduleAction = remember(host) { host::requestQuickSheet }
    CompositionLocalProvider(
        LocalMessageComposerScheduleAction provides scheduleAction.takeIf { scheduleState.canSchedule },
        content = content,
    )
    if (scheduleState.canSchedule) {
        ScheduleMessageSheetHost(viewModel = viewModel, state = scheduleState, host = host)
    }
}

/**
 * The sheet that is open, if any.
 */
private sealed interface ScheduleSheet {

    /** The quick dates. */
    data object Quick : ScheduleSheet

    /** A custom date, starting at [initialDate], with an [error] that does not depend on the picked date. */
    data class Custom(
        val message: Message,
        val initialDate: Date,
        val error: ScheduleMessageError.Failed? = null,
    ) : ScheduleSheet
}

private class ScheduleMessageSheetHostState {

    /** The sheet asked for, shown once the keyboard is hidden, so they don't animate together. */
    var requested by mutableStateOf<ScheduleSheet?>(null)

    /** The sheet shown. */
    var shown by mutableStateOf<ScheduleSheet?>(null)

    fun requestQuickSheet() {
        requested = ScheduleSheet.Quick
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScheduleMessageSheetHost(
    viewModel: MessageComposerViewModel,
    state: ScheduleMessageState,
    host: ScheduleMessageSheetHostState,
) {
    val composerState by viewModel.messageComposerState.collectAsState()
    val onMessageScheduled = LocalOnMessageScheduled.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isImeVisible by rememberUpdatedState(WindowInsets.isImeVisible)
    LaunchedEffect(host.requested) {
        val requested = host.requested ?: return@LaunchedEffect
        keyboardController?.hide()
        // Bounded, as the keyboard insets don't reach every screen, and can stay "visible" there.
        withTimeoutOrNull(KeyboardHideTimeoutMillis) { snapshotFlow { isImeVisible }.first { !it } }
        host.shown = requested
        host.requested = null
    }

    fun buildMessage(): Message = viewModel.buildNewMessage(composerState.inputValue, composerState.attachments)

    fun schedule(message: Message, date: Date) {
        if (state.messageError(message) != null || state.dateError(date) != null) {
            // The custom time sheet shows why.
            host.shown = ScheduleSheet.Custom(message = message, initialDate = date)
            return
        }
        viewModel.scheduleMessage(message, date) { result ->
            when (result) {
                is Result.Success -> {
                    host.shown = null
                    onMessageScheduled()
                }

                is Result.Failure -> host.shown = ScheduleSheet.Custom(
                    message = message,
                    initialDate = date,
                    error = ScheduleMessageError.Failed(result.value),
                )
            }
        }
    }

    when (val sheet = host.shown) {
        ScheduleSheet.Quick -> ChatTheme.componentFactory.ScheduleMessageSheet(
            params = ScheduleMessageSheetParams(
                dates = remember { scheduleMessageQuickDates(Date()) },
                state = state,
                onDateClick = { date -> schedule(buildMessage(), date) },
                onCustomTimeClick = {
                    host.shown = ScheduleSheet.Custom(
                        message = buildMessage(),
                        initialDate = scheduleMessageQuickDates(Date()).first(),
                    )
                },
                onDismiss = { host.shown = null },
            ),
        )

        is ScheduleSheet.Custom -> ChatTheme.componentFactory.ScheduleMessageTimeSheet(
            params = ScheduleMessageTimeSheetParams(
                message = sheet.message,
                channelName = rememberChannelName(viewModel),
                initialDate = sheet.initialDate,
                state = state,
                error = state.messageError(sheet.message) ?: sheet.error,
                onConfirm = { date -> schedule(sheet.message, date) },
                onDismiss = { host.shown = null },
            ),
        )

        null -> Unit
    }
}

@Composable
private fun rememberChannelName(viewModel: MessageComposerViewModel): String {
    val formatter = ChatTheme.channelNameFormatter
    val currentUser = viewModel.messageComposerState.value.currentUser
    return remember(viewModel, formatter, currentUser) {
        viewModel.channelState.value?.toChannel()?.let { formatter.formatChannelName(it, currentUser) }.orEmpty()
    }
}

private const val KeyboardHideTimeoutMillis = 500L
