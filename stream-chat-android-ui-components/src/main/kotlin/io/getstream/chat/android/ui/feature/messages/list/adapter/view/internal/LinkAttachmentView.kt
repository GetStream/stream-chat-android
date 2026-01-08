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
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.images.internal.StreamImageLoader.ImageTransformation.RoundedCorners
import io.getstream.chat.android.ui.common.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.ui.databinding.StreamUiLinkAttachmentsViewBinding
import io.getstream.chat.android.ui.feature.messages.list.MessageListItemStyle
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.utils.load

internal class LinkAttachmentView : FrameLayout {
    private val binding = StreamUiLinkAttachmentsViewBinding.inflate(streamThemeInflater, this, true)
    private var previewUrl: String? = null

    constructor(context: Context) : super(context.createStreamThemeWrapper())
    constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
    )

    /**
     * Displays the given attachment.
     *
     * @param attachment The attachment to be displayed.
     * @param style The style used for applying various things such as text styles.
     */
    fun showLinkAttachment(attachment: Attachment, style: MessageListItemStyle) {
        previewUrl = attachment.titleLink ?: attachment.ogUrl
        showTitle(attachment, style)
        showDescription(attachment, style)
        showLabel(attachment, style)
        showAttachmentImage(attachment)
    }

    /**
     * Sets up the style for the link title text and displays it
     * if it exists.
     *
     * @param attachment The attachment used to obtain the title.
     * @param style The style which contains the title text style that
     * will be applied.
     */
    private fun showTitle(attachment: Attachment, style: MessageListItemStyle) {
        val title = attachment.title
        if (title != null) {
            binding.titleTextView.isVisible = true
            binding.titleTextView.text = title
        } else {
            binding.titleTextView.isVisible = false
        }
        binding.titleTextView.setTextStyle(style.textStyleLinkTitle)
    }

    /**
     * Sets up the style for the link description text and displays it
     * if it exists.
     *
     * @param attachment The attachment used to obtain the description.
     * @param style The style which contains the description text style that
     * will be applied.
     */
    private fun showDescription(attachment: Attachment, style: MessageListItemStyle) {
        val description = attachment.text
        if (description != null) {
            binding.descriptionTextView.isVisible = true
            binding.descriptionTextView.text = description
        } else {
            binding.descriptionTextView.isVisible = false
        }
        binding.descriptionTextView.setTextStyle(style.textStyleLinkDescription)
    }

    /**
     * Sets up the style for the link label text and displays it
     * if it exists.
     *
     * @param attachment The attachment used to obtain the label.
     * @param style The style which contains the label text style that
     * will be applied.
     */
    private fun showLabel(attachment: Attachment, style: MessageListItemStyle) {
        val label = attachment.authorName
        if (label != null) {
            binding.labelContainer.isVisible = true
            binding.labelTextView.text = label.replaceFirstChar(Char::uppercase)
        } else {
            binding.labelContainer.isVisible = false
        }
        binding.labelTextView.setTextStyle(style.textStyleLinkLabel)
    }

    /**
     * Shows the attachment preview image if it is not null.
     */
    private fun showAttachmentImage(attachment: Attachment) {
        if (attachment.imagePreviewUrl != null) {
            binding.linkPreviewContainer.isVisible = true

            binding.linkPreviewImageView.load(
                data = attachment.imagePreviewUrl,
                placeholderResId = R.drawable.stream_ui_picture_placeholder,
                onStart = { binding.progressBar.isVisible = true },
                onComplete = { binding.progressBar.isVisible = false },
                transformation = RoundedCorners(LINK_PREVIEW_CORNER_RADIUS),
            )
        } else {
            binding.linkPreviewContainer.isVisible = false
            binding.progressBar.isVisible = false
        }
    }

    internal fun setTitleTextStyle(textStyle: TextStyle) {
        binding.titleTextView.setTextStyle(textStyle)
    }

    internal fun setDescriptionTextStyle(textStyle: TextStyle) {
        binding.descriptionTextView.setTextStyle(textStyle)
    }

    internal fun setLabelTextStyle(textStyle: TextStyle) {
        binding.labelTextView.setTextStyle(textStyle)
    }

    internal fun setLinkDescriptionMaxLines(maxLines: Int) {
        binding.descriptionTextView.maxLines = maxLines
    }

    fun setLinkPreviewClickListener(linkPreviewClickListener: LinkPreviewClickListener) {
        setOnClickListener {
            previewUrl?.let { url ->
                linkPreviewClickListener.onLinkPreviewClick(url)
            }
        }
    }

    fun setLongClickTarget(longClickTarget: View) {
        setOnLongClickListener {
            longClickTarget.performLongClick()
            true
        }
    }

    fun interface LinkPreviewClickListener {
        fun onLinkPreviewClick(url: String)
    }

    companion object {
        private val LINK_PREVIEW_CORNER_RADIUS = 8.dpToPxPrecise()
    }
}
