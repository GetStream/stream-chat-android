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
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.utils.MediaStringUtil
import com.getstream.sdk.chat.utils.extensions.getDisplayableName
import com.getstream.sdk.chat.utils.extensions.inflater
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.uploader.ProgressTrackerFactory
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.common.extensions.internal.dpToPxPrecise
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.common.internal.loadAttachmentThumb
import io.getstream.chat.android.ui.databinding.StreamUiItemFileAttachmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

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
    private val attachmentDownloadClickListener: AttachmentDownloadClickListener,
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
    private val attachmentDownloadClickListener: AttachmentDownloadClickListener,
) : SimpleListAdapter.ViewHolder<Attachment>(binding.root) {
    private lateinit var attachment: Attachment

    private var scope: CoroutineScope? = null

    private fun clearScope() {
        scope?.cancel()
        scope = null
    }

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
                    ContextCompat.getColor(itemView.context, R.color.stream_ui_grey_whisper)
                )
                setTint(ContextCompat.getColor(itemView.context, R.color.stream_ui_white))
            }
    }

    override fun bind(item: Attachment) {
        this.attachment = item

        binding.apply {
            fileTypeIcon.loadAttachmentThumb(attachment)
            fileTitle.text = attachment.getDisplayableName()

            if (attachment.uploadState is Attachment.UploadState.Failed) {
                actionButton.setImageResource(R.drawable.stream_ui_ic_warning)
                val tintColor = ContextCompat.getColor(context, R.color.stream_ui_accent_red)
                ImageViewCompat.setImageTintList(actionButton, ColorStateList.valueOf(tintColor))
                fileSize.text = MediaStringUtil.convertFileSizeByteCount(attachment.upload?.length() ?: 0L)
            } else {
                actionButton.setImageResource(R.drawable.stream_ui_ic_icon_download)
                val tintColor = ContextCompat.getColor(context, R.color.stream_ui_black)
                ImageViewCompat.setImageTintList(actionButton, ColorStateList.valueOf(tintColor))
                fileSize.text = MediaStringUtil.convertFileSizeByteCount(attachment.fileSize.toLong())
            }

            binding.progressBar.isVisible = attachment.uploadState is Attachment.UploadState.InProgress

            if (attachment.uploadState is Attachment.UploadState.InProgress) {
                handleInProgressAttachment(fileSize)
            }
        }
    }

    private fun handleInProgressAttachment(fileSizeView: TextView) {
        attachment.uploadId?.let(ProgressTrackerFactory::getOrCreate)?.let { tracker ->
            val progress = tracker.currentProgress()
            val completion = tracker.isComplete()
            val totalValue = MediaStringUtil.convertFileSizeByteCount(attachment.upload?.length() ?: 0)
            val progressCorrection = tracker.maxValue / 100F

            val fileProgress = progress.combine(completion, ::Pair)

            clearScope()
            scope = CoroutineScope(DispatcherProvider.Main)

            scope!!.launch {
                fileProgress.collect { (progress, isComplete) ->
                    updateProgress(context, fileSizeView, progress, isComplete, progressCorrection, totalValue)
                }
            }
        }
    }

    private fun updateProgress(
        context: Context,
        fileSizeView: TextView,
        progress: Int,
        isComplete: Boolean,
        progressCorrection: Float,
        targetValue: String,
    ) {
        if (!isComplete) {
            val nominalProgress = MediaStringUtil.convertFileSizeByteCount((progress * progressCorrection).toLong())

            fileSizeView.text =
                context.getString(R.string.stream_ui_upload_sending_percentage, nominalProgress, targetValue)
        } else {
            binding.progressBar.isVisible = false
            fileSizeView.text = attachment.upload?.length()?.let(MediaStringUtil::convertFileSizeByteCount)
        }
    }

    override fun unbind() {
        super.unbind()
        clearScope()
    }

    companion object {
        private val CORNER_SIZE_PX = 12.dpToPxPrecise()
        private val STROKE_WIDTH_PX = 1.dpToPxPrecise()
    }
}
