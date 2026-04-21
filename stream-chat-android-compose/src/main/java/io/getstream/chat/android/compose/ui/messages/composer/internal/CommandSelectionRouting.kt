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

package io.getstream.chat.android.compose.ui.messages.composer.internal

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import io.getstream.chat.android.ui.common.state.messages.Reply

/**
 * Default handler for command taps in the composer. Routes available commands to
 * [MessageComposerViewModel.selectCommand] and shows a Toast explaining why unavailable
 * commands cannot be used in the current [MessageAction].
 */
@Composable
internal fun rememberDefaultOnCommandSelected(
    viewModel: MessageComposerViewModel,
): (Command) -> Unit {
    val state by viewModel.messageComposerState.collectAsState()
    val action = state.action
    val context = LocalContext.current
    return remember(viewModel, action, context) {
        defaultOnCommandSelected(action, context, viewModel::selectCommand)
    }
}

private fun defaultOnCommandSelected(
    action: MessageAction?,
    context: Context,
    onAvailable: (Command) -> Unit,
): (Command) -> Unit = { command ->
    routeCommandSelection(command, action, context, onAvailable)
}

internal fun routeCommandSelection(
    command: Command,
    action: MessageAction?,
    context: Context,
    onAvailable: (Command) -> Unit,
) {
    if (command.isAvailableFor(action)) {
        onAvailable(command)
    } else {
        showCommandUnavailableToast(context, action)
    }
}

private fun showCommandUnavailableToast(context: Context, action: MessageAction?) {
    val messageRes = when (action) {
        is Edit -> R.string.stream_compose_message_composer_command_unavailable_in_edit
        is Reply -> R.string.stream_compose_message_composer_command_unavailable_in_reply
        else -> return
    }
    Toast.makeText(context, context.getString(messageRes), Toast.LENGTH_SHORT).show()
}
