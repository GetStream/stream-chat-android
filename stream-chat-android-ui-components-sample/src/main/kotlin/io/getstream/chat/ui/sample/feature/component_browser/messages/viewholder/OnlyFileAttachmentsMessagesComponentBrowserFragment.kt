package io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder
import io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder.PlainTextWithFileAttachmentsMessagesComponentBrowserFragment.Companion.attachment7z
import io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder.PlainTextWithFileAttachmentsMessagesComponentBrowserFragment.Companion.attachmentDoc
import io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder.PlainTextWithFileAttachmentsMessagesComponentBrowserFragment.Companion.attachmentPdf
import io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder.PlainTextWithFileAttachmentsMessagesComponentBrowserFragment.Companion.attachmentPpt
import io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder.PlainTextWithFileAttachmentsMessagesComponentBrowserFragment.Companion.attachmentTxt
import io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder.PlainTextWithFileAttachmentsMessagesComponentBrowserFragment.Companion.attachmentXls

class OnlyFileAttachmentsMessagesComponentBrowserFragment : BaseMessagesComponentBrowserFragment() {

    override fun createAdapter(): RecyclerView.Adapter<*> {
        return DefaultAdapter(
            getDummyDeletedMessagesList(),
            { viewGroup -> OnlyFileAttachmentsViewHolder(viewGroup, decorators, EmptyMessageListListenerContainer) },
            OnlyFileAttachmentsViewHolder::bind
        )
    }

    @OptIn(InternalStreamChatApi::class)
    private fun getDummyDeletedMessagesList(): List<MessageListItem.MessageItem> {
        return listOf(
            MessageListItem.MessageItem(
                message = Message(attachments = mutableListOf(attachmentPdf)),
                positions = listOf(MessageListItem.Position.TOP),
                isMine = true
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(attachment7z, attachmentPdf)
                ),
                positions = listOf(MessageListItem.Position.MIDDLE),
                isMine = true
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(attachmentTxt, attachmentPdf, attachmentPpt)
                ),
                positions = listOf(MessageListItem.Position.BOTTOM),
                isMine = true
            ),
            MessageListItem.MessageItem(
                message = Message(attachments = mutableListOf(attachmentDoc, attachmentXls)),
                positions = listOf(MessageListItem.Position.TOP),
                isMine = false
            ),
            MessageListItem.MessageItem(
                message = Message(attachments = mutableListOf(attachmentXls, attachmentPdf, attachment7z)),
                positions = listOf(MessageListItem.Position.MIDDLE),
                isMine = false
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(
                        attachmentPpt,
                        attachment7z,
                        attachmentTxt,
                        attachmentDoc
                    )
                ),
                positions = listOf(MessageListItem.Position.BOTTOM),
                isMine = false
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
                    )
                ),
                positions = listOf(MessageListItem.Position.TOP, MessageListItem.Position.BOTTOM),
                isMine = true
            )
        )
    }
}
