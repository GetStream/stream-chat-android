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

import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.ui.common.state.messages.list.MessagePosition
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.feature.componentbrowser.utils.drawableResToUri

class PlainTextWithMediaAttachmentsMessagesComponentBrowserFragment : BaseMessagesComponentBrowserFragment() {

    override fun getItems(): List<MessageListItem.MessageItem> {
        val context = requireContext()
        val uri1 = drawableResToUri(context, R.drawable.stream_ui_sample_image_1)
        val uri2 = drawableResToUri(context, R.drawable.stream_ui_sample_image_2)
        val uri3 = drawableResToUri(context, R.drawable.stream_ui_sample_image_3)
        val attachmentLink = Attachment(
            titleLink = drawableResToUri(requireContext(), R.drawable.stream_ui_sample_image_1),
            title = "Title",
            text = "Some description",
            authorName = "Stream",
        )

        return listOf(
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(Attachment(type = "image", imageUrl = uri1)),
                    text = "Some text",
                ),
                positions = listOf(MessagePosition.TOP),
                isMine = true,
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(
                        Attachment(type = "image", imageUrl = uri1),
                        Attachment(type = "image", imageUrl = uri2),
                    ),
                    text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                ),
                positions = listOf(MessagePosition.MIDDLE),
                isMine = true,
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(
                        Attachment(type = "image", imageUrl = uri1),
                        Attachment(type = "image", imageUrl = uri2),
                        Attachment(type = "image", imageUrl = uri3),
                    ),
                    text = "Hi!",
                ),
                positions = listOf(MessagePosition.BOTTOM),
                isMine = true,
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(
                        Attachment(type = "image", imageUrl = uri1),
                        Attachment(type = "image", imageUrl = uri2),
                    ),
                    text = "Lorem ipsum dolor sit amet",
                ),
                positions = listOf(MessagePosition.TOP),
                isMine = false,
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(
                        Attachment(type = "image", imageUrl = uri2),
                        Attachment(type = "image", imageUrl = uri1),
                        Attachment(type = "image", imageUrl = uri3),
                    ),
                    text = "Another message",
                ),
                positions = listOf(MessagePosition.MIDDLE),
                isMine = false,
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(
                        Attachment(type = "image", imageUrl = uri1),
                        Attachment(type = "image", imageUrl = uri2),
                        Attachment(type = "image", imageUrl = uri3),
                        Attachment(type = "image", imageUrl = uri1),
                    ),
                    text = "Bye!!!",
                ),
                positions = listOf(MessagePosition.BOTTOM),
                isMine = false,
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
                    ),
                    text = "Bye!!!",
                ),
                positions = listOf(MessagePosition.TOP, MessagePosition.BOTTOM),
                isMine = true,
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(
                        Attachment(type = "image", imageUrl = uri1),
                        Attachment(type = "image", imageUrl = uri2),
                        Attachment(type = "image", imageUrl = uri3),
                    ),
                    text = "Hi!",
                    syncStatus = SyncStatus.FAILED_PERMANENTLY,
                ),
                positions = listOf(MessagePosition.BOTTOM),
                isMine = true,
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(
                        Attachment(type = "image", imageUrl = uri1),
                        Attachment(type = "image", imageUrl = uri2),
                        Attachment(type = "image", imageUrl = uri3),
                        attachmentLink,
                    ),
                    text = "Hi! https://www.google.com/",
                ),
                positions = listOf(MessagePosition.BOTTOM),
                isMine = true,
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(
                        Attachment(type = "image", imageUrl = uri1),
                        Attachment(type = "image", imageUrl = uri2),
                        Attachment(type = "image", imageUrl = uri3),
                        attachmentLink,
                    ),
                    text = "Hi! https://www.google.com/",
                ),
                positions = listOf(MessagePosition.BOTTOM),
                isMine = false,
            ),
        )
    }
}
