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
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.messages.list.MessagePosition
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.ui.sample.feature.componentbrowser.messages.viewholder.PlainTextWithFileAttachmentsMessagesComponentBrowserFragment.Companion.attachment7z
import io.getstream.chat.ui.sample.feature.componentbrowser.messages.viewholder.PlainTextWithFileAttachmentsMessagesComponentBrowserFragment.Companion.attachmentDoc
import io.getstream.chat.ui.sample.feature.componentbrowser.messages.viewholder.PlainTextWithFileAttachmentsMessagesComponentBrowserFragment.Companion.attachmentPdf
import io.getstream.chat.ui.sample.feature.componentbrowser.messages.viewholder.PlainTextWithFileAttachmentsMessagesComponentBrowserFragment.Companion.attachmentPpt
import io.getstream.chat.ui.sample.feature.componentbrowser.messages.viewholder.PlainTextWithFileAttachmentsMessagesComponentBrowserFragment.Companion.attachmentTxt
import io.getstream.chat.ui.sample.feature.componentbrowser.messages.viewholder.PlainTextWithFileAttachmentsMessagesComponentBrowserFragment.Companion.attachmentXls

class OnlyFileAttachmentsMessagesComponentBrowserFragment : BaseMessagesComponentBrowserFragment() {

    @OptIn(InternalStreamChatApi::class)
    override fun getItems(): List<MessageListItem.MessageItem> {
        return listOf(
            MessageListItem.MessageItem(
                message = Message(attachments = mutableListOf(attachmentPdf)),
                positions = listOf(MessagePosition.TOP),
                isMine = true,
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(attachment7z, attachmentPdf),
                ),
                positions = listOf(MessagePosition.MIDDLE),
                isMine = true,
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(attachmentTxt, attachmentPdf, attachmentPpt),
                ),
                positions = listOf(MessagePosition.BOTTOM),
                isMine = true,
            ),
            MessageListItem.MessageItem(
                message = Message(attachments = mutableListOf(attachmentDoc, attachmentXls)),
                positions = listOf(MessagePosition.TOP),
                isMine = false,
            ),
            MessageListItem.MessageItem(
                message = Message(attachments = mutableListOf(attachmentXls, attachmentPdf, attachment7z)),
                positions = listOf(MessagePosition.MIDDLE),
                isMine = false,
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(
                        attachmentPpt,
                        attachment7z,
                        attachmentTxt,
                        attachmentDoc,
                    ),
                ),
                positions = listOf(MessagePosition.BOTTOM),
                isMine = false,
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(
                        attachmentPdf,
                        attachmentPpt,
                        attachment7z,
                        attachmentTxt,
                        attachmentDoc,
                        attachmentXls,
                    ),
                ),
                positions = listOf(MessagePosition.TOP, MessagePosition.BOTTOM),
                isMine = true,
            ),
        )
    }
}
