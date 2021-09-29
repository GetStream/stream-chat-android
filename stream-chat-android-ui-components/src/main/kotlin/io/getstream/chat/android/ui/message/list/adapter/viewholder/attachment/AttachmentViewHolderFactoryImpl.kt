package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import android.view.ViewGroup
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.common.extensions.internal.isMedia
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.databinding.StreamUiItemFileAttachmentBinding
import io.getstream.chat.android.ui.databinding.StreamUiItemImageAttachmentBinding
import io.getstream.chat.android.ui.message.list.FileAttachmentViewStyle
import io.getstream.chat.android.ui.message.list.adapter.attachments.AttachmentGroup
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentDownloadClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentLongClickListener

public open class AttachmentViewHolderFactoryImpl(
    private val attachmentClickListener: AttachmentClickListener,
    private val attachmentLongClickListener: AttachmentLongClickListener,
    private val attachmentDownloadClickListener: AttachmentDownloadClickListener,
    private val style: FileAttachmentViewStyle,
) : AttachmentViewHolderFactory {

    private lateinit var attachments: List<Attachment>

    override fun setUp(attachments: List<Attachment>) {
        this.attachments = attachments
    }

    override fun attachmentViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SimpleListAdapter.ViewHolder<AttachmentGroup> {
        return when {
            attachments.isMedia() -> StreamUiItemImageAttachmentBinding
                .inflate(parent.streamThemeInflater, parent, false)
                .let(::MediaAttachmentsViewHolder)

            attachments.isNotEmpty() -> StreamUiItemFileAttachmentBinding
                .inflate(parent.streamThemeInflater, parent, false)
                .let {
                    FileAttachmentViewHolder(
                        it,
                        attachmentClickListener,
                        attachmentLongClickListener,
                        attachmentDownloadClickListener,
                        style,
                    )
                }

            else -> error("Unsupported case for attachment adapter factory!")
        }
    }
}
