package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import android.content.Context
import android.view.View
import io.getstream.chat.android.client.models.Message

/**
 *
 */
public open class AttachmentViewHolder(public val itemView: View) {
    public val context: Context = itemView.context

    /**
     *
     */
    public open fun onBindViewHolder(message: Message) {}

    /**
     *
     */
    public open fun onUnbindViewHolder() {}

    /**
     *
     */
    public open fun onViewAttachedToWindow() {}

    /**
     *
     */
    public open fun onViewDetachedFromWindow() {}
}