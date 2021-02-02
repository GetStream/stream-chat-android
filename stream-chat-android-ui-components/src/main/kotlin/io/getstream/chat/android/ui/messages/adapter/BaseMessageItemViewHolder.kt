package io.getstream.chat.android.ui.messages.adapter

import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.utils.animateHighlight

public abstract class BaseMessageItemViewHolder<T : MessageListItem>(
    itemView: View,
) : RecyclerView.ViewHolder(itemView) {

    /**
     * The data that was last bound to this ViewHolder via [bindData].
     * Can be used for listeners that need to pass along the currently
     * bound data as a parameter.
     */
    protected lateinit var data: T
        private set

    private var highlightAnimation: ValueAnimator? = null

    /**
     * Workaround to allow a downcast of the MessageListItem to T
     */
    @Suppress("UNCHECKED_CAST")
    internal fun bindListItem(messageListItem: MessageListItem, diff: MessageListItemPayloadDiff? = null) {
        messageListItem as T

        this.data = messageListItem
        bindData(messageListItem, diff)
    }

    public abstract fun bindData(data: T, diff: MessageListItemPayloadDiff?)

    @CallSuper
    public open fun unbind() {
        cancelHighlightAnimation()
    }

    internal fun startHighlightAnimation() {
        highlightAnimation = itemView.animateHighlight()
    }

    private fun cancelHighlightAnimation() {
        highlightAnimation?.cancel()
        highlightAnimation = null
    }

    protected val context: Context = itemView.context
}
