package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.Message

/**
 * ViewHolder base class for attachments displayed within message items.
 *
 * Create your own implementation of this ViewHolder and then subclass
 * [AttachmentViewHolderFactory] to add custom attachment types to the
 * message list.
 */
public abstract class AttachmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    /**
     * Called when a new list of attachments contained within the message
     * item should be displayed on the View contained by the ViewHolder.
     *
     * Note: while you may use all of the data to display the attachment
     * contents, this binding only be re-triggered if the `attachments`
     * inside the contained [Message] change.
     */
    public abstract fun bind(data: MessageListItem.MessageItem)

    /**
     * Called when the view is no longer displayed on the UI.
     *
     * Use this method to free up resources consumed by the attachment
     * view, if any.
     */
    public open fun unbind() {}
}
