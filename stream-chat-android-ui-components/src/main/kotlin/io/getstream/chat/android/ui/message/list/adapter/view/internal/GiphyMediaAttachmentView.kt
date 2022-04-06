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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.images.loadAndResize
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiGiphyMediaAttachmentViewBinding
import io.getstream.chat.android.ui.message.list.adapter.view.GiphyMediaAttachmentViewStyle
import io.getstream.chat.android.ui.utils.GIPHY_INFO_DEFAULT_HEIGHT_DP
import io.getstream.chat.android.ui.utils.GiphyInfoType
import io.getstream.chat.android.ui.utils.giphyInfo
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

    /**
     * Initializes the style and configuration of the View based on the [style].
     */
    private fun init(attrs: AttributeSet?) {
        style = GiphyMediaAttachmentViewStyle(context, attrs)

        binding.loadingProgressBar.indeterminateDrawable = style.progressIcon
        binding.giphyLabel.setImageDrawable(style.giphyIcon)

        binding.imageView.scaleType = style.scaleType
        binding.imageView.setBackgroundColor(style.imageBackgroundColor)
    }

    /**
     * Displays a Giphy inside its container. Depending on [GiphyMediaAttachmentViewStyle.giphyType]
     * it displays the Giphy either inside of a constant height that resizes its width to keep aspect ratio or a
     * resizeable container that is resized according to the GIF.
     *
     * @param attachment The attachment holding the Giphy information.
     * @param giphyType The type of Giphy to load. By default we fetch this info from the [style].
     */
    public fun showGiphy(
        attachment: Attachment,
        giphyType: GiphyInfoType = style.giphyType,
    ) {
        val giphyInfo = attachment.giphyInfo(giphyType)

        val url = giphyInfo?.url ?: attachment.let {
            it.imagePreviewUrl ?: it.titleLink ?: it.ogUrl
        } ?: return

        val height = if (style.giphyType == GiphyInfoType.ORIGINAL) {
            giphyInfo?.height ?: GIPHY_INFO_DEFAULT_HEIGHT_DP.dpToPx()
        } else {
            (giphyInfo?.height ?: GIPHY_INFO_DEFAULT_HEIGHT_DP).dpToPx()
        }
        val width = giphyInfo?.width

        this.updateLayoutParams {
            this.height = height
            if (width != null && style.giphyType == GiphyInfoType.ORIGINAL) {
                this.width = width
            }
        }
        binding.imageView.updateLayoutParams {
            this.height = height
            if (width != null && style.giphyType == GiphyInfoType.ORIGINAL) {
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
     *
     * @param url The URL required to load the giphy image.
     */
    private fun loadGiphy(url: String) {
        CoroutineScope(DispatcherProvider.Main).launch {
            binding.imageView.loadAndResize(
                data = url,
                placeholderDrawable = style.placeholderIcon,
                onStart = { binding.loadImage.isVisible = true }
            ) { binding.loadImage.isVisible = false }
        }
    }

    /**
     * Creates and sets the shape of the Giphy image container.
     *
     * @param topLeft The top left container corner.
     * @param topRight The top right container corner.
     * @param bottomRight The bottom right container corner.
     * @param topLeft The bottom left container corner.
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
     *
     * @param shapeAppearanceModel The shape defining model, holding all the shape values, like the corner radius.
     */
    private fun setImageShape(shapeAppearanceModel: ShapeAppearanceModel) {
        binding.imageView.shapeAppearanceModel = shapeAppearanceModel
    }
}
