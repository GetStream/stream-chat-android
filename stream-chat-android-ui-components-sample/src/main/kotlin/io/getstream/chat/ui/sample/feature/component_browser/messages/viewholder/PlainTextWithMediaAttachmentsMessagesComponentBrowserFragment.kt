package io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder

import android.content.ContentResolver
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder
import io.getstream.chat.ui.sample.R

class PlainTextWithMediaAttachmentsMessagesComponentBrowserFragment : BaseMessagesComponentBrowserFragment() {

    override fun createAdapter(): RecyclerView.Adapter<*> {
        return DefaultAdapter(
            getDummyDeletedMessagesList(requireContext()),
            { viewGroup -> PlainTextWithMediaAttachmentsViewHolder(viewGroup, null) },
            PlainTextWithMediaAttachmentsViewHolder::bind
        )
    }

    private fun getDummyDeletedMessagesList(context: Context): List<MessageListItem.MessageItem> {
        val uri1 = drawableResToUri(context, R.drawable.stream_ui_sample_image_1)
        val uri2 = drawableResToUri(context, R.drawable.stream_ui_sample_image_2)
        val uri3 = drawableResToUri(context, R.drawable.stream_ui_sample_image_3)
        return listOf(
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(Attachment(type = "image", imageUrl = uri1)),
                    text = "Some text"
                ),
                positions = listOf(MessageListItem.Position.TOP),
                isMine = true
            ),
            MessageListItem.MessageItem(
                message = Message(
                    attachments = mutableListOf(
                        Attachment(type = "image", imageUrl = uri1),
                        Attachment(type = "image", imageUrl = uri2)
                    ),
                    text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
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
                    ),
                    text = "Hi!"
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
                    text = "Lorem ipsum dolor sit amet"
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
                    ),
                    text = "Another message"
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
                    ),
                    text = "Bye!!!"
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
                    ),
                    text = "Bye!!!"
                ),
                positions = listOf(MessageListItem.Position.TOP, MessageListItem.Position.BOTTOM),
                isMine = true
            )
        )
    }

    private fun drawableResToUri(context: Context, @DrawableRes drawableResId: Int): String {
        val res = context.resources
        return ContentResolver.SCHEME_ANDROID_RESOURCE +
            "://" + res.getResourcePackageName(drawableResId) +
            '/' + res.getResourceTypeName(drawableResId) +
            '/' + res.getResourceEntryName(drawableResId)
    }
}
