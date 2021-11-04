package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import android.view.ViewGroup
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.common.adapters.SimpleListAdapter
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
        public const val MEDIA: Int = 1
        public const val FILE: Int = 2
    }

    public fun createAttachmentViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SimpleListAdapter.ViewHolder<List<Attachment>> {
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

}
