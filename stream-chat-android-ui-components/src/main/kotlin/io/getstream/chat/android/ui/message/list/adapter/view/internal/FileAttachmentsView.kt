package io.getstream.chat.android.ui.message.list.adapter.view.internal

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.utils.MediaStringUtil
import com.getstream.sdk.chat.utils.extensions.getDisplayableName
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.doForAllViewHolders
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.common.extensions.internal.dpToPxPrecise
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.common.internal.loadAttachmentThumb
import io.getstream.chat.android.ui.common.style.setTextStyle
import io.getstream.chat.android.ui.databinding.StreamUiItemFileAttachmentBinding
import io.getstream.chat.android.ui.message.list.FileAttachmentViewStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

internal class FileAttachmentsView : RecyclerView {
    var attachmentClickListener: AttachmentClickListener? = null
    var attachmentLongClickListener: AttachmentLongClickListener? = null
    var attachmentDownloadClickListener: AttachmentDownloadClickListener? = null

    private lateinit var style: FileAttachmentViewStyle

    private lateinit var fileAttachmentsAdapter: FileAttachmentsAdapter

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    init {
        layoutManager = LinearLayoutManager(context)
        addItemDecoration(VerticalSpaceItemDecorator(4.dpToPx()))
    }

    private fun init(attrs: AttributeSet?) {
        style = FileAttachmentViewStyle(context, attrs)
        fileAttachmentsAdapter = FileAttachmentsAdapter(
            attachmentClickListener = { attachmentClickListener?.onAttachmentClick(it) },
            attachmentLongClickListener = { attachmentLongClickListener?.onAttachmentLongClick() },
            attachmentDownloadClickListener = { attachmentDownloadClickListener?.onAttachmentDownloadClick(it) },
            style,
        )
        adapter = fileAttachmentsAdapter
    }

    fun setAttachments(attachments: List<Attachment>) {
        fileAttachmentsAdapter.setItems(attachments)
    }

    override fun onDetachedFromWindow() {
        adapter?.onDetachedFromRecyclerView(this)
        super.onDetachedFromWindow()
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
    private val attachmentDownloadClickListener: AttachmentDownloadClickListener,
    private val style: FileAttachmentViewStyle,
) : SimpleListAdapter<Attachment, FileAttachmentViewHolder>() {

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

private class FileAttachmentViewHolder(
    private val binding: StreamUiItemFileAttachmentBinding,
    private val attachmentClickListener: AttachmentClickListener,
    private val attachmentLongClickListener: AttachmentLongClickListener,
    private val attachmentDownloadClickListener: AttachmentDownloadClickListener,
    private val style: FileAttachmentViewStyle,
) : SimpleListAdapter.ViewHolder<Attachment>(binding.root) {
    private var attachment: Attachment? = null

    private var scope: CoroutineScope? = null

    fun clearScope() {
        scope?.cancel()
        scope = null
    }

    init {
        binding.root.setOnClickListener {
            attachment?.let(attachmentClickListener::onAttachmentClick)
        }
        binding.root.setOnLongClickListener {
            attachmentLongClickListener.onAttachmentLongClick()
            true
        }
        binding.actionButton.setOnClickListener {
            attachment?.let(attachmentDownloadClickListener::onAttachmentDownloadClick)
        }
    }

    private fun setupBackground() {
        val shapeAppearanceModel = ShapeAppearanceModel.Builder()
            .setAllCorners(CornerFamily.ROUNDED, style.cornerRadius.toFloat())
            .build()
        val bgShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
        bgShapeDrawable.apply {
            fillColor = ColorStateList.valueOf(style.backgroundColor)
            strokeColor = ColorStateList.valueOf(style.strokeColor)
            strokeWidth = style.strokeWidth.toFloat()
        }

        binding.root.background = bgShapeDrawable
    }

    init {
        binding.root.background = ShapeAppearanceModel.builder()
            .setAllCornerSizes(CORNER_SIZE_PX)
            .build()
            .let(::MaterialShapeDrawable)
            .apply {
                setStroke(
                    STROKE_WIDTH_PX,
                    ContextCompat.getColor(itemView.context, R.color.stream_ui_grey_whisper)
                )
                setTint(ContextCompat.getColor(itemView.context, R.color.stream_ui_white))
            }
    }

    fun restartJob() {
        attachment?.let(::subscribeForProgressIfNeeded)
    }

    private fun subscribeForProgressIfNeeded(attachment: Attachment) {
        val uploadState = attachment.uploadState
        if (uploadState is Attachment.UploadState.Idle) {
            handleInProgressAttachment(binding.fileSize, 0L, attachment.upload?.length() ?: 0)
        } else if (uploadState is Attachment.UploadState.InProgress) {
            handleInProgressAttachment(binding.fileSize, uploadState.bytesRead, uploadState.totalBytes)
        }
    }

    override fun bind(item: Attachment) {
        this.attachment = item

        binding.apply {
            fileTitle.setTextStyle(style.titleTextStyle)
            fileSize.setTextStyle(style.fileSizeTextStyle)

            fileTypeIcon.loadAttachmentThumb(item)
            fileTitle.text = item.getDisplayableName()

            if (item.uploadState is Attachment.UploadState.Idle
                || item.uploadState is Attachment.UploadState.InProgress
                || (item.uploadState is Attachment.UploadState.Success && item.fileSize == 0)
            ) {
                actionButton.setImageDrawable(null)
                fileSize.text = MediaStringUtil.convertFileSizeByteCount(item.upload?.length() ?: 0L)
            } else if (item.uploadState is Attachment.UploadState.Failed || item.fileSize == 0) {
                actionButton.setImageDrawable(style.failedAttachmentIcon)
                fileSize.text = MediaStringUtil.convertFileSizeByteCount(item.upload?.length() ?: 0L)
            } else {
                actionButton.setImageDrawable(style.actionButtonIcon)
                fileSize.text = MediaStringUtil.convertFileSizeByteCount(item.fileSize.toLong())
            }

            binding.progressBar.indeterminateDrawable = style.progressBarDrawable
            binding.progressBar.isVisible = item.uploadState is Attachment.UploadState.InProgress

            subscribeForProgressIfNeeded(item)
            setupBackground()
        }
    }

    private fun handleInProgressAttachment(fileSizeView: TextView, bytesRead: Long, totalBytes: Long) {
        val totalValue = MediaStringUtil.convertFileSizeByteCount(totalBytes)

        fileSizeView.text =
            context.getString(
                R.string.stream_ui_message_list_attachment_upload_progress,
                MediaStringUtil.convertFileSizeByteCount(bytesRead),
                totalValue
            )
    }

    override fun unbind() {
        clearScope()
        super.unbind()
    }

    private companion object {
        private val CORNER_SIZE_PX = 12.dpToPxPrecise()
        private val STROKE_WIDTH_PX = 1.dpToPxPrecise()
    }
}
