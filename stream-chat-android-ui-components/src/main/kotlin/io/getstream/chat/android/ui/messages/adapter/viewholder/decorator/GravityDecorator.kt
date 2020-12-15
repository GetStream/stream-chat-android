package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import androidx.constraintlayout.widget.ConstraintSet
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.adapter.updateConstraints
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder

internal class GravityDecorator : BaseDecorator() {
    override fun decoratePlainTextMessage(viewHolder: MessagePlainTextViewHolder, data: MessageListItem.MessageItem) {
        viewHolder.binding.root.updateConstraints {
            val messageViewId = viewHolder.binding.messageText.id
            clear(messageViewId, ConstraintSet.START)
            clear(messageViewId, ConstraintSet.END)
            if (data.isTheirs) {
                connect(messageViewId, ConstraintSet.START, viewHolder.binding.marginStart.id, ConstraintSet.START)
            } else {
                connect(messageViewId, ConstraintSet.END, viewHolder.binding.marginEnd.id, ConstraintSet.END)
            }
        }
    }
}
