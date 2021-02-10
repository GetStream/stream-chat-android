package io.getstream.chat.android.ui.message.list.adapter.view.internal

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.getstream.sdk.chat.images.StreamImageLoader.ImageTransformation.RoundedCorners
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.dpToPxPrecise
import io.getstream.chat.android.ui.databinding.StreamUiLinkAttachmentsViewBinding

internal class LinkAttachmentView : FrameLayout {
    internal val binding: StreamUiLinkAttachmentsViewBinding = StreamUiLinkAttachmentsViewBinding
        .inflate(context.inflater, this, true)
    private var previewUrl: String? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun showLinkAttachment(attachment: Attachment) {
        previewUrl = attachment.ogUrl

        val title = attachment.title
        if (title != null) {
            binding.titleTextView.isVisible = true
            binding.titleTextView.text = title
        } else {
            binding.titleTextView.isVisible = false
        }

        val description = attachment.text
        if (description != null) {
            binding.descriptionTextView.isVisible = true
            binding.descriptionTextView.text = description
        } else {
            binding.descriptionTextView.isVisible = false
        }

        val label = attachment.authorName
        if (label != null) {
            binding.labelContainer.isVisible = true
            binding.labelTextView.text = label.capitalize()
        } else {
            binding.labelContainer.isVisible = false
        }

        val previewUrl = attachment.thumbUrl ?: attachment.imageUrl
        if (previewUrl != null) {
            binding.linkPreviewImageView.load(
                data = previewUrl,
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
