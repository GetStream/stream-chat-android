/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.sample.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.ui.pinned.PinnedMessageItem
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel

@Composable
fun StickyPinnedMessagesSubheader(
    viewModel: MessageListViewModel,
) {
    val pinnedMessages = viewModel
        .channel
        .pinnedMessages
        .sortedByDescending { it.pinnedAt }

    if (pinnedMessages.isNotEmpty()) {
        val currentUser = viewModel.user.collectAsStateWithLifecycle().value
        var currentPinnedMessageIndex by rememberSaveable(pinnedMessages.size) { mutableIntStateOf(0) }
        val index = if (pinnedMessages.isNotEmpty()) currentPinnedMessageIndex % pinnedMessages.size else 0
        val pinnedMessage = pinnedMessages[index]
        Column(
            modifier = Modifier.background(ChatTheme.colors.barsBackground),
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                text = "Pinned messages",
                style = ChatTheme.typography.bodyItalic,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                PinnedMessageItem(
                    modifier = Modifier.weight(1f),
                    message = pinnedMessage,
                    currentUser = currentUser,
                    onPinnedMessageClick = { message ->
                        viewModel.scrollToMessage(message.id, message.parentId)
                    },
                )
                Icon(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(),
                            onClick = {
                                currentPinnedMessageIndex++
                            },
                        )
                        .padding(16.dp),
                    contentDescription = "move to the next",
                    painter = painterResource(id = R.drawable.ic_arrow_up),
                )
            }
        }
    }
}
