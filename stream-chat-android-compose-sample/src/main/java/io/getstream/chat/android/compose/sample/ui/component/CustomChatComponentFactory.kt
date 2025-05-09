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

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.isPinned
import io.getstream.chat.android.compose.sample.feature.reminders.MessageRemindersComponentFactory
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.state.messageoptions.MessageOptionItemState
import io.getstream.chat.android.compose.ui.channels.list.ChannelItem
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import io.getstream.result.call.enqueue
import java.util.Date

class CustomChatComponentFactory(
    private val messageRemindersComponentFactory: MessageRemindersComponentFactory = MessageRemindersComponentFactory(),
) : ChatComponentFactory {

    @Composable
    override fun LazyItemScope.ChannelListItemContent(
        channelItem: ItemState.ChannelItemState,
        currentUser: User?,
        onChannelClick: (Channel) -> Unit,
        onChannelLongClick: (Channel) -> Unit,
    ) {
        ChannelItem(
            modifier = Modifier
                .animateItem()
                .run {
                    // Highlight the item background color if it is pinned
                    if (channelItem.channel.isPinned()) {
                        background(color = ChatTheme.colors.highlight)
                    } else {
                        this
                    }
                },
            channelItem = channelItem,
            currentUser = currentUser,
            onChannelClick = onChannelClick,
            onChannelLongClick = onChannelLongClick,
        )
    }

    @Composable
    override fun MessageMenu(
        modifier: Modifier,
        message: Message,
        messageOptions: List<MessageOptionItemState>,
        ownCapabilities: Set<String>,
        onMessageAction: (MessageAction) -> Unit,
        onShowMore: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        messageRemindersComponentFactory.MessageMenu(
            modifier = modifier,
            message = message,
            messageOptions = messageOptions,
            ownCapabilities = ownCapabilities,
            onMessageAction = onMessageAction,
            onShowMore = onShowMore,
            onDismiss = onDismiss,
        )
    }

    private fun createReminder(messageId: String, remindAt: Date? = null) {
        val client = ChatClient.instance()
        client.createReminder(messageId, remindAt).enqueue(
            onSuccess = { reminder ->
                Log.d("X_PETAR", "Reminder created: $reminder")
            },
            onError = { error ->
                Log.e("X_PETAR", "Error creating reminder: ${error.message}")
            },
        )
    }
}
