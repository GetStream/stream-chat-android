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

package io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.AttachmentType
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.MessageType
import io.getstream.chat.android.common.state.messagelist.MessagePosition
import io.getstream.chat.android.core.internal.InternalStreamChatApi

class GiphyMessageComponentBrowserFragment : BaseMessagesComponentBrowserFragment() {

    @OptIn(InternalStreamChatApi::class)
    override fun getItems(): List<MessageListItem.MessageItem> {
        return listOf(
            MessageListItem.MessageItem(
                message = Message(
                    text = "/giphy Victory",
                    type = MessageType.EPHEMERAL,
                    command = AttachmentType.GIPHY,
                    attachments = mutableListOf(
                        Attachment(
                            thumbUrl = "https://media4.giphy.com/media/o75ajIFH0QnQC3nCeD/giphy.gif",
                            type = AttachmentType.GIPHY
                        )
                    )
                ),
                positions = listOf(MessagePosition.TOP, MessagePosition.BOTTOM),
                isMine = true
            )
        )
    }
}
