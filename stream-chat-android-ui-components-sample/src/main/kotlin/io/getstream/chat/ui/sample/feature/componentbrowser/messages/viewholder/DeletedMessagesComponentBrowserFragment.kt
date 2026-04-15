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

package io.getstream.chat.ui.sample.feature.componentbrowser.messages.viewholder

import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.messages.list.MessagePosition
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import java.util.Date

class DeletedMessagesComponentBrowserFragment : BaseMessagesComponentBrowserFragment() {

    override fun getItems(): List<MessageListItem.MessageItem> {
        return listOf(
            MessageListItem.MessageItem(
                message = Message(deletedAt = Date()),
                position = MessagePosition.TOP,
                isMine = true,
            ),
            MessageListItem.MessageItem(
                message = Message(deletedAt = Date()),
                position = MessagePosition.MIDDLE,
                isMine = true,
            ),
            MessageListItem.MessageItem(
                message = Message(deletedAt = Date()),
                position = MessagePosition.BOTTOM,
                isMine = true,
            ),
        )
    }
}
