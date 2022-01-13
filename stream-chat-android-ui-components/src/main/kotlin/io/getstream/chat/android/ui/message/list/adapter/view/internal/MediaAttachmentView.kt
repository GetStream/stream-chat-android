package io.getstream.chat.android.ui.message.list.adapter.view.internal

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.Px
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.images.loadAndResize
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.style.setTextStyle
import io.getstream.chat.android.ui.databinding.StreamUiMediaAttachmentViewBinding
import io.getstream.chat.android.ui.message.list.adapter.view.MediaAttachmentViewStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class MediaAttachmentView : ConstraintLayout {
    var attachmentClickListener: AttachmentClickListener? = null
    var attachmentLongClickListener: AttachmentLongClickListener? = null
    var giphyBadgeEnabled: Boolean = true

    internal val binding: StreamUiMediaAttachmentViewBinding =
        StreamUiMediaAttachmentViewBinding.inflate(streamThemeInflater, this, true)
    private lateinit var style: MediaAttachmentViewStyle

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
        style = MediaAttachmentViewStyle(context, attrs)
        binding.loadingProgressBar.indeterminateDrawable = style.progressIcon
        binding.moreCountLabel.setTextStyle(style.moreCountTextStyle)
        binding.giphyLabel.setImageDrawable(style.giphyIcon)
    }

    fun showAttachment(attachment: Attachment, andMoreCount: Int = NO_MORE_COUNT) {
        val url = attachment.imagePreviewUrl ?: attachment.titleLink ?: attachment.ogUrl ?: return

        showImageByUrl(url) {
            if (andMoreCount > NO_MORE_COUNT) {
                showMoreCount(andMoreCount)
            }
        }

        setOnClickListener { attachmentClickListener?.onAttachmentClick(attachment) }
        setOnLongClickListener {
            attachmentLongClickListener?.onAttachmentLongClick()
            true
        }
    }

    fun showGiphy(attachment: Attachment) {
        val url = attachment.giphyUrl(GiphyInfoType.FIXED_HEIGHT) ?: attachment.let {
            it.imagePreviewUrl ?: it.titleLink ?: it.ogUrl
        } ?: return

        binding.loadImage.updateLayoutParams {
            this.height = GIPHY_INFO_DEFAULT_HEIGHT_DP.dpToPx()
        }

        binding.giphyLabel.isVisible = true

        CoroutineScope(DispatcherProvider.Main).launch {
            binding.imageView.setImageDrawable(style.placeholderIcon)

            binding.imageView.loadAndResize(
                data = url,
                placeholderDrawable = style.placeholderIcon,
                maxHeight = GIPHY_INFO_DEFAULT_HEIGHT_DP.dpToPx(),
                container = this@MediaAttachmentView,
                onStart = { binding.loadImage.isVisible = true }
            ) {
                binding.loadImage.isVisible = false
            }
        }
    }

    private fun showImageByUrl(imageUrl: String, onCompleteCallback: () -> Unit) {
        binding.imageView.load(
            data = imageUrl,
            placeholderDrawable = style.placeholderIcon,
            onComplete = {
                onCompleteCallback()
            }
        )
    }

    private fun showMoreCount(andMoreCount: Int) {
        binding.moreCount.isVisible = true
        binding.moreCountLabel.text =
            context.getString(R.string.stream_ui_message_list_attachment_more_count, andMoreCount)
    }

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

    private fun setImageShape(shapeAppearanceModel: ShapeAppearanceModel) {
        binding.imageView.shapeAppearanceModel = shapeAppearanceModel
        binding.moreCount.background = MaterialShapeDrawable(shapeAppearanceModel).apply {
            setTint(style.moreCountOverlayColor)
        }
    }

    companion object {
        private const val NO_MORE_COUNT = 0
        private const val DEFAULT_MARGIN_FOR_CONTAINER_DP = 4

        private const val GIPHY_INFO_DEFAULT_WIDTH_DP = 200
        private const val GIPHY_INFO_DEFAULT_HEIGHT_DP = 200

        private fun Attachment.giphyInfo(field: GiphyInfoType): GiphyInfo? {
            val giphyInfoMap =
                (extraData[ModelType.attach_giphy] as? Map<String, Any>?)?.get(field.value) as? Map<String, String>?

            return giphyInfoMap?.let { map ->
                GiphyInfo(
                    url = map["url"] ?: "",
                    width = map["width"]?.toInt() ?: GIPHY_INFO_DEFAULT_WIDTH_DP.dpToPx(),
                    height = map["height"]?.toInt() ?: GIPHY_INFO_DEFAULT_HEIGHT_DP.dpToPx()
                )
            }
        }

        private enum class GiphyInfoType(val value: String) {
            ORIGINAL("original"),
            FIXED_HEIGHT("fixed_height"),
            FIXED_HEIGHT_DOWNSAMPLED("fixed_height_downsampled")
        }

        private data class GiphyInfo(
            val url: String,
            @Px val width: Int,
            @Px val height: Int,
        )

        private fun Attachment.giphyUrl(type: GiphyInfoType): String? = giphyInfo(type)?.url
    }
}
