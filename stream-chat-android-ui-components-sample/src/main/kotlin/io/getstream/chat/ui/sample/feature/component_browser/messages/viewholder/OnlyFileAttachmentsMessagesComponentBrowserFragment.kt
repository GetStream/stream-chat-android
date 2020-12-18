package io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder

class OnlyFileAttachmentsMessagesComponentBrowserFragment : BaseMessagesComponentBrowserFragment() {

    override fun createAdapter(): RecyclerView.Adapter<*> {
        return DefaultAdapter(
            getDummyDeletedMessagesList(),
            { viewGroup -> OnlyFileAttachmentsViewHolder(viewGroup, null) },
            OnlyFileAttachmentsViewHolder::bind
        )
    }

    @OptIn(InternalStreamChatApi::class)
    private fun getDummyDeletedMessagesList(): List<MessageListItem.MessageItem> {
        val attachmentPdf = Attachment(
            type = "file",
            mimeType = ModelType.attach_mime_pdf,
            fileSize = 120.kiloBytes(),
            title = "Sample pdf file"
        )
        val attachmentPpt = Attachment(
            type = "file",
            mimeType = ModelType.attach_mime_ppt,
            fileSize = 567.kiloBytes(),
            title = "Sample ppt file"
        )
        val attachment7z = Attachment(
            type = "file",
            mimeType = ModelType.attach_mime_7z,
            fileSize = 1920.kiloBytes(),
            title = "Sample archive file"
        )
        val attachmentTxt = Attachment(
            type = "file",
            mimeType = ModelType.attach_mime_txt,
            fileSize = 18.kiloBytes(),
            title = "Sample text file"
        )
        val attachmentDoc = Attachment(
            type = "file",
            mimeType = ModelType.attach_mime_doc,
            fileSize = 89.kiloBytes(),
            title = "Sample doc file"
        )
        val attachmentXls = Attachment(
            type = "file",
            mimeType = ModelType.attach_mime_xls,
            fileSize = 5234.kiloBytes(),
            title = "Sample xls file"
        )
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

    companion object {
        private const val KILOBYTE = 1024 * 1024
        private fun Int.kiloBytes() = this * KILOBYTE
    }
}
