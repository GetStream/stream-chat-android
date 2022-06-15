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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.common.model.ModeratedMessageOption

// TODO - docs for everything
@Composable
public fun ModeratedMessageDialogOptions(
    message: Message,
    options: List<ModeratedMessageOption>,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
    onDialogInteraction: (message: Message, option: ModeratedMessageOption) -> Unit = { _, _ -> },
    itemContent: @Composable (ModeratedMessageOption) -> Unit = { option ->
        DefaultModeratedMessageOptionItem(message, option, onDismissRequest, onDialogInteraction)
    },
) {
    LazyColumn(modifier = modifier) {
        items(options) { option ->
            itemContent(option)
        }
    }
}

@Composable
internal fun DefaultModeratedMessageOptionItem(
    message: Message,
    option: ModeratedMessageOption,
    onDismissRequest: () -> Unit,
    onDialogInteraction: (message: Message, option: ModeratedMessageOption) -> Unit,
) {
    ModeratedMessageOptionItem(
        option = option,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple()
            ) {
                onDialogInteraction(message, option)
                onDismissRequest()
            }
    )
}
