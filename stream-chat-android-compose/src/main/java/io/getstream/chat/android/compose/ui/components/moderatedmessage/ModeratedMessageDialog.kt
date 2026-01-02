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

package io.getstream.chat.android.compose.ui.components.moderatedmessage

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.messages.list.ModeratedMessageOption
import io.getstream.chat.android.ui.common.state.messages.list.defaultMessageModerationOptions

/**
 * Dialog that is shown when user clicks or long taps on a moderated message. Gives the user the ability to either
 * send the message again, edit it and then send it or to delete it.
 *
 * @param message The moderated [Message] upon which the user can take action.
 * @param modifier Modifier for styling.
 * @param moderatedMessageOptions List of options that the user can choose from for the moderated message.
 * @param onDismissRequest Handler for dialog dismissal.
 * @param onDialogOptionInteraction Handler for detecting the action taken upon the dialog.
 * @param dialogTitle Composable that represents the dialog title. Shows an icon and the cause by default.
 * @param dialogDescription Composable that represents the dialog description. Shows more information about the cause
 * to the user.
 * @param dialogOptions Composable that represents the [ModeratedMessageOption]s the user can choose for the moderated
 * message. By default uses [defaultMessageModerationOptions].
 */
@Composable
public fun ModeratedMessageDialog(
    message: Message,
    onDismissRequest: () -> Unit,
    onDialogOptionInteraction: (message: Message, option: ModeratedMessageOption) -> Unit,
    modifier: Modifier = Modifier,
    moderatedMessageOptions: List<ModeratedMessageOption> = defaultMessageModerationOptions(),
    dialogTitle: @Composable () -> Unit = { DefaultModeratedMessageDialogTitle() },
    dialogDescription: @Composable () -> Unit = { DefaultModeratedMessageDialogDescription() },
    dialogOptions: @Composable () -> Unit = {
        DefaultModeratedDialogOptions(
            message = message,
            moderatedMessageOptions = moderatedMessageOptions,
            onDialogOptionInteraction = onDialogOptionInteraction,
            onDismissRequest = onDismissRequest,
        )
    },
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            dialogTitle()

            dialogDescription()

            dialogOptions()
        }
    }
}

/**
 * Represents the default content shown for the moderated message dialog options.
 *
 * By default we show the options to send the message anyway, edit it or delete it.
 *
 * @param message The moderated [Message] upon which the user can take action.
 * @param moderatedMessageOptions The list of options user can select in the dialog.
 * @param onDialogOptionInteraction Handler for detecting the action taken upon the dialog.
 * @param onDismissRequest Handler for dialog dismissal.
 */
@Composable
internal fun DefaultModeratedDialogOptions(
    message: Message,
    moderatedMessageOptions: List<ModeratedMessageOption>,
    onDialogOptionInteraction: (message: Message, option: ModeratedMessageOption) -> Unit,
    onDismissRequest: () -> Unit,
) {
    Spacer(modifier = Modifier.height(12.dp))

    ModeratedMessageDialogOptions(
        message = message,
        options = moderatedMessageOptions,
        onDismissRequest = onDismissRequest,
        onDialogOptionInteraction = onDialogOptionInteraction,
    )
}

/**
 * Moderated message dialog title composable. Shows an icon and a title.
 * */
@Composable
internal fun DefaultModeratedMessageDialogTitle() {
    Spacer(modifier = Modifier.height(12.dp))

    val painter = painterResource(id = R.drawable.stream_compose_ic_flag)
    Image(
        painter = painter,
        contentDescription = "",
        colorFilter = ColorFilter.tint(ChatTheme.colors.primaryAccent),
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = stringResource(id = R.string.stream_ui_moderation_dialog_title),
        textAlign = TextAlign.Center,
        style = ChatTheme.typography.title3,
        color = ChatTheme.colors.textHighEmphasis,
    )
}

/**
 * Moderated message dialog description composable.
 */
@Composable
internal fun DefaultModeratedMessageDialogDescription() {
    Spacer(modifier = Modifier.height(12.dp))

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        text = stringResource(id = R.string.stream_ui_moderation_dialog_description),
        textAlign = TextAlign.Center,
        style = ChatTheme.typography.body,
        color = ChatTheme.colors.textLowEmphasis,
    )
}
