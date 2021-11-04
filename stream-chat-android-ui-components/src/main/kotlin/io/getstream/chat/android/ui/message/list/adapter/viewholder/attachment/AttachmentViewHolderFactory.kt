package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.common.extensions.internal.isMedia
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiItemFileAttachmentGroupBinding
import io.getstream.chat.android.ui.databinding.StreamUiItemImageAttachmentBinding
import io.getstream.chat.android.ui.message.list.FileAttachmentViewStyle
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentDownloadClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentLongClickListener

public open class AttachmentViewHolderFactory(
    private val attachmentClickListener: AttachmentClickListener,
    private val attachmentLongClickListener: AttachmentLongClickListener,
    private val attachmentDownloadClickListener: AttachmentDownloadClickListener,
    private val style: FileAttachmentViewStyle,
) {

    public companion object {
        private const val BUILT_IN_TYPES = 9000

        public const val MEDIA: Int = BUILT_IN_TYPES + 1
        public const val FILE: Int = BUILT_IN_TYPES + 2
    }

    public open fun createViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): AttachmentViewHolder {
        return when (viewType) {
            MEDIA -> {
                StreamUiItemImageAttachmentBinding
                    .inflate(parent.streamThemeInflater, parent, false)
                    .let(::MediaAttachmentsViewHolder)
            }

            FILE -> {
                StreamUiItemFileAttachmentGroupBinding
                    .inflate(parent.streamThemeInflater, parent, false)
                    .let {
                        FileGroupAttachmentViewHolder(
                            it,
                            attachmentClickListener,
                            attachmentLongClickListener,
                            attachmentDownloadClickListener,
                            style,
                        )
                    }
            }

            else -> error("This view type: $viewType is not supported")
        }
    }

    public open fun getItemViewType(attachments: List<Attachment>): Int {
        return when {
            attachments.isMedia() -> MEDIA
            else -> FILE
        }
    }
}

public abstract class AttachmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    public abstract fun bind(attachments: List<Attachment>)
    public open fun unbind() {}
}
