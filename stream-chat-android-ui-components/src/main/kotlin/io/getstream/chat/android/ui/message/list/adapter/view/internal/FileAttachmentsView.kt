package io.getstream.chat.android.ui.message.list.adapter.view.internal

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.utils.MediaStringUtil
import com.getstream.sdk.chat.utils.extensions.getDisplayableName
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.loadAttachmentThumb
import io.getstream.chat.android.ui.common.style.setTextStyle
import io.getstream.chat.android.ui.databinding.StreamUiItemFileAttachmentBinding
import io.getstream.chat.android.ui.message.list.FileAttachmentViewStyle
import io.getstream.chat.android.ui.message.list.adapter.AttachmentItemPayloadDiff
import io.getstream.chat.android.ui.message.list.adapter.EMPTY_ATTACHMENT_ITEM_PAYLOAD_DIFF
import io.getstream.chat.android.ui.message.list.adapter.FULL_ATTACHMENT_ITEM_PAYLOAD_DIFF
import io.getstream.chat.android.ui.message.list.adapter.internal.AttachmentItemDiffCallback

private const val SPACE_HEIGHT_DP = 4

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
        addItemDecoration(VerticalSpaceItemDecorator(SPACE_HEIGHT_DP.dpToPx()))
    }

    private fun init(attrs: AttributeSet?) {
        style = FileAttachmentViewStyle(context, attrs)
    }

    fun setAttachments(attachments: List<Attachment>) {
        if (!::fileAttachmentsAdapter.isInitialized) {
            fileAttachmentsAdapter = FileAttachmentsAdapter(
                attachmentClickListener = attachmentClickListener?.let { listener ->
                    AttachmentClickListener(listener::onAttachmentClick)
                },
                attachmentLongClickListener = attachmentLongClickListener?.let { listener ->
                    AttachmentLongClickListener(listener::onAttachmentLongClick)
                },
                attachmentDownloadClickListener = attachmentDownloadClickListener?.let { listener ->
                    AttachmentDownloadClickListener(listener::onAttachmentDownloadClick)
                },
                style,
            )

            adapter = fileAttachmentsAdapter
        }

        fileAttachmentsAdapter.submitList(attachments)
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

/*
 * We can't use SimpleListAdapter adapter here. Other wise we are always going to redraw the hold attachment item instead
 * of updating only the necessary data. This would cause all the animations to start over when any update is made in the list.
 */
private class FileAttachmentsAdapter(
    private val attachmentClickListener: AttachmentClickListener?,
    private val attachmentLongClickListener: AttachmentLongClickListener?,
    private val attachmentDownloadClickListener: AttachmentDownloadClickListener?,
    private val style: FileAttachmentViewStyle,
) : ListAdapter<Attachment, FileAttachmentViewHolder>(AttachmentItemDiffCallback) {

    override fun onBindViewHolder(holder: FileAttachmentViewHolder, position: Int) {
        holder.bindData(getItem(position), FULL_ATTACHMENT_ITEM_PAYLOAD_DIFF)
    }

    override fun onBindViewHolder(holder: FileAttachmentViewHolder, position: Int, payloads: MutableList<Any>) {
        val diff = payloads.filterIsInstance<AttachmentItemPayloadDiff>()
            .takeIf { it.isNotEmpty() }
            .let { it ?: listOf(FULL_ATTACHMENT_ITEM_PAYLOAD_DIFF) }
            .fold(EMPTY_ATTACHMENT_ITEM_PAYLOAD_DIFF, AttachmentItemPayloadDiff::plus)

        holder.bindData(getItem(position), diff)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileAttachmentViewHolder {
        return StreamUiItemFileAttachmentBinding
            .inflate(parent.streamThemeInflater, parent, false)
            .let {
                FileAttachmentViewHolder(
                    it,
                    attachmentClickListener,
                    attachmentLongClickListener,
                    attachmentDownloadClickListener,
                    style
                )
            }
    }

    override fun onViewAttachedToWindow(holder: FileAttachmentViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.restartJob()
    }
}

private class FileAttachmentViewHolder(
    private val binding: StreamUiItemFileAttachmentBinding,
    attachmentClickListener: AttachmentClickListener?,
    attachmentLongClickListener: AttachmentLongClickListener?,
    attachmentDownloadClickListener: AttachmentDownloadClickListener?,
    private val style: FileAttachmentViewStyle,
) : RecyclerView.ViewHolder(binding.root) {
    private var attachment: Attachment? = null

    init {
        attachmentClickListener?.let { listener ->
            binding.root.setOnClickListener {
                attachment?.let(listener::onAttachmentClick)
            }
        }

        attachmentLongClickListener?.let { listener ->
            binding.root.setOnLongClickListener {
                listener.onAttachmentLongClick()
                true
            }
        }

        attachmentDownloadClickListener?.let { listener ->
            binding.actionButton.setOnClickListener {
                attachment?.let(listener::onAttachmentDownloadClick)
            }
        }

        setupBackground()
    }

    private fun setupBackground() {
        val bgShapeDrawable = ShapeAppearanceModel.Builder()
            .setAllCorners(CornerFamily.ROUNDED, style.cornerRadius.toFloat())
            .build()
            .let(::MaterialShapeDrawable)
            .apply {
                fillColor = ColorStateList.valueOf(style.backgroundColor)
                strokeColor = ColorStateList.valueOf(style.strokeColor)
                strokeWidth = style.strokeWidth.toFloat()
            }

        binding.root.background = bgShapeDrawable
    }

    private fun subscribeForProgressIfNeeded(attachment: Attachment) {
        val uploadState = attachment.uploadState
        if (uploadState is Attachment.UploadState.Idle) {
            handleInProgressAttachment(binding.fileSize, 0L, attachment.upload?.length() ?: 0)
        } else if (uploadState is Attachment.UploadState.InProgress) {
            handleInProgressAttachment(binding.fileSize, uploadState.bytesUploaded, uploadState.totalBytes)
        }
    }

    private fun handleInProgressAttachment(fileSizeView: TextView, bytesRead: Long, totalBytes: Long) {
        val totalValue = MediaStringUtil.convertFileSizeByteCount(totalBytes)

        fileSizeView.text =
            itemView.context.getString(
                R.string.stream_ui_message_list_attachment_upload_progress,
                MediaStringUtil.convertFileSizeByteCount(bytesRead),
                totalValue
            )
    }

    fun restartJob() {
        attachment?.let(::subscribeForProgressIfNeeded)
    }

    fun bindData(data: Attachment, diff: AttachmentItemPayloadDiff) {
        this.attachment = data

        binding.apply {
            fileSize.setTextStyle(style.fileSizeTextStyle)

            if (shouldChangeThumb(diff)) fileTypeIcon.loadAttachmentThumb(data)
            if (shouldChangeTitle(diff)) {
                fileTitle.setTextStyle(style.titleTextStyle)
                fileTitle.text = data.getDisplayableName()
            }

            if (diff.uploadState) {
                renderUploadState(data)
            }

            subscribeForProgressIfNeeded(data)
        }
    }

    private fun renderUploadState(data: Attachment) {
        binding.run {
            if (data.uploadState is Attachment.UploadState.Idle
                || data.uploadState is Attachment.UploadState.InProgress
                || (data.uploadState is Attachment.UploadState.Success && data.fileSize == 0)
            ) {
                actionButton.setImageDrawable(null)
                fileSize.text = MediaStringUtil.convertFileSizeByteCount(data.upload?.length() ?: 0L)
            } else if (data.uploadState is Attachment.UploadState.Failed || data.fileSize == 0) {
                actionButton.setImageDrawable(style.failedAttachmentIcon)
                fileSize.text = MediaStringUtil.convertFileSizeByteCount(data.upload?.length() ?: 0L)
            } else {
                actionButton.setImageDrawable(style.actionButtonIcon)
                fileSize.text = MediaStringUtil.convertFileSizeByteCount(data.fileSize.toLong())
            }
        }

        binding.progressBar.indeterminateDrawable = style.progressBarDrawable
        binding.progressBar.isVisible = data.uploadState is Attachment.UploadState.InProgress
    }

    private fun shouldChangeTitle(diff: AttachmentItemPayloadDiff): Boolean {
        return diff.run {
            title || name || upload
        }
    }

    private fun shouldChangeThumb(diff: AttachmentItemPayloadDiff): Boolean {
        return diff.run {
            type || thumbUrl || imageUrl || mimeType || title || name || upload
        }
    }
}
