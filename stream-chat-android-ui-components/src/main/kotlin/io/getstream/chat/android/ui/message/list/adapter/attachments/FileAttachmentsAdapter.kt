package io.getstream.chat.android.ui.message.list.adapter.attachments

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.ui.common.extensions.internal.doForAllViewHolders
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.databinding.StreamUiItemFileAttachmentBinding
import io.getstream.chat.android.ui.message.list.FileAttachmentViewStyle
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentDownloadClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentLongClickListener
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.FileAttachmentViewHolder

internal class FileAttachmentsAdapter(
    private val attachmentClickListener: AttachmentClickListener,
    private val attachmentLongClickListener: AttachmentLongClickListener,
    private val attachmentDownloadClickListener: AttachmentDownloadClickListener,
    private val style: FileAttachmentViewStyle,
) : SimpleListAdapter<AttachmentGroup, FileAttachmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileAttachmentViewHolder {
        return StreamUiItemFileAttachmentBinding
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
    }

    override fun onViewAttachedToWindow(holder: FileAttachmentViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.restartJob()
    }

    override fun onViewDetachedFromWindow(holder: FileAttachmentViewHolder) {
        holder.clearScope()
        super.onViewDetachedFromWindow(holder)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        doForAllViewHolders(recyclerView) { it.clearScope() }
        super.onDetachedFromRecyclerView(recyclerView)
    }
}
