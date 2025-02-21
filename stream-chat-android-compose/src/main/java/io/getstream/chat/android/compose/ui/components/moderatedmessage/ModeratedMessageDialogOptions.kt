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

package io.getstream.chat.android.compose.ui.components.moderatedmessage

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.messages.list.ModeratedMessageOption

/**
 * Composable that represents the dialog options a user can select to act upon a moderated message.
 *
 * @param message The moderated [Message] upon which the user can take action.
 * @param modifier Modifier for styling.
 * @param options List of options that the user can choose from for the moderated message.
 * @param onDismissRequest Handler for dialog dismissal.
 * @param onDialogOptionInteraction Handler for detecting the action taken upon the dialog.
 * @param itemContent Composable that represents a single option item. By default shows just a text.
 */
@Composable
public fun ModeratedMessageDialogOptions(
    message: Message,
    options: List<ModeratedMessageOption>,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
    onDialogOptionInteraction: (message: Message, option: ModeratedMessageOption) -> Unit = { _, _ -> },
    itemContent: @Composable (ModeratedMessageOption) -> Unit = { option ->
        DefaultModeratedMessageOptionItem(message, option, onDismissRequest, onDialogOptionInteraction)
    },
) {
    LazyColumn(modifier = modifier) {
        items(options) { option ->
            itemContent(option)
        }
    }
}

/**
 * Represents the default moderated message options item.
 *
 * By default shows only text of the action a user can perform.
 *
 * @param message The moderated [Message] upon which the user can take action.
 * @param option The option that the user can choose for the moderated message.
 * @param onDismissRequest Handler for dialog dismissal.
 * @param onDialogOptionInteraction Handler for detecting the action taken upon the dialog.
 */
@Composable
internal fun DefaultModeratedMessageOptionItem(
    message: Message,
    option: ModeratedMessageOption,
    onDismissRequest: () -> Unit,
    onDialogOptionInteraction: (message: Message, option: ModeratedMessageOption) -> Unit,
) {
    ModeratedMessageOptionItem(
        option = option,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable {
                onDialogOptionInteraction(message, option)
                onDismissRequest()
            },
    )
}
