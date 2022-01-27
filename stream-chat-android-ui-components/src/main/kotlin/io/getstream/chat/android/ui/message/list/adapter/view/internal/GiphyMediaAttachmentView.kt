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
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiGiphyMediaAttachmentViewBinding
import io.getstream.chat.android.ui.message.list.adapter.view.GiphyMediaAttachmentViewStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// TODO - document everything
public class GiphyMediaAttachmentView : ConstraintLayout {

    internal val binding: StreamUiGiphyMediaAttachmentViewBinding =
        StreamUiGiphyMediaAttachmentViewBinding.inflate(streamThemeInflater, this, true)
    private lateinit var style: GiphyMediaAttachmentViewStyle

    public constructor(context: Context) : this(context, null, 0)

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        style = GiphyMediaAttachmentViewStyle(context, attrs)
        binding.loadingProgressBar.indeterminateDrawable = style.progressIcon
        binding.giphyLabel.setImageDrawable(style.giphyIcon)
        binding.imageView.scaleType = style.scaleType
    }

    public fun showGiphy(attachment: Attachment) {
        val url = attachment.giphyUrl(style.giphyType) ?: attachment.let {
            it.imagePreviewUrl ?: it.titleLink ?: it.ogUrl
        } ?: return

        if (style.giphyConstantSizeEnabled) {
            showConstantSizeGiphy(url)
        } else {
            showResizeableGiphy(url)
        }
    }

    private fun showConstantSizeGiphy(url: String) {
        binding.mediaAttachmentContent.updateLayoutParams {
            this.height = style.giphyHeight
            this.width = style.giphyHeight
        }

        binding.loadImage.updateLayoutParams {
            this.height = style.giphyHeight
            this.width = style.giphyHeight
        }

        CoroutineScope(DispatcherProvider.Main).launch {
            binding.imageView.setImageDrawable(style.placeholderIcon)

            binding.imageView.load(
                data = url,
                placeholderDrawable = style.placeholderIcon,
                onStart = { binding.loadImage.isVisible = true },
                onComplete = { binding.loadImage.isVisible = false }
            )
        }
    }

    private fun showResizeableGiphy(url: String) {
        binding.mediaAttachmentContent.updateLayoutParams {
            this.height = style.giphyHeight
        }

        binding.loadImage.updateLayoutParams {
            this.height = style.giphyHeight
        }

        binding.giphyLabel.isVisible = true

        CoroutineScope(DispatcherProvider.Main).launch {
            binding.imageView.setImageDrawable(style.placeholderIcon)

            binding.imageView.loadAndResize(
                data = url,
                placeholderDrawable = style.placeholderIcon,
                maxHeight = style.giphyHeight,
                container = this@GiphyMediaAttachmentView,
                onStart = { binding.loadImage.isVisible = true },
                onComplete = { binding.loadImage.isVisible = false }
            )
        }
    }

    public fun setImageShapeByCorners(
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
        binding.imageView.background = MaterialShapeDrawable(shapeAppearanceModel).apply {
            setTint(style.imageBackgroundColor)
        }
    }

    public companion object {
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

        public enum class GiphyInfoType(public val value: String) {
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