package io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.DateFormatter
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.MessageListItemDecoratorProvider
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewHolderFactory
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewTypeMapper
import io.getstream.chat.android.ui.messages.adapter.initEmptyListeners
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder.PlainTextWithFileAttachmentsMessagesComponentBrowserFragment.Companion.attachment7z
import io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder.PlainTextWithFileAttachmentsMessagesComponentBrowserFragment.Companion.attachmentDoc
import io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder.PlainTextWithFileAttachmentsMessagesComponentBrowserFragment.Companion.attachmentPdf
import io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder.PlainTextWithFileAttachmentsMessagesComponentBrowserFragment.Companion.attachmentPpt
import io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder.PlainTextWithFileAttachmentsMessagesComponentBrowserFragment.Companion.attachmentTxt
import io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder.PlainTextWithFileAttachmentsMessagesComponentBrowserFragment.Companion.attachmentXls
import io.getstream.chat.ui.sample.feature.component_browser.utils.drawableResToUri
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomUser

class RepliedMessagesComponentBrowserFragment : BaseMessagesComponentBrowserFragment() {

    @OptIn(InternalStreamChatApi::class)
    override fun createAdapter(): RecyclerView.Adapter<*> {
        val factory = MessageListItemViewHolderFactory(
            MessageListItemDecoratorProvider(
                currentUser = currentUser,
                dateFormatter = DateFormatter.from(requireContext()),
                isDirectMessage = false,
            )
        )
        factory.initEmptyListeners()
        return SampleAdapter(getDummyDeletedMessagesList(requireContext()), factory)
    }

    @InternalStreamChatApi
    private fun getDummyDeletedMessagesList(context: Context): List<MessageListItem.MessageItem> {
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
            user = other
        )

        return listOf(
            MessageListItem.MessageItem(
                message = Message(
                    text = "Wow",
                    user = me,
                    replyTo = Message(
                        text = "Some long-long, super long text which is much longer that original post",
                        user = me
                    )
                ),
                isMine = true,
                positions = listOf(MessageListItem.Position.TOP, MessageListItem.Position.BOTTOM)
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(Attachment(type = "image", imageUrl = uri1)),
                    text = "Some text",
                    user = me,
                    replyTo = Message(text = "Text from reply message", user = other)
                ),
                positions = listOf(MessageListItem.Position.TOP),
                isMine = true,
            ),
            MessageListItem.MessageItem(
                message = Message(
                    text = "Hey! Nice thing!!!",
                    user = me,
                    replyTo = Message(
                        attachments = mutableListOf(
                            Attachment(type = "image", imageUrl = uri1),
                            Attachment(type = "image", imageUrl = uri2)
                        ),
                        text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                        user = other
                    )
                ),
                positions = listOf(MessageListItem.Position.MIDDLE),
                isMine = true
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
                            Attachment(type = "image", imageUrl = uri3)
                        ),
                        text = "Hi!",
                        user = other
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
                    ),
                    user = other,
                    replyTo = Message(
                        text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                        user = me
                    )
                ),
                positions = listOf(MessageListItem.Position.TOP, MessageListItem.Position.BOTTOM),
                isMine = false
            ),
            MessageListItem.MessageItem(
                message = theirMessage,
                positions = listOf(MessageListItem.Position.TOP, MessageListItem.Position.BOTTOM),
                isMine = false
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
                    replyTo = theirMessage
                ),
                positions = listOf(MessageListItem.Position.TOP, MessageListItem.Position.BOTTOM),
                isMine = true
            ),
        )
    }

    @InternalStreamChatApi
    private class SampleAdapter(
        private val items: List<MessageListItem.MessageItem>,
        private val vhFactory: MessageListItemViewHolderFactory,
    ) : RecyclerView.Adapter<BaseMessageItemViewHolder<MessageListItem.MessageItem>>() {

        @Suppress("UNCHECKED_CAST")
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): BaseMessageItemViewHolder<MessageListItem.MessageItem> {
            return vhFactory.createViewHolder(
                parent,
                viewType
            ) as BaseMessageItemViewHolder<MessageListItem.MessageItem>
        }

        override fun onBindViewHolder(holder: BaseMessageItemViewHolder<MessageListItem.MessageItem>, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        override fun getItemViewType(position: Int): Int {
            return MessageListItemViewTypeMapper.getViewTypeValue(items[position])
        }
    }
}
