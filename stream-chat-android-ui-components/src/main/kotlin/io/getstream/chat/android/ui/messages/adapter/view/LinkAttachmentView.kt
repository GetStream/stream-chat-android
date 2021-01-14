package io.getstream.chat.android.ui.messages.adapter.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.getstream.sdk.chat.images.StreamImageLoader.ImageTransformation.RoundedCorners
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiLinkAttachmentsViewBinding
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise

internal class LinkAttachmentView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val binding: StreamUiLinkAttachmentsViewBinding =
        StreamUiLinkAttachmentsViewBinding.inflate(LayoutInflater.from(context), this, true)
    private var previewUrl: String? = null

    fun showLinkAttachment(attachment: Attachment) {
        previewUrl = attachment.ogUrl
        attachment.title?.let { title ->
            binding.titleTextView.apply {
                isVisible = true
                text = title
            }
        }

        attachment.text?.let { description ->
            binding.descriptionTextView.apply {
                isVisible = true
                text = description
            }
        }
        attachment.authorName?.let { label ->
            binding.labelContainer.isVisible = true
            binding.labelTextView.text = label.capitalize()
        }

        binding.linkPreviewImageView.load(
            data = attachment.thumbUrl ?: attachment.imageUrl,
            placeholderResId = R.drawable.stream_ui_picture_placeholder,
            onStart = { binding.progressBar.isVisible = true },
            onComplete = { binding.progressBar.isVisible = false },
            transformation = RoundedCorners(LINK_PREVIEW_CORNER_RADIUS),
        )
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
