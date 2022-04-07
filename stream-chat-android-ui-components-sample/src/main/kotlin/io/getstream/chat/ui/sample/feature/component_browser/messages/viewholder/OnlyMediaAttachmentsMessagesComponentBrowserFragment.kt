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
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.feature.component_browser.utils.drawableResToUri

class OnlyMediaAttachmentsMessagesComponentBrowserFragment : BaseMessagesComponentBrowserFragment() {

    override fun getItems(): List<MessageListItem.MessageItem> {
        val context = requireContext()
        val uri1 = drawableResToUri(context, R.drawable.stream_ui_sample_image_1)
        val uri2 = drawableResToUri(context, R.drawable.stream_ui_sample_image_2)
        val uri3 = drawableResToUri(context, R.drawable.stream_ui_sample_image_3)
        return listOf(
            MessageListItem.MessageItem(
                message = Message(attachments = mutableListOf(Attachment(type = "image", imageUrl = uri1))),
                positions = listOf(MessageListItem.Position.TOP),
                isMine = true
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(
                        Attachment(type = "image", imageUrl = uri1),
                        Attachment(type = "image", imageUrl = uri2)
                    )
                ),
                positions = listOf(MessageListItem.Position.MIDDLE),
                isMine = true
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(
                        Attachment(type = "image", imageUrl = uri1),
                        Attachment(type = "image", imageUrl = uri2),
                        Attachment(type = "image", imageUrl = uri3)
                    )
                ),
                positions = listOf(MessageListItem.Position.BOTTOM),
                isMine = true
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(
                        Attachment(type = "image", imageUrl = uri1),
                        Attachment(type = "image", imageUrl = uri2)
                    )
                ),
                positions = listOf(MessageListItem.Position.TOP),
                isMine = false
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(
                        Attachment(type = "image", imageUrl = uri2),
                        Attachment(type = "image", imageUrl = uri1),
                        Attachment(type = "image", imageUrl = uri3)
                    )
                ),
                positions = listOf(MessageListItem.Position.MIDDLE),
                isMine = false
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(
                        Attachment(type = "image", imageUrl = uri1),
                        Attachment(type = "image", imageUrl = uri2),
                        Attachment(type = "image", imageUrl = uri3),
                        Attachment(type = "image", imageUrl = uri1)
                    )
                ),
                positions = listOf(MessageListItem.Position.BOTTOM),
                isMine = false
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(
                        Attachment(type = "image", imageUrl = uri1),
                        Attachment(type = "image", imageUrl = uri2),
                        Attachment(type = "image", imageUrl = uri3),
                        Attachment(type = "image", imageUrl = uri1),
                        Attachment(type = "image", imageUrl = uri2),
                        Attachment(type = "image", imageUrl = uri3),
                    )
                ),
                positions = listOf(MessageListItem.Position.TOP, MessageListItem.Position.BOTTOM),
                isMine = true
            )
        )
    }
}
