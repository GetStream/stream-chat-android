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

package io.getstream.chat.ui.sample.util.extensions

import androidx.recyclerview.widget.ListAdapter
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem

/**
 * Notifies the [MessageListView] that a message has changed.
 */
@Suppress("UNCHECKED_CAST")
internal fun MessageListView.notifyMessageChanged(message: Message) {
    val adapter = getRecyclerView().adapter as? ListAdapter<MessageListItem, *> ?: return
    adapter.currentList.indexOfFirst { it is MessageListItem.MessageItem && it.message.id == message.id }
        .takeIf { it != -1 }
        ?.let { position ->
            adapter.notifyItemChanged(position)
        }
}
