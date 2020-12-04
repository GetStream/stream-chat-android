package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.databinding.StreamItemMessagePlainTextBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder

public class MessagePlainTextViewHolder(
    parent: ViewGroup,
    internal val binding: StreamItemMessagePlainTextBinding =
        StreamItemMessagePlainTextBinding.inflate(
            LayoutInflater.from(
                parent.context
            ),
            parent,
            false
        )
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root) {

    override fun bindData(data: MessageListItem.MessageItem) {
        (binding.messageText.layoutParams as LinearLayout.LayoutParams).gravity = if (data.isMine) {
            Gravity.END
        } else {
            Gravity.START
        }
        binding.messageText.text = data.message.text
    }
}
