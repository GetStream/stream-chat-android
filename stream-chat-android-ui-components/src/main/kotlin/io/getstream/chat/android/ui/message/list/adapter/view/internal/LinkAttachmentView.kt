/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.message.list.adapter.view.internal

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.getstream.sdk.chat.images.StreamImageLoader.ImageTransformation.RoundedCorners
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.dpToPxPrecise
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.android.ui.common.style.setTextStyle
import io.getstream.chat.android.ui.databinding.StreamUiLinkAttachmentsViewBinding
import io.getstream.chat.android.ui.message.list.MessageListItemStyle

internal class LinkAttachmentView : FrameLayout {
    private val binding = StreamUiLinkAttachmentsViewBinding.inflate(streamThemeInflater, this, true)
    private var previewUrl: String? = null

    constructor(context: Context) : super(context.createStreamThemeWrapper())
    constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    )

    fun showLinkAttachment(attachment: Attachment, style: MessageListItemStyle) {
        previewUrl = attachment.titleLink ?: attachment.ogUrl

        val title = attachment.title
        if (title != null) {
            binding.titleTextView.isVisible = true
            binding.titleTextView.text = title
        } else {
            binding.titleTextView.isVisible = false
        }
        style.textStyleLinkTitle.apply(binding.titleTextView)

        val description = attachment.text
        if (description != null) {
            binding.descriptionTextView.isVisible = true
            binding.descriptionTextView.text = description
        } else {
            binding.descriptionTextView.isVisible = false
        }
        style.textStyleLinkDescription.apply(binding.descriptionTextView)

        val label = attachment.authorName
        if (label != null) {
            binding.labelContainer.isVisible = true
            binding.labelTextView.text = label.replaceFirstChar(Char::uppercase)
        } else {
            binding.labelContainer.isVisible = false
        }

        if (attachment.imagePreviewUrl != null) {
            binding.linkPreviewImageView.load(
                data = attachment.imagePreviewUrl,
                placeholderResId = R.drawable.stream_ui_picture_placeholder,
                onStart = { binding.progressBar.isVisible = true },
                onComplete = { binding.progressBar.isVisible = false },
                transformation = RoundedCorners(LINK_PREVIEW_CORNER_RADIUS),
            )
        } else {
            binding.linkPreviewImageView.isVisible = false
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
