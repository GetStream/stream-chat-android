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
import com.getstream.sdk.chat.utils.extensions.getDisplayableName
import com.getstream.sdk.chat.utils.extensions.inflater
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiItemFileAttachmentBinding
import io.getstream.chat.android.ui.utils.SimpleListAdapter
import io.getstream.chat.android.ui.utils.UiUtils
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise

internal class FileAttachmentsView : RecyclerView {
    var attachmentClickListener: AttachmentClickListener? = null
    var attachmentLongClickListener: AttachmentLongClickListener? = null
    var attachmentDownloadClickListener: AttachmentDownloadClickListener? = null

    private val fileAttachmentsAdapter = FileAttachmentsAdapter(
        attachmentClickListener = { attachmentClickListener?.onAttachmentClick(it) },
        attachmentLongClickListener = { attachmentLongClickListener?.onAttachmentLongClick() },
        attachmentDownloadClickListener = { attachmentDownloadClickListener?.onAttachmentDownloadClick(it) }
    )

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        layoutManager = LinearLayoutManager(context)
        adapter = fileAttachmentsAdapter
        addItemDecoration(VerticalSpaceItemDecorator(4.dpToPx()))
    }

    fun setAttachments(attachments: List<Attachment>) {
        fileAttachmentsAdapter.setItems(attachments)
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
    private val attachmentClickListener: AttachmentClickListener,
    private val attachmentLongClickListener: AttachmentLongClickListener,
    private val attachmentDownloadClickListener: AttachmentDownloadClickListener
) : SimpleListAdapter<Attachment, FileAttachmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileAttachmentViewHolder {
        return StreamUiItemFileAttachmentBinding
            .inflate(parent.inflater, parent, false)
            .let {
                FileAttachmentViewHolder(
                    it,
                    attachmentClickListener,
                    attachmentLongClickListener,
                    attachmentDownloadClickListener
                )
            }
    }
}

private class FileAttachmentViewHolder(
    private val binding: StreamUiItemFileAttachmentBinding,
    private val attachmentClickListener: AttachmentClickListener,
    private val attachmentLongClickListener: AttachmentLongClickListener,
    private val attachmentDownloadClickListener: AttachmentDownloadClickListener
) : SimpleListAdapter.ViewHolder<Attachment>(binding.root) {
    private lateinit var attachment: Attachment

    init {
        binding.root.setOnClickListener {
            attachmentClickListener.onAttachmentClick(attachment)
        }
        binding.root.setOnLongClickListener {
            attachmentLongClickListener.onAttachmentLongClick()
            true
        }
        binding.actionButton.setOnClickListener {
            attachmentDownloadClickListener.onAttachmentDownloadClick(attachment)
        }
    }

    init {
        binding.root.background = ShapeAppearanceModel.builder()
            .setAllCornerSizes(CORNER_SIZE_PX)
            .build()
            .let(::MaterialShapeDrawable)
            .apply {
                setStroke(
                    STROKE_WIDTH_PX,
                    ContextCompat.getColor(itemView.context, R.color.stream_ui_border_stroke)
                )
                setTint(ContextCompat.getColor(itemView.context, R.color.stream_ui_background_light))
            }
    }

    override fun bind(item: Attachment) {
        this.attachment = item

        binding.apply {
            fileTypeIcon.setImageResource(UiUtils.getIcon(attachment.mimeType))
            fileTitle.text = attachment.getDisplayableName()
            fileSize.text = MediaStringUtil.convertFileSizeByteCount(attachment.fileSize.toLong())
        }
    }

    companion object {
        private val CORNER_SIZE_PX = 12.dpToPxPrecise()
        private val STROKE_WIDTH_PX = 1.dpToPxPrecise()
    }
}
