/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.audio.AudioPlayer
import io.getstream.chat.android.client.audio.AudioState
import io.getstream.chat.android.client.audio.audioHash
import io.getstream.chat.android.client.extensions.duration
import io.getstream.chat.android.client.extensions.durationInMs
import io.getstream.chat.android.client.extensions.waveformData
import io.getstream.chat.android.client.utils.attachment.isAudioRecording
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.doForAllViewHolders
import io.getstream.chat.android.ui.common.utils.MediaStringUtil
import io.getstream.chat.android.ui.common.utils.extensions.getDisplayableName
import io.getstream.chat.android.ui.databinding.StreamUiItemFileAttachmentBinding
import io.getstream.chat.android.ui.databinding.StreamUiItemRecordingAttachmentBinding
import io.getstream.chat.android.ui.feature.messages.list.FileAttachmentViewStyle
import io.getstream.chat.android.ui.feature.messages.list.background.ShapeAppearanceModelFactory
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.utils.loadAttachmentThumb
import io.getstream.chat.android.ui.widgets.internal.SimpleListAdapter
import io.getstream.chat.android.uiutils.extension.hasLink
import io.getstream.chat.android.uiutils.extension.isFailed
import io.getstream.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

internal class FileAttachmentsView : RecyclerView {

    private val logger by taggedLogger("FileAttachmentListView")

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
        defStyleAttr,
    ) {
        init(attrs)
    }

    init {
        layoutManager = LinearLayoutManager(context)
        addItemDecoration(VerticalSpaceItemDecorator(4.dpToPx()))
    }

    private fun init(attrs: AttributeSet?) {
        style = FileAttachmentViewStyle(context, attrs)
    }

    /**
     * Sets click listeners on individual items and filters out attachments containing links
     * before setting the data on the RecyclerView adapter.
     */
    fun setAttachments(attachments: List<Attachment>) {
        logger.d { "[setAttachments] attachments: $attachments" }
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

        val filteredAttachments = attachments.filter { attachment ->
            !attachment.hasLink() /*&& !attachment.isAudioRecording()*/
        }
        logger.v { "[setAttachments] filteredAttachments.size: ${filteredAttachments.size}" }
        fileAttachmentsAdapter.setItems(filteredAttachments)
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
    private val attachmentClickListener: AttachmentClickListener?,
    private val attachmentLongClickListener: AttachmentLongClickListener?,
    private val attachmentDownloadClickListener: AttachmentDownloadClickListener?,
    private val style: FileAttachmentViewStyle,
) : SimpleListAdapter<Attachment, FileAttachmentViewHolder>() {

    companion object {
        private const val VIEW_TYPE_RECORDING = 1
        private const val VIEW_TYPE_GENERAL = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (itemList.getOrNull(position)?.isAudioRecording()) {
            true -> VIEW_TYPE_RECORDING
            else -> VIEW_TYPE_GENERAL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileAttachmentViewHolder {
        return when (viewType) {
            VIEW_TYPE_RECORDING ->
                StreamUiItemRecordingAttachmentBinding
                    .inflate(parent.streamThemeInflater, parent, false)
                    .let {
                        RecordingFileAttachmentViewHolder(
                            it,
                            attachmentClickListener,
                            attachmentLongClickListener,
                            attachmentDownloadClickListener,
                            style,
                        )
                    }
            else ->
                StreamUiItemFileAttachmentBinding
                    .inflate(parent.streamThemeInflater, parent, false)
                    .let {
                        GeneralFileAttachmentViewHolder(
                            it,
                            attachmentClickListener,
                            attachmentLongClickListener,
                            attachmentDownloadClickListener,
                            style,
                        )
                    }
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

private sealed class FileAttachmentViewHolder(itemView: View) : SimpleListAdapter.ViewHolder<Attachment>(itemView) {

    abstract fun clearScope()

    abstract fun restartJob()
}

private class GeneralFileAttachmentViewHolder(
    private val binding: StreamUiItemFileAttachmentBinding,
    attachmentClickListener: AttachmentClickListener?,
    attachmentLongClickListener: AttachmentLongClickListener?,
    attachmentDownloadClickListener: AttachmentDownloadClickListener?,
    private val style: FileAttachmentViewStyle,
) : FileAttachmentViewHolder(binding.root) {

    private val logger by taggedLogger("FileAttachmentVH")

    private var attachment: Attachment? = null

    private var scope: CoroutineScope? = null

    override fun clearScope() {
        scope?.cancel()
        scope = null
    }

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
                if (attachment?.isFailed() == true) {
                    return@setOnClickListener
                }
                attachment?.let(listener::onAttachmentDownloadClick)
            }
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
        binding.root.background = ShapeAppearanceModelFactory.fileBackground(context)
    }

    override fun restartJob() {
        attachment?.let(::subscribeForProgressIfNeeded)
    }

    private fun subscribeForProgressIfNeeded(attachment: Attachment) {
        val uploadState = attachment.uploadState
        if (uploadState is Attachment.UploadState.Idle) {
            handleInProgressAttachment(binding.fileSize, 0L, attachment.upload?.length() ?: 0)
        } else if (uploadState is Attachment.UploadState.InProgress) {
            handleInProgressAttachment(binding.fileSize, uploadState.bytesUploaded, uploadState.totalBytes)
        }
    }

    override fun bind(item: Attachment) {
        logger.d { "[bind] item: $item" }
        this.attachment = item

        binding.apply {
            fileTitle.setTextStyle(style.titleTextStyle)
            fileSize.setTextStyle(style.fileSizeTextStyle)

            fileTypeIcon.loadAttachmentThumb(item)
            fileTitle.text = item.getDisplayableName()

            if (item.uploadState is Attachment.UploadState.Idle ||
                item.uploadState is Attachment.UploadState.InProgress ||
                (item.uploadState is Attachment.UploadState.Success && item.fileSize == 0)
            ) {
                actionButton.visibility = View.GONE
                fileSize.text = MediaStringUtil.convertFileSizeByteCount(item.upload?.length() ?: 0L)
            } else if (item.uploadState is Attachment.UploadState.Failed || item.fileSize == 0) {
                actionButton.visibility = View.VISIBLE
                actionButton.setImageDrawable(style.failedAttachmentIcon)
                fileSize.text = MediaStringUtil.convertFileSizeByteCount(item.upload?.length() ?: 0L)
            } else {
                actionButton.visibility = View.VISIBLE
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
                totalValue,
            )
    }

    override fun unbind() {
        clearScope()
        super.unbind()
    }
}

private class RecordingFileAttachmentViewHolder(
    private val binding: StreamUiItemRecordingAttachmentBinding,
    attachmentClickListener: AttachmentClickListener?,
    attachmentLongClickListener: AttachmentLongClickListener?,
    attachmentDownloadClickListener: AttachmentDownloadClickListener?,
    private val style: FileAttachmentViewStyle,
) : FileAttachmentViewHolder(binding.root) {

    private val logger by taggedLogger("RecordingAttachmentVH")

    private var attachment: Attachment? = null

    private var scope: CoroutineScope? = null

    override fun clearScope() {
        scope?.cancel()
        scope = null
    }

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
                if (attachment?.isFailed() == true) {
                    return@setOnClickListener
                }
                attachment?.let(listener::onAttachmentDownloadClick)
            }
        }

        val audioPlayer = ChatClient.instance().audioPlayer

        binding.playerView.registerButtonsListeners(audioPlayer)
    }

    private fun AudioPlayer.registerStateChange(playerView: AudioRecordPlayerView, hashCode: Int) {
        registerOnAudioStateChange(hashCode) { audioState ->
            logger.d { "[onAudioStateChange] audioState: $audioState" }
            when (audioState) {
                AudioState.LOADING -> playerView.setLoading()
                AudioState.PAUSE -> playerView.setPaused()
                AudioState.UNSET, AudioState.IDLE -> playerView.setIdle()
                AudioState.PLAYING -> playerView.setPlaying()
            }
        }
        registerOnProgressStateChange(hashCode) { (durationInMs, progress) ->
            playerView.setDuration(ChatUI.durationFormatter.format(durationInMs))
            playerView.setProgress(progress.toDouble())
        }
        registerOnSpeedChange(hashCode, playerView::setSpeedText)
    }

    private fun AudioRecordPlayerView.registerButtonsListeners(
        audioPlayer: AudioPlayer,
    ) {
        setOnPlayButtonClickListener {
            val assetUrl = attachment?.assetUrl
            val audioHash = attachment?.audioHash ?: 0
            logger.d { "[onPlayButtonClick] audioHash: $audioHash, assetUrl: $assetUrl" }
            if (assetUrl != null) {
                audioPlayer.play(assetUrl, audioHash)
            } else {
                setLoading()
            }
        }

        setOnSpeedButtonClickListener {
            audioPlayer.changeSpeed()
        }

        setOnSeekbarMoveListeners({
            val hash = attachment?.audioHash ?: 0
            audioPlayer.startSeek(hash)
        }, { progress ->
            val hash = attachment?.audioHash ?: 0
            audioPlayer.seekTo(
                progressToDecimal(progress, attachment?.duration),
                hash,
            )
        })
    }

    private fun progressToDecimal(progress: Int, totalDuration: Float?): Int =
        progress * (totalDuration ?: NULL_DURATION).toInt() / 100

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
        binding.root.background = ShapeAppearanceModelFactory.fileBackground(context)
    }

    override fun restartJob() {
        attachment?.let(::subscribeForProgressIfNeeded)
    }

    private fun subscribeForProgressIfNeeded(attachment: Attachment) {
        val uploadState = attachment.uploadState
        if (uploadState is Attachment.UploadState.Idle) {
            handleInProgressAttachment(binding.fileSize, 0L, attachment.upload?.length() ?: 0)
        } else if (uploadState is Attachment.UploadState.InProgress) {
            handleInProgressAttachment(binding.fileSize, uploadState.bytesUploaded, uploadState.totalBytes)
        }
    }

    override fun bind(item: Attachment) {
        logger.d { "[bind] item: $item" }
        this.attachment = item

        binding.apply {
            fileTitle.setTextStyle(style.titleTextStyle)
            fileSize.setTextStyle(style.fileSizeTextStyle)

            fileTypeIcon.loadAttachmentThumb(item)
            fileTitle.text = context.getString(R.string.stream_ui_attachment_list_recording)

            val isSuccess = item.uploadState?.let { it is Attachment.UploadState.Success } ?: true
            uploadingContainer.isVisible = isSuccess.not()
            playerView.isVisible = isSuccess

            item.durationInMs
                ?.let(ChatUI.durationFormatter::format)
                ?.let(playerView::setDuration)

            item.waveformData?.also {
                playerView.setWaveBars(it)
            }

            if (item.uploadState is Attachment.UploadState.Idle ||
                item.uploadState is Attachment.UploadState.InProgress ||
                (item.uploadState is Attachment.UploadState.Success && item.fileSize == 0)
            ) {
                actionButton.visibility = View.GONE
                fileSize.text = MediaStringUtil.convertFileSizeByteCount(item.upload?.length() ?: 0L)
            } else if (item.uploadState is Attachment.UploadState.Failed || item.fileSize == 0) {
                actionButton.visibility = View.VISIBLE
                actionButton.setImageDrawable(style.failedAttachmentIcon)
                fileSize.text = ChatUI.durationFormatter.format(item.durationInMs ?: 0)
            } else {
                actionButton.visibility = View.GONE
                actionButton.setImageDrawable(style.actionButtonIcon)
                fileSize.text = ChatUI.durationFormatter.format(item.durationInMs ?: 0)
            }

            binding.progressBar.indeterminateDrawable = style.progressBarDrawable
            binding.progressBar.isVisible = item.uploadState is Attachment.UploadState.InProgress

            subscribeForProgressIfNeeded(item)
            setupBackground()
        }

        val audioPlayer = ChatClient.instance().audioPlayer
        audioPlayer.registerStateChange(binding.playerView, item.audioHash)
    }

    private fun handleInProgressAttachment(fileSizeView: TextView, bytesRead: Long, totalBytes: Long) {
        val totalValue = MediaStringUtil.convertFileSizeByteCount(totalBytes)

        fileSizeView.text =
            context.getString(
                R.string.stream_ui_message_list_attachment_upload_progress,
                MediaStringUtil.convertFileSizeByteCount(bytesRead),
                totalValue,
            )
    }

    override fun unbind() {
        clearScope()
        super.unbind()
    }

    companion object {
        private const val NULL_DURATION = 0.0f
    }
}
