package io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import java.util.Date

class PlainTextMessagesComponentBrowserFragment : BaseMessagesComponentBrowserFragment() {
    override fun createAdapter(): RecyclerView.Adapter<*> {
        return DefaultAdapter(
            getDummyMessageList(),
            { viewGroup -> MessagePlainTextViewHolder(viewGroup, null) },
            MessagePlainTextViewHolder::bind
        )
    }

    private fun getDummyMessageList(): List<MessageListItem.MessageItem> {
        val date = Date()
        return listOf(
            MessageListItem.MessageItem(
                message = Message(text = "Lorem ipsum dolor"),
                positions = listOf(MessageListItem.Position.TOP),
                isMine = true
            ),
            MessageListItem.MessageItem(
                message = Message(text = "sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."),
                positions = listOf(MessageListItem.Position.MIDDLE),
                isMine = true
            ),
            MessageListItem.MessageItem(
                message = Message(text = "Ut enim ad minim veniam", createdAt = date),
                positions = listOf(MessageListItem.Position.BOTTOM),
                isMine = true
            ),
            MessageListItem.MessageItem(
                message = Message(text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit", createdAt = date),
                positions = listOf(MessageListItem.Position.TOP, MessageListItem.Position.BOTTOM),
                isMine = false
            ),
            MessageListItem.MessageItem(
                message = Message(text = "Whaaat?", createdAt = date),
                positions = listOf(MessageListItem.Position.TOP, MessageListItem.Position.BOTTOM),
                isMine = true
            ),
            MessageListItem.MessageItem(
                message = Message(text = "Ephemeral", createdAt = date, type = "ephemeral"),
                positions = listOf(MessageListItem.Position.TOP, MessageListItem.Position.BOTTOM),
                isMine = true,
            ),
        )
    }
}
