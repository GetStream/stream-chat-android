package io.getstream.chat.android.ui.message.list.adapter.view.internal

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.getstream.sdk.chat.images.StreamImageLoader
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.internal.loadAttachmentThumb
import io.getstream.chat.android.ui.message.list.QuotedAttachmentViewStyle

/**
 * View tasked to show the attachments supported by default.
 */
internal class QuotedAttachmentView : AppCompatImageView {

    constructor(context: Context) : super(context.createStreamThemeWrapper()) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private lateinit var style: QuotedAttachmentViewStyle

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        layoutParams = layoutParams.apply {
            height = style.height
            width = style.width
        }
    }

    /**
     * Show the attachment sent inside the message.
     *
     * @param message The message containing the attachments.
     */
    fun showAttachment(attachment: Attachment) {
        when (attachment.type) {
            ModelType.attach_file, ModelType.attach_video -> loadAttachmentThumb(attachment)
            ModelType.attach_image -> showAttachmentThumb(attachment.imagePreviewUrl)
            ModelType.attach_giphy -> showAttachmentThumb(attachment.thumbUrl)
            else -> showAttachmentThumb(attachment.image)
        }
    }

    /**
     * Sets the attachment thumbnail in case of images and giphy.
     *
     * @param url The url to the image resource.
     */
    private fun showAttachmentThumb(url: String?) {
        load(
            data = url,
            transformation = StreamImageLoader.ImageTransformation.RoundedCorners(style.radius.toFloat())
        )
    }

    private fun init(context: Context) {
        this.style = QuotedAttachmentViewStyle(context)
    }
}