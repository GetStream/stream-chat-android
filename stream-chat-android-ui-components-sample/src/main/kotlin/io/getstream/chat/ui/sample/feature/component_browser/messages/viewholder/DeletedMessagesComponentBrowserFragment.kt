package io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessageDeletedViewHolder
import java.util.Date

class DeletedMessagesComponentBrowserFragment : BaseMessagesComponentBrowserFragment() {

    override fun createAdapter(): RecyclerView.Adapter<*> {
        return DefaultAdapter(
            getDummyDeletedMessagesList(),
            { parent -> MessageDeletedViewHolder(parent, currentUser) },
            MessageDeletedViewHolder::bind
        )
    }

    private fun getDummyDeletedMessagesList(): List<MessageListItem.MessageItem> {
        return listOf(
            MessageListItem.MessageItem(
                message = Message(deletedAt = Date()),
                positions = listOf(MessageListItem.Position.TOP),
                isMine = true
            ),
            MessageListItem.MessageItem(
                message = Message(deletedAt = Date()),
                positions = listOf(MessageListItem.Position.MIDDLE),
                isMine = true
            ),
            MessageListItem.MessageItem(
                message = Message(deletedAt = Date()),
                positions = listOf(MessageListItem.Position.BOTTOM),
                isMine = true
            ),
            MessageListItem.MessageItem(
                message = Message(deletedAt = Date()),
                positions = listOf(MessageListItem.Position.TOP, MessageListItem.Position.BOTTOM),
                isMine = false
            ),
            MessageListItem.MessageItem(
                message = Message(deletedAt = Date()),
                positions = listOf(MessageListItem.Position.TOP, MessageListItem.Position.BOTTOM),
                isMine = true
            )
        )
    }
}
