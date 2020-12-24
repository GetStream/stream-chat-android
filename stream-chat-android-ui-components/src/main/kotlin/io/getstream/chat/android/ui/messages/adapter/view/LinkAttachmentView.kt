package io.getstream.chat.android.ui.messages.adapter.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.databinding.StreamUiLinkAttachmentsViewBinding

internal class LinkAttachmentView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    internal val binding: StreamUiLinkAttachmentsViewBinding =
        StreamUiLinkAttachmentsViewBinding.inflate(LayoutInflater.from(context), this, true)

    fun showLinkAttachment(attachment: Attachment) {
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

        binding.mediaAttachmentView.showAttachment(attachment)
    }
}
