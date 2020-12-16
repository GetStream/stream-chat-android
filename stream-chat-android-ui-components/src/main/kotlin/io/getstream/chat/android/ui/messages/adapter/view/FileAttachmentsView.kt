package io.getstream.chat.android.ui.messages.adapter.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.inflater
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.databinding.StreamUiFileAttachmentItemBinding

internal class FileAttachmentsView : RecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        layoutManager = LinearLayoutManager(context)
    }

    fun setAttachments(attachments: List<Attachment>) {
        adapter = FileAttachmentsAdapter(attachments)
    }
}

private class FileAttachmentsAdapter(private val attachments: List<Attachment>) :
    RecyclerView.Adapter<FileAttachmentViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileAttachmentViewHolder {
        return StreamUiFileAttachmentItemBinding.inflate(parent.inflater, parent, false).let(::FileAttachmentViewHolder)
    }

    override fun onBindViewHolder(holder: FileAttachmentViewHolder, position: Int) {
        holder.bind(attachments[position])
    }

    override fun getItemCount(): Int = attachments.size
}

private class FileAttachmentViewHolder(private val binding: StreamUiFileAttachmentItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(attachment: Attachment) {
    }
}