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

package io.getstream.chat.android.ui.feature.messages.list.internal

import io.getstream.chat.android.client.utils.message.isGiphyEphemeral
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem

internal object HiddenMessageListItemPredicate : MessageListView.MessageListItemPredicate {

    private val theirGiphyEphemeralMessagePredicate: (MessageListItem) -> Boolean = { item ->
        item is MessageListItem.MessageItem && item.message.isGiphyEphemeral() && item.isTheirs
    }

    override fun predicate(item: MessageListItem): Boolean = theirGiphyEphemeralMessagePredicate(item).not()
}
