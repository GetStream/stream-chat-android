package io.getstream.chat.android.ui.message.list.adapter.view.internal

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.Px
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.images.loadAndResize
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
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

/**
 * View used to display Giphy images inside a shapeable container.
 */
public class GiphyMediaAttachmentView : ConstraintLayout {

    /**
     * Binding generated for [io.getstream.chat.android.ui.R.layout.stream_ui_giphy_media_attachment_view].
     */
    internal val binding: StreamUiGiphyMediaAttachmentViewBinding =
        StreamUiGiphyMediaAttachmentViewBinding.inflate(streamThemeInflater, this, true)

    /**
     * Style applied to [GiphyMediaAttachmentView].
     */
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
        binding.imageView.setBackgroundColor(style.imageBackgroundColor)
    }

    /**
     * Displays a Giphy inside its container. Depending on [GiphyMediaAttachmentViewStyle.isConstantSizeEnabled]
     * it displays the Giphy either inside of a constant height that resizes its width to keep aspect ratio or a
     * resizeable container that is resized according to the GIF.
     */
    public fun showGiphy(attachment: Attachment) {
        val url = attachment.giphyUrl(style.giphyType) ?: attachment.let {
            it.imagePreviewUrl ?: it.titleLink ?: it.ogUrl
        } ?: return

        val giphyInfo = attachment.giphyInfo(style.giphyType)

        val height = (giphyInfo?.height ?: GIPHY_INFO_DEFAULT_HEIGHT_DP).dpToPx()
        val width = giphyInfo?.width?.dpToPx()

        this.updateLayoutParams {
            this.height = height
            if (width != null && width < height) {
                this.width = width
            }
        }
        binding.imageView.updateLayoutParams {
            this.height = height
            if (width != null && width < height) {
                this.width = width
            }
        }

        loadGiphy(url)
    }

    /**
     * Displays the Giphy image inside of the container. We call [loadAndResize] here because we need to
     * resize the container's width based on the height.
     *
     * In case of original sized giphies, we don't have a constant max height. For resizable giphies, we rely on the
     * information from the API to give use the constant height.
     */
    public fun loadGiphy(url: String) {
        CoroutineScope(DispatcherProvider.Main).launch {
            binding.imageView.loadAndResize(
                data = url,
                placeholderDrawable = style.placeholderIcon,
                container = this@GiphyMediaAttachmentView,
                onStart = { binding.loadImage.isVisible = true }
            ) { binding.loadImage.isVisible = false }
        }
    }

    /**
     * Creates and sets the shape of the Giphy image container.
     */
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

    /**
     * Applies the shape to the container that holds the Giphy image.
     */
    private fun setImageShape(shapeAppearanceModel: ShapeAppearanceModel) {
        binding.imageView.shapeAppearanceModel = shapeAppearanceModel
    }

    public companion object {
        /**
         * Default width used for Giphy Images if no width metadata is available.
         */
        private const val GIPHY_INFO_DEFAULT_WIDTH_DP = 200

        /**
         * Default height used for Giphy Images if no width metadata is available.
         */
        private const val GIPHY_INFO_DEFAULT_HEIGHT_DP = 200

        /**
         * Returns an object containing extra information about the Giphy image based
         * on its type.
         *
         * @see GiphyInfoType
         * @see GiphyInfo
         */
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

        /**
         * Enum class that holds a value used to obtain Giphy images of different quality level.
         */
        public enum class GiphyInfoType(public val value: String) {
            /**
             * Original quality
             */
            ORIGINAL("original"),

            /**
             * Lower quality with a fixed height, adjusts width according to the Giphy aspect ratio
             */
            FIXED_HEIGHT("fixed_height"),

            /**
             * Lower quality with a fixed height with width adjusted according to the aspect ratio
             * and played at a lower framerate.
             */
            FIXED_HEIGHT_DOWNSAMPLED("fixed_height_downsampled")
        }

        /**
         * Contains extra information about Giphy attachments.
         *
         * @param url Url for the Giphy image.
         * @param width The with of the Giphy image.
         * @param height The Height of the Giphy Image.
         */
        private data class GiphyInfo(
            val url: String,
            @Px val width: Int,
            @Px val height: Int,
        )

        /**
         * Returns a url for a Giphy image based on its type.
         *
         * @see GiphyInfoType
         */
        private fun Attachment.giphyUrl(type: GiphyInfoType): String? = giphyInfo(type)?.url
    }
}