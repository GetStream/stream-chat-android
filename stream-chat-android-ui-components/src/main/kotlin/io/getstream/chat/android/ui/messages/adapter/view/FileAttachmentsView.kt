package io.getstream.chat.android.ui.messages.adapter.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.utils.MediaStringUtil
import com.getstream.sdk.chat.utils.extensions.inflater
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiItemFileAttachmentBinding
import io.getstream.chat.android.ui.utils.UiUtils
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise

internal class FileAttachmentsView : RecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        layoutManager = LinearLayoutManager(context)
        addItemDecoration(VerticalSpaceItemDecorator(4.dpToPx()))
    }

    fun setAttachments(attachments: List<Attachment>, listener: (Attachment) -> Unit) {
        adapter = FileAttachmentsAdapter(attachments, listener)
    }
}

private class VerticalSpaceItemDecorator(private val marginPx: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        parent.adapter?.let { adapter ->
            if (parent.getChildAdapterPosition(view) != adapter.itemCount - 1) {
                outRect.bottom = marginPx
            }
        }

    }
}

private class FileAttachmentsAdapter(
    private val attachments: List<Attachment>,
    private val listener: (Attachment) -> Unit = {}
) : RecyclerView.Adapter<FileAttachmentViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileAttachmentViewHolder {
        return FileAttachmentViewHolder(
            StreamUiItemFileAttachmentBinding.inflate(parent.inflater, parent, false),
            listener
        )
    }

    override fun onBindViewHolder(holder: FileAttachmentViewHolder, position: Int) {
        holder.bind(attachments[position])
    }

    override fun getItemCount(): Int = attachments.size
}

private class FileAttachmentViewHolder(
    private val binding: StreamUiItemFileAttachmentBinding,
    private val listener: (Attachment) -> Unit
) :
    RecyclerView.ViewHolder(binding.root) {

    init {
        binding.root.background =
            ShapeAppearanceModel.builder().setAllCornerSizes(CORNER_SIZE_PX).build().let(::MaterialShapeDrawable)
                .apply {
                    setStroke(
                        STROKE_WIDTH_PX,
                        ContextCompat.getColor(itemView.context, R.color.stream_ui_border_stroke)
                    )
                    setTint(ContextCompat.getColor(itemView.context, R.color.stream_ui_white))
                }
    }

    fun bind(attachment: Attachment) {
        binding.fileTypeIcon.setImageResource(UiUtils.getIcon(attachment.mimeType))
        binding.fileTitle.text = attachment.title ?: attachment.name
        binding.fileSize.text = MediaStringUtil.convertFileSizeByteCount(attachment.fileSize.toLong())
        binding.root.setOnClickListener { listener(attachment) }
    }

    companion object {
        private val CORNER_SIZE_PX = 12.dpToPxPrecise()
        private val STROKE_WIDTH_PX = 1.dpToPxPrecise()
    }
}
