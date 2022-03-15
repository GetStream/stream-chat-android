package io.getstream.chat.android.ui.message.list.adapter.view.internal

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.utils.extensions.constrainViewToParentBySide
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import com.getstream.sdk.chat.utils.extensions.updateConstraints
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.style.setTextStyle
import io.getstream.chat.android.ui.databinding.StreamUiImageAttachmentViewBinding
import io.getstream.chat.android.ui.message.list.adapter.view.ImageAttachmentViewStyle

/**
 * View used to display image attachments.
 *
 * Giphy images are handled by a separate View.
 * @see GiphyMediaAttachmentView
 */
internal class ImageAttachmentView : ConstraintLayout {
    /**
     * Handles image attachment clicks.
     */
    var attachmentClickListener: AttachmentClickListener? = null

    /**
     * Handles image attachment long clicks.
     */
    var attachmentLongClickListener: AttachmentLongClickListener? = null

    /**
     * Binding for [R.layout.stream_ui_image_attachment_view].
     */
    internal val binding: StreamUiImageAttachmentViewBinding =
        StreamUiImageAttachmentViewBinding.inflate(streamThemeInflater).also {
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
     * Style applied to [ImageAttachmentView].
     */
    private lateinit var style: ImageAttachmentViewStyle

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        style = ImageAttachmentViewStyle(context, attrs)
        binding.loadingProgressBar.indeterminateDrawable = style.progressIcon
        binding.moreCountLabel.setTextStyle(style.moreCountTextStyle)
    }

    /**
     * Displays the images in a message. Displays a count saying how many more
     * images the message contains in case they don't all fit in the preview.
     */
    fun showAttachment(attachment: Attachment, andMoreCount: Int = NO_MORE_COUNT) {
        val url = attachment.imagePreviewUrl ?: attachment.titleLink ?: attachment.ogUrl ?: return
        val showMore = {
            if (andMoreCount > NO_MORE_COUNT) {
                showMoreCount(andMoreCount)
            }
        }

        showImageByUrl(url) {
            showMore()
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
     * Loads the images.
     */
    private fun showImageByUrl(imageUrl: String, onCompleteCallback: () -> Unit) {
        binding.imageView.load(
            data = imageUrl,
            placeholderDrawable = style.placeholderIcon,
            onStart = { showLoading(true) },
            onComplete = {
                showLoading(false)
                onCompleteCallback()
            }
        )
    }

    /**
     * Displays how many more images the message contains that are not
     * able to fit inside the preview.
     */
    private fun showMoreCount(andMoreCount: Int) {
        binding.moreCount.isVisible = true
        binding.moreCountLabel.text =
            context.getString(R.string.stream_ui_message_list_attachment_more_count, andMoreCount)
    }

    /**
     * Creates and sets the shape of the image/s container.
     */
    fun setImageShapeByCorners(
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
            .let(this::setImageShape)
    }

    /**
     * Applies the shape to the image/s container. Also sets the container
     * background color and the overlay color for the label that displays
     * how many more images there are in a message.
     */
    private fun setImageShape(shapeAppearanceModel: ShapeAppearanceModel) {
        binding.imageView.shapeAppearanceModel = shapeAppearanceModel
        binding.loadImage.background = MaterialShapeDrawable(shapeAppearanceModel).apply {
            setTint(style.imageBackgroundColor)
        }
        binding.moreCount.background = MaterialShapeDrawable(shapeAppearanceModel).apply {
            setTint(style.moreCountOverlayColor)
        }
    }

    companion object {
        /**
         * When all images in a message are able to fit in the preview.
         */
        private const val NO_MORE_COUNT = 0
    }
}
