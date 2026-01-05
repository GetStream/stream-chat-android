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
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.images.resizing.applyStreamCdnImageResizingIfEnabled
import io.getstream.chat.android.ui.common.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.ui.databinding.StreamUiMediaAttachmentViewBinding
import io.getstream.chat.android.ui.feature.messages.list.adapter.view.MediaAttachmentViewStyle
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.constrainViewToParentBySide
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.utils.extensions.updateConstraints
import io.getstream.chat.android.ui.utils.load

/**
 * View used to display image and video attachments.
 *
 * Giphy images are handled by a separate View.
 * @see GiphyMediaAttachmentView
 */
internal class MediaAttachmentView : ConstraintLayout {
    /**
     * Handles clicks on media attachment previews.
     */
    var attachmentClickListener: AttachmentClickListener? = null

    /**
     * Handles media attachment long clicks.
     */
    var attachmentLongClickListener: AttachmentLongClickListener? = null

    /**
     * Binding for [R.layout.stream_ui_media_attachment_view].
     */
    internal val binding: StreamUiMediaAttachmentViewBinding =
        StreamUiMediaAttachmentViewBinding.inflate(streamThemeInflater).also {
            it.root.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            val padding = 1.dpToPx()
            it.root.setPadding(padding, padding, padding, padding)
            addView(it.root)
            updateConstraints {
                constrainViewToParentBySide(it.root, ConstraintSet.LEFT)
                constrainViewToParentBySide(it.root, ConstraintSet.TOP)
            }
        }

    /**
     * Style applied to [MediaAttachmentView].
     */
    private lateinit var style: MediaAttachmentViewStyle

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        style = MediaAttachmentViewStyle(context, attrs)
        binding.loadingProgressBar.indeterminateDrawable = style.progressIcon
        binding.moreCountLabel.setTextStyle(style.moreCountTextStyle)
        setupPlayIcon()
    }

    /**
     * Sets up the play icon overlaid above video attachment previews
     * by pulling relevant values from [MediaAttachmentViewStyle].
     **/
    private fun setupPlayIcon() {
        with(binding.playIconCardView) {
            elevation = style.playVideoIconElevation
            setCardBackgroundColor(style.playVideoIconBackgroundColor)
            radius = style.playVideoIconCornerRadius
        }

        with(binding.playIconImageView) {
            val playVideoDrawable = style.playVideoIcon?.mutate()?.apply {
                val tintColor = style.playVideoIconTint

                if (tintColor != null) {
                    this.setTint(tintColor)
                }
            }

            setImageDrawable(playVideoDrawable)
            setPaddingRelative(
                style.playVideoIconPaddingStart,
                style.playVideoIconPaddingTop,
                style.playVideoIconPaddingEnd,
                style.playVideoIconPaddingBottom,
            )
        }
    }

    /**
     * Displays the media previews in a message. Displays a count saying how many more
     * media attachments the message contains in case they don't all fit in the preview.
     */
    fun showAttachment(attachment: Attachment, andMoreCount: Int = NO_MORE_COUNT) {
        val url =
            if (attachment.isImage() ||
                (attachment.isVideo() && ChatUI.videoThumbnailsEnabled && attachment.thumbUrl != null)
            ) {
                attachment.imagePreviewUrl?.applyStreamCdnImageResizingIfEnabled(ChatUI.streamCdnImageResizing)
                    ?: attachment.titleLink ?: attachment.ogUrl ?: attachment.upload ?: return
            } else {
                null
            }

        val showMore = {
            if (andMoreCount > NO_MORE_COUNT) {
                showMoreCount(andMoreCount)
            }
        }

        showMediaPreview(
            mediaUrl = url,
            showImagePlaceholder = attachment.isImage(),
        ) {
            showMore()
            binding.playIconImageView.isVisible = attachment.isVideo()
        }

        setOnClickListener { attachmentClickListener?.onAttachmentClick(attachment) }
        setOnLongClickListener {
            attachmentLongClickListener?.onAttachmentLongClick()
            true
        }
    }

    /**
     * Sets the visibility for the progress bar.
     */
    private fun showLoading(isLoading: Boolean) {
        binding.loadImage.isVisible = isLoading
    }

    /**
     * Loads the media preview.
     */
    private fun showMediaPreview(
        mediaUrl: Any?,
        showImagePlaceholder: Boolean,
        onCompleteCallback: () -> Unit,
    ) {
        val placeholder = if (showImagePlaceholder) {
            style.placeholderIcon.mutate().apply {
                val tint = style.placeholderIconTint

                if (tint != null) {
                    this.setTint(tint)
                }
            }
        } else {
            null
        }

        binding.imageView.load(
            data = mediaUrl,
            placeholderDrawable = placeholder,
            onStart = { showLoading(true) },
            onComplete = {
                showLoading(false)
                onCompleteCallback()
            },
        )
    }

    /**
     * Displays how many more media attachments the message contains that are not
     * able to fit inside the preview.
     */
    private fun showMoreCount(andMoreCount: Int) {
        binding.moreCount.isVisible = true
        binding.moreCountLabel.text =
            context.getString(R.string.stream_ui_message_list_attachment_more_count, andMoreCount)
    }

    /**
     * Creates and sets the shape of the media preview containers.
     */
    fun setMediaPreviewShapeByCorners(
        topLeft: Float,
        topRight: Float,
        bottomRight: Float,
        bottomLeft: Float,
    ) {
        ShapeAppearanceModel.Builder()
            .setTopLeftCornerSize(topLeft)
            .setTopRightCornerSize(topRight)
            .setBottomRightCornerSize(bottomRight)
            .setBottomLeftCornerSize(bottomLeft)
            .build()
            .let(this::setMediaPreviewShape)
    }

    /**
     * Applies the shape to the media preview containers. Also sets the container
     * background color and the overlay color for the label that displays
     * how many more media attachments there are in a message.
     */
    private fun setMediaPreviewShape(shapeAppearanceModel: ShapeAppearanceModel) {
        binding.imageView.shapeAppearanceModel = shapeAppearanceModel
        binding.imageView.background = MaterialShapeDrawable(shapeAppearanceModel).apply {
            setTint(style.mediaPreviewBackgroundColor)
        }
        binding.loadImage.background = MaterialShapeDrawable(shapeAppearanceModel).apply {
            setTint(style.mediaPreviewBackgroundColor)
        }
        binding.moreCount.background = MaterialShapeDrawable(shapeAppearanceModel).apply {
            setTint(style.moreCountOverlayColor)
        }
    }

    companion object {
        /**
         * When all media attachments in a message are able to fit in the preview.
         */
        private const val NO_MORE_COUNT = 0
    }
}
