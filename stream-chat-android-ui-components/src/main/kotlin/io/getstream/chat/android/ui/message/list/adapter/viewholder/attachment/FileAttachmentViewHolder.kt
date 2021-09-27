package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import android.content.Context
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.getstream.sdk.chat.utils.MediaStringUtil
import com.getstream.sdk.chat.utils.extensions.getDisplayableName
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.uploader.ProgressTrackerFactory
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.dpToPxPrecise
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.common.internal.loadAttachmentThumb
import io.getstream.chat.android.ui.databinding.StreamUiItemFileAttachmentBinding
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentDownloadClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentLongClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

internal class FileAttachmentViewHolder(
    private val binding: StreamUiItemFileAttachmentBinding,
    private val attachmentClickListener: AttachmentClickListener,
    private val attachmentLongClickListener: AttachmentLongClickListener,
    private val attachmentDownloadClickListener: AttachmentDownloadClickListener,
    // private val style: FileAttachmentViewStyle,
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
            .setAllCorners(CornerFamily.ROUNDED, 5.toFloat())
            .build()
        val bgShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
        bgShapeDrawable.apply {
            // fillColor = ColorStateList.valueOf(style.backgroundColor)
            // strokeColor = ColorStateList.valueOf(style.strokeColor)
            // strokeWidth = style.strokeWidth.toFloat()
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
        if (attachment.uploadState is Attachment.UploadState.InProgress) {
            handleInProgressAttachment(binding.fileSize)
        }
    }

    override fun bind(item: Attachment) {
        this.attachment = item

        binding.apply {
            // fileTitle.setTextStyle(style.titleTextStyle)
            // fileSize.setTextStyle(style.fileSizeTextStyle)

            fileTypeIcon.loadAttachmentThumb(item)
            fileTitle.text = item.getDisplayableName()

            if (item.uploadState == Attachment.UploadState.InProgress) {
                actionButton.setImageDrawable(null)
                fileSize.text = MediaStringUtil.convertFileSizeByteCount(item.upload?.length() ?: 0L)
            } else if (item.uploadState is Attachment.UploadState.Failed || item.fileSize == 0) {
                // actionButton.setImageDrawable(style.failedAttachmentIcon)
                fileSize.text = MediaStringUtil.convertFileSizeByteCount(item.upload?.length() ?: 0L)
            } else {
                // actionButton.setImageDrawable(style.actionButtonIcon)
                fileSize.text = MediaStringUtil.convertFileSizeByteCount(item.fileSize.toLong())
            }

            // binding.progressBar.indeterminateDrawable = style.progressBarDrawable
            binding.progressBar.isVisible = item.uploadState is Attachment.UploadState.InProgress

            subscribeForProgressIfNeeded(item)
            setupBackground()
        }
    }

    private fun handleInProgressAttachment(fileSizeView: TextView) {
        attachment?.let { attachment ->
            attachment.uploadId?.let(ProgressTrackerFactory::getOrCreate)?.let { tracker ->
                val progress = tracker.currentProgress()
                val completion = tracker.isComplete()
                val totalValue = MediaStringUtil.convertFileSizeByteCount(attachment.upload?.length() ?: 0)

                val fileProgress = progress.combine(completion, ::Pair)

                clearScope()
                scope = CoroutineScope(DispatcherProvider.Main)

                scope?.launch {
                    fileProgress.collect { (progress, isComplete) ->
                        val uploadedBytes = (progress / 100F * tracker.maxValue).toLong()
                        updateProgress(
                            context,
                            fileSizeView,
                            binding.progressBar,
                            attachment,
                            uploadedBytes,
                            isComplete,
                            totalValue
                        )
                    }
                }
            }
        }
    }

    override fun unbind() {
        clearScope()
        super.unbind()
    }

    private companion object {
        private val CORNER_SIZE_PX = 12.dpToPxPrecise()
        private val STROKE_WIDTH_PX = 1.dpToPxPrecise()

        private fun updateProgress(
            context: Context,
            fileSizeView: TextView,
            progressBar: ProgressBar,
            attachment: Attachment,
            uploadedBytes: Long,
            isComplete: Boolean,
            targetValue: String,
        ) {
            if (!isComplete) {
                val nominalProgress = MediaStringUtil.convertFileSizeByteCount(uploadedBytes)

                fileSizeView.text =
                    context.getString(
                        R.string.stream_ui_message_list_attachment_upload_progress,
                        nominalProgress,
                        targetValue
                    )
            } else {
                progressBar.isVisible = false
                fileSizeView.text =
                    attachment.upload?.length()?.let(MediaStringUtil::convertFileSizeByteCount)
            }
        }
    }
}
