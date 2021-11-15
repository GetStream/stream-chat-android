package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import android.view.ViewGroup
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.common.extensions.internal.isMedia
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiItemFileAttachmentGroupBinding
import io.getstream.chat.android.ui.databinding.StreamUiItemImageAttachmentBinding
import io.getstream.chat.android.ui.message.list.FileAttachmentViewStyle
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentDownloadClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentLongClickListener

/**
 * Factory for creating the attachment contents displayed within message items.
 *
 * Displays media and file attachments by default.
 */
public open class AttachmentViewHolderFactory(
    private val attachmentClickListener: AttachmentClickListener,
    private val attachmentLongClickListener: AttachmentLongClickListener,
    private val attachmentDownloadClickListener: AttachmentDownloadClickListener,
    private val style: FileAttachmentViewStyle,
) {

    public companion object {
        private const val BUILT_IN_TYPES = 9000

        /**
         * The media attachment type.
         */
        public const val MEDIA: Int = BUILT_IN_TYPES + 1

        /**
         * The file attachment type.
         */
        public const val FILE: Int = BUILT_IN_TYPES + 2
    }

    /**
     * Determine the view type to be used for the given list of attachments.
     *
     * Supports media and file attachments by default. Make sure to call into the
     * super implementation when overriding this method if you want these default
     * attachment types to be handled.
     */
    public open fun getItemViewType(attachments: List<Attachment>): Int {
        // TODO update the logic for rendering attachments as files - match Compose implementation?
        return when {
            attachments.isMedia() -> MEDIA
            else -> FILE
        }
    }

    /**
     * Create a ViewHolder for the given [viewType].
     *
     * Supports media and file attachments by default. Make sure to call into the
     * super implementation when overriding this method if you want these default
     * attachment types to be handled.
     */
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
}
