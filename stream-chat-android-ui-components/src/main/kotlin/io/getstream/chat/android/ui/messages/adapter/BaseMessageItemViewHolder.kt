package io.getstream.chat.android.ui.messages.adapter

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.adapter.MessageListItemPayloadDiff
import com.getstream.sdk.chat.adapter.updateConstraints
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.BackgroundDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.GapDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.MaxPossibleWidthDecorator

public abstract class BaseMessageItemViewHolder<T : MessageListItem>(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {
    private val decorators = listOf<Decorator>(BackgroundDecorator(), GapDecorator(), MaxPossibleWidthDecorator())

    public fun bind(data: T, diff: MessageListItemPayloadDiff? = null) {
        decorators.forEach { it.decorate(this, data) }
        bindData(data, diff)
    }

    /**
     * Workaround to allow a downcast of the MessageListItem to T
     */
    @Suppress("UNCHECKED_CAST")
    internal fun bindListItem(messageListItem: MessageListItem, diff: MessageListItemPayloadDiff) =
        bind(messageListItem as T, diff)

    public abstract fun bindData(data: T, diff: MessageListItemPayloadDiff?)

    protected fun constraintView(isMine: Boolean, view: View, layout: ConstraintLayout) {
        layout.updateConstraints {
            clear(view.id, ConstraintSet.LEFT)
            clear(view.id, ConstraintSet.RIGHT)
            val anchorSide = if (isMine) ConstraintSet.RIGHT else ConstraintSet.LEFT
            connect(view.id, anchorSide, ConstraintSet.PARENT_ID, anchorSide)
        }
    }
}
