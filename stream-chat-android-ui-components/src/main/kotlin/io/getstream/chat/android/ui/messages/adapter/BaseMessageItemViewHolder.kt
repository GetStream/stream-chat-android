package io.getstream.chat.android.ui.messages.adapter

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.BackgroundDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.GapDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.MaxPossibleWidthDecorator

public abstract class BaseMessageItemViewHolder<T : MessageListItem>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val decorators = listOf<Decorator>(BackgroundDecorator(), GapDecorator(), MaxPossibleWidthDecorator())

    public fun bind(data: T) {
        decorators.forEach { it.decorate(this, data) }
        bindData(data)
    }

    public abstract fun bindData(data: T)

    protected fun constraintView(isMine: Boolean, view: View, layout: ConstraintLayout) {
        ConstraintSet().apply {
            clone(layout)
            clear(view.id, ConstraintSet.LEFT)
            clear(view.id, ConstraintSet.RIGHT)
            val anchorSide = if (isMine) ConstraintSet.END else ConstraintSet.START
            connect(view.id,  anchorSide, ConstraintSet.PARENT_ID, anchorSide)
            applyTo(layout)
        }
    }
}
