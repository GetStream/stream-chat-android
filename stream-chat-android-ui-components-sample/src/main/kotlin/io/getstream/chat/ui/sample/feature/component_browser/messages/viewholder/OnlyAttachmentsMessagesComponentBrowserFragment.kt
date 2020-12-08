package io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder

import android.content.ContentResolver
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.ui.sample.R

class OnlyAttachmentsMessagesComponentBrowserFragment : BaseMessagesComponentBrowserFragment() {

    override fun createAdapter(): RecyclerView.Adapter<*> {
        return DefaultAdapter(
            getDummyDeletedMessagesList(requireContext()),
            ::OnlyMediaAttachmentsViewHolder,
            OnlyMediaAttachmentsViewHolder::bind
        )
    }

    private fun getDummyDeletedMessagesList(context: Context): List<MessageListItem.MessageItem> {
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
                message = Message(attachments = mutableListOf(Attachment(type = "image", imageUrl = uri2))),
                positions = listOf(MessageListItem.Position.MIDDLE),
                isMine = true
            ),
            MessageListItem.MessageItem(
                message = Message(attachments = mutableListOf(Attachment(type = "image", imageUrl = uri3))),
                positions = listOf(MessageListItem.Position.BOTTOM),
                isMine = true
            ),
            MessageListItem.MessageItem(
                message = Message(attachments = mutableListOf(Attachment(type = "image", imageUrl = uri1))),
                positions = listOf(MessageListItem.Position.TOP, MessageListItem.Position.BOTTOM),
                isMine = false
            ),
            MessageListItem.MessageItem(
                message = Message(attachments = mutableListOf(Attachment(type = "image", imageUrl = uri1))),
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
