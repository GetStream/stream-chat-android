package io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.messages.adapter.viewholder.GiphyViewHolder

class GiphyMessageComponentBrowserFragment : BaseMessagesComponentBrowserFragment() {
    @OptIn(InternalStreamChatApi::class)
    override fun createAdapter(): RecyclerView.Adapter<*> {
        return DefaultAdapter(
            getDummyMessageList(),
            { viewGroup -> GiphyViewHolder(viewGroup, decorators, null) },
            GiphyViewHolder::bind
        )
    }

    @InternalStreamChatApi
    private fun getDummyMessageList(): List<MessageListItem.MessageItem> {
        return listOf(
            MessageListItem.MessageItem(
                message = Message(
                    text = "/giphy Victory",
                    type = ModelType.message_ephemeral,
                    command = ModelType.attach_giphy,
                    attachments = mutableListOf(
                        Attachment(
                            thumbUrl = "https://media4.giphy.com/media/o75ajIFH0QnQC3nCeD/giphy.gif",
                            type = ModelType.attach_giphy
                        )
                    )
                ),
                positions = listOf(MessageListItem.Position.TOP, MessageListItem.Position.BOTTOM),
                isMine = true
            )
        )
    }
}
