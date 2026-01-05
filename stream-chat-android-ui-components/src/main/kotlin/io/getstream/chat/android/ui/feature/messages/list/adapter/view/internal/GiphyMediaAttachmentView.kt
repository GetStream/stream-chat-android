/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.utils.GiphyInfo
import io.getstream.chat.android.ui.common.utils.GiphyInfoType
import io.getstream.chat.android.ui.common.utils.GiphySizingMode
import io.getstream.chat.android.ui.common.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.ui.common.utils.giphyInfo
import io.getstream.chat.android.ui.databinding.StreamUiGiphyMediaAttachmentViewBinding
import io.getstream.chat.android.ui.feature.messages.list.adapter.view.GiphyMediaAttachmentViewStyle
import io.getstream.chat.android.ui.utils.extensions.constrainViewToParentBySide
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.utils.extensions.updateConstraints
import io.getstream.chat.android.ui.utils.loadAndResize
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
    internal lateinit var style: GiphyMediaAttachmentViewStyle

    public constructor(context: Context) : this(context, null, 0)

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
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

        if (style.sizingMode == GiphySizingMode.ADAPTIVE) {
            applyAdaptiveSizing(
                giphyInfo = giphyInfo,
                giphyUrl = url,
            )
        } else {
            applyFixedSizing(
                giphyUrl = url,
            )
        }
    }

    /**
     * Automatically resizes the Giphy while respecting the Giphy's
     * aspect ratio.
     *
     * This method is meant to ignore [GiphyMediaAttachmentViewStyle.width],
     * [GiphyMediaAttachmentViewStyle.height] and [GiphyMediaAttachmentViewStyle.dimensionRatio].
     *
     * @param giphyInfo Used to determine the aspect ratio of the Giphy.
     * @param giphyUrl The URL to the Giphy we are loading.
     */
    private fun applyAdaptiveSizing(giphyInfo: GiphyInfo?, giphyUrl: String) {
        binding.root.updateConstraints {
            constrainMaxWidth(binding.imageView.id, ViewGroup.LayoutParams.MATCH_PARENT)
            val ratio = (giphyInfo?.width ?: 1).toFloat() / (giphyInfo?.height ?: 1).toFloat()
            setDimensionRatio(binding.imageView.id, ratio.toString())

            constrainViewToParentBySide(binding.imageView, ConstraintSet.LEFT)
            constrainViewToParentBySide(binding.imageView, ConstraintSet.RIGHT)
            constrainViewToParentBySide(binding.imageView, ConstraintSet.TOP)
            constrainViewToParentBySide(binding.imageView, ConstraintSet.BOTTOM)
        }

        loadGiphy(giphyUrl)
    }

    /**
     * Applies fixed sizing to the Giphy container set via
     * [GiphyMediaAttachmentViewStyle].
     *
     * @param giphyUrl The URL to the Giphy we are loading.
     */
    private fun applyFixedSizing(giphyUrl: String) {
        binding.root.updateConstraints {
            constrainWidth(binding.imageView.id, style.width)

            when {
                // If the user has set a height, respect the height
                style.height != GiphyMediaAttachmentViewStyle.NO_GIVEN_HEIGHT ->
                    constrainHeight(binding.imageView.id, style.height)
                // If the user has set a dimension ratio, respect the dimension ratio
                style.dimensionRatio != GiphyMediaAttachmentViewStyle.NO_GIVEN_DIMENSION_RATIO ->
                    setDimensionRatio(binding.imageView.id, style.dimensionRatio.toString())
                // If the user has set no dimension ratio or height, use a square dimension ratio
                else -> setDimensionRatio(binding.imageView.id, "1")
            }
        }

        loadGiphy(giphyUrl)
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
                onStart = { binding.loadImage.isVisible = true },
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

    /**
     * Checks if [style] has been initialized.
     *
     * @return True if [style] has been initialized, false otherwise.
     */
    internal fun isStyleInitialized(): Boolean = this::style.isInitialized
}
