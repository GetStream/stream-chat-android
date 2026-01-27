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

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.messages.list.MessagePosition
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.feature.componentbrowser.messages.viewholder.PlainTextWithFileAttachmentsMessagesComponentBrowserFragment.Companion.attachment7z
import io.getstream.chat.ui.sample.feature.componentbrowser.messages.viewholder.PlainTextWithFileAttachmentsMessagesComponentBrowserFragment.Companion.attachmentDoc
import io.getstream.chat.ui.sample.feature.componentbrowser.messages.viewholder.PlainTextWithFileAttachmentsMessagesComponentBrowserFragment.Companion.attachmentPdf
import io.getstream.chat.ui.sample.feature.componentbrowser.messages.viewholder.PlainTextWithFileAttachmentsMessagesComponentBrowserFragment.Companion.attachmentPpt
import io.getstream.chat.ui.sample.feature.componentbrowser.messages.viewholder.PlainTextWithFileAttachmentsMessagesComponentBrowserFragment.Companion.attachmentTxt
import io.getstream.chat.ui.sample.feature.componentbrowser.messages.viewholder.PlainTextWithFileAttachmentsMessagesComponentBrowserFragment.Companion.attachmentXls
import io.getstream.chat.ui.sample.feature.componentbrowser.utils.drawableResToUri
import io.getstream.chat.ui.sample.feature.componentbrowser.utils.randomUser

class RepliedMessagesComponentBrowserFragment : BaseMessagesComponentBrowserFragment() {

    @OptIn(InternalStreamChatApi::class)
    override fun getItems(): List<MessageListItem.MessageItem> {
        val context = requireContext()
        val uri1 = drawableResToUri(context, R.drawable.stream_ui_sample_image_1)
        val uri2 = drawableResToUri(context, R.drawable.stream_ui_sample_image_2)
        val uri3 = drawableResToUri(context, R.drawable.stream_ui_sample_image_3)

        val me = currentUser
        val other = randomUser()

        val theirMessage = Message(
            attachments = mutableListOf(
                Attachment(type = "image", imageUrl = uri1),
                Attachment(type = "image", imageUrl = uri2),
                Attachment(type = "image", imageUrl = uri3),
                Attachment(type = "image", imageUrl = uri1),
                Attachment(type = "image", imageUrl = uri2),
                Attachment(type = "image", imageUrl = uri3),
            ),
            text = "Bye!!!",
            user = other,
        )

        return listOf(
            MessageListItem.MessageItem(
                message = Message(
                    text = "Wow",
                    user = me,
                    replyTo = Message(
                        text = "Some long-long, super long text which is much longer that original post",
                        user = me,
                    ),
                ),
                isMine = true,
                position = MessagePosition.TOP,
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(Attachment(type = "image", imageUrl = uri1)),
                    text = "Some text",
                    user = me,
                    replyTo = Message(text = "Text from reply message", user = other),
                ),
                position = MessagePosition.TOP,
                isMine = true,
            ),
            MessageListItem.MessageItem(
                message = Message(
                    text = "Hey! Nice thing!!!",
                    user = me,
                    replyTo = Message(
                        attachments = mutableListOf(
                            Attachment(type = "image", imageUrl = uri1),
                            Attachment(type = "image", imageUrl = uri2),
                        ),
                        text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                        user = other,
                    ),
                ),
                position = MessagePosition.MIDDLE,
                isMine = true,
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(attachmentTxt, attachmentPdf, attachmentPpt),
                    text = "Hi!",
                    user = me,
                    replyTo = Message(
                        attachments = mutableListOf(
                            Attachment(type = "image", imageUrl = uri1),
                            Attachment(type = "image", imageUrl = uri2),
                            Attachment(type = "image", imageUrl = uri3),
                        ),
                        text = "Hi!",
                        user = other,
                    ),
                ),
                position = MessagePosition.BOTTOM,
                isMine = true,
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(
                        Attachment(type = "image", imageUrl = uri1),
                        Attachment(type = "image", imageUrl = uri2),
                    ),
                    user = other,
                    replyTo = Message(
                        text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                        user = me,
                    ),
                ),
                position = MessagePosition.TOP,
                isMine = false,
            ),
            MessageListItem.MessageItem(
                message = theirMessage,
                position = MessagePosition.TOP,
                isMine = false,
            ),
            MessageListItem.MessageItem(
                message = Message(
                    user = me,
                    attachments = mutableListOf(
                        attachmentPdf,
                        attachmentPpt,
                        attachment7z,
                        attachmentTxt,
                        attachmentDoc,
                        attachmentXls,
                    ),
                    text = "Bye!!!",
                    replyTo = theirMessage,
                ),
                position = MessagePosition.TOP,
                isMine = true,
            ),
        )
    }
}
