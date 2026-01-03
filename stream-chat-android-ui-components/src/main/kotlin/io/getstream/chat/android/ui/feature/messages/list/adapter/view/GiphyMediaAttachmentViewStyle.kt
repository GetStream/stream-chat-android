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

package io.getstream.chat.android.ui.feature.messages.list.adapter.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.ColorInt
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.utils.GiphyInfoType
import io.getstream.chat.android.ui.common.utils.GiphySizingMode
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat
import io.getstream.chat.android.ui.utils.extensions.getEnum
import io.getstream.chat.android.ui.utils.extensions.use

/**
 * Sets the style for [io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal.GiphyMediaAttachmentView]
 * by obtaining styled attributes.
 *
 * @param progressIcon Displayed while the Giphy is Loading.
 * @param giphyIcon Displays the Giphy logo over the Giphy image.
 * @param placeholderIcon Displayed while the Giphy is Loading.
 * @param imageBackgroundColor Sets the background colour for the Giphy container.
 * @param giphyType Sets the Giphy type which directly affects image quality and if the container is resized or not.
 * @param scaleType Sets the scaling type for loading the image. E.g. 'centerCrop', 'fitCenter', etc...
 * @param sizingMode Sets the way the Giphy container scales itself, either adaptive or of a fixed size.
 * @param width Sets the width of the Giphy container. This value is ignored if Giphys are adaptively sized.
 * @param height Sets the height of the Giphy container. This value is ignored if Giphys are adaptively sized.
 * @param dimensionRatio Sets the dimension ratio of the Giphy container. This value is ignored if
 * Giphys are adaptively sized.
 */
public class GiphyMediaAttachmentViewStyle(
    public val progressIcon: Drawable,
    public val giphyIcon: Drawable,
    public val placeholderIcon: Drawable,
    @ColorInt public val imageBackgroundColor: Int,
    public val giphyType: GiphyInfoType,
    public val scaleType: ImageView.ScaleType,
    public val sizingMode: GiphySizingMode,
    public val width: Int,
    public val height: Int,
    public val dimensionRatio: Float,
) {
    public companion object {
        /**
         * Fetches styled attributes and returns them wrapped inside of [GiphyMediaAttachmentViewStyle].
         */
        internal operator fun invoke(context: Context, attrs: AttributeSet?): GiphyMediaAttachmentViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.GiphyMediaAttachmentView,
                R.attr.streamUiMessageListGiphyAttachmentStyle,
                R.style.StreamUi_MessageList_GiphyMediaAttachment,
            ).use { attributes ->
                val progressIcon =
                    attributes.getDrawable(R.styleable.GiphyMediaAttachmentView_streamUiGiphyMediaAttachmentProgressIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_rotating_indeterminate_progress_gradient)!!

                val giphyIcon =
                    attributes.getDrawable(R.styleable.GiphyMediaAttachmentView_streamUiGiphyMediaAttachmentGiphyIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_giphy_label)!!

                val imageBackgroundColor = attributes.getColor(
                    R.styleable.GiphyMediaAttachmentView_streamUiGiphyMediaAttachmentImageBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_grey),
                )

                val placeholderIcon =
                    attributes.getDrawable(R.styleable.GiphyMediaAttachmentView_streamUiGiphyMediaAttachmentPlaceHolderIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_picture_placeholder)!!

                val giphyType =
                    attributes.getEnum(
                        R.styleable.GiphyMediaAttachmentView_streamUiGiphyMediaAttachmentGiphyType,
                        GiphyInfoType.FIXED_HEIGHT,
                    )

                val scaleType =
                    attributes.getEnum(
                        R.styleable.GiphyMediaAttachmentView_streamUiGiphyMediaAttachmentScaleType,
                        ImageView.ScaleType.FIT_CENTER,
                    )

                val sizingMode = attributes.getEnum(
                    R.styleable.GiphyMediaAttachmentView_streamUiGiphyMediaAttachmentSizingMode,
                    GiphySizingMode.ADAPTIVE,
                )

                val width =
                    attributes.getLayoutDimension(
                        R.styleable.GiphyMediaAttachmentView_streamUiGiphyMediaAttachmentWidth,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )

                val height =
                    attributes.getLayoutDimension(
                        R.styleable.GiphyMediaAttachmentView_streamUiGiphyMediaAttachmentHeight,
                        NO_GIVEN_HEIGHT,
                    )

                val dimensionRatio =
                    attributes.getFloat(
                        R.styleable.GiphyMediaAttachmentView_streamUiGiphyMediaAttachmentDimensionRatio,
                        SQUARE_DIMENSION_RATIO,
                    )

                return GiphyMediaAttachmentViewStyle(
                    progressIcon = progressIcon,
                    giphyIcon = giphyIcon,
                    placeholderIcon = placeholderIcon,
                    imageBackgroundColor = imageBackgroundColor,
                    giphyType = giphyType,
                    scaleType = scaleType,
                    sizingMode = sizingMode,
                    width = width,
                    height = height,
                    dimensionRatio = dimensionRatio,
                )
            }
        }

        /**
         * Signifies that the user has not set a dimension ratio.
         */
        public const val NO_GIVEN_DIMENSION_RATIO: Float = -1f

        /**
         * Signifies that the user has not set height.
         * Used to set the initial condition.
         */
        public const val NO_GIVEN_HEIGHT: Int = -1

        /**
         * A dimension ratios that gives an equal height as width,
         * hence creating a square appearance.
         */
        public const val SQUARE_DIMENSION_RATIO: Float = 1f
    }
}
