package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.adapter.constrainViewEndToEndOfView
import com.getstream.sdk.chat.adapter.constrainViewStartToEndOfView
import com.getstream.sdk.chat.adapter.updateConstraints
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder

internal class GravityDecorator : BaseDecorator() {
    override fun decoratePlainTextMessage(viewHolder: MessagePlainTextViewHolder, data: MessageListItem.MessageItem) {
        viewHolder.binding.root.updateConstraints {
            applyGravity(viewHolder.binding.messageText, viewHolder.binding.avatarView, viewHolder.binding.marginEnd, data)
            applyGravity(viewHolder.binding.tvTime, viewHolder.binding.avatarView, viewHolder.binding.marginEnd, data)
        }
    }

    override fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        viewHolder.binding.root.updateConstraints {
            applyGravity(viewHolder.binding.tvTime, viewHolder.binding.avatarView, viewHolder.binding.marginEnd, data)
        }
    }

    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) {
        viewHolder.binding.root.updateConstraints {
            applyGravity(viewHolder.binding.tvTime, viewHolder.binding.avatarView, viewHolder.binding.marginEnd, data)
        }
    }

    private fun ConstraintSet.applyGravity(targetView: View, startView: View, endView: View, data: MessageListItem.MessageItem) {
        clear(targetView.id, ConstraintSet.START)
        clear(targetView.id, ConstraintSet.END)
        if (data.isTheirs) {
            constrainViewStartToEndOfView(targetView, startView)
        } else {
            constrainViewEndToEndOfView(targetView, endView)
        }
    }
}
