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

package io.getstream.chat.android.ui.message.list.adapter.view

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.ViewStyle
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.common.style.TextStyle

/**
 * Style for [io.getstream.chat.android.ui.message.list.adapter.view.internal.ImageAttachmentView].
 * Use this class together with [TransformStyle.imageAttachmentStyleTransformer] to change styles programmatically.
 *
 * @param progressIcon Animated progress drawable. Default value is [R.drawable.stream_ui_rotating_indeterminate_progress_gradient].
 * @param placeholderIcon Displayed while the image is Loading.
 * @param imageBackgroundColor Image background. Default value is [R.color.stream_ui_grey].
 * @param moreCountOverlayColor More count semi-transparent overlay color. Default value is [R.color.stream_ui_overlay].
 * @param moreCountTextStyle Appearance for "more count" text.
 */
public data class ImageAttachmentViewStyle(
    public val progressIcon: Drawable,
    public val placeholderIcon: Drawable,
    @ColorInt val imageBackgroundColor: Int,
    @ColorInt val moreCountOverlayColor: Int,
    public val moreCountTextStyle: TextStyle,
) : ViewStyle {
    internal companion object {
        /**
         * Fetches styled attributes and returns them wrapped inside of [ImageAttachmentViewStyle].
         */
        operator fun invoke(context: Context, attrs: AttributeSet?): ImageAttachmentViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.ImageAttachmentView,
                R.attr.streamUiMessageListImageAttachmentStyle,
                R.style.StreamUi_MessageList_ImageAttachment
            ).use { a ->
                val progressIcon = a.getDrawable(R.styleable.ImageAttachmentView_streamUiImageAttachmentProgressIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_rotating_indeterminate_progress_gradient)!!

                val imageBackgroundColor = a.getColor(
                    R.styleable.ImageAttachmentView_streamUiImageAttachmentImageBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_grey)
                )

                val moreCountOverlayColor = a.getColor(
                    R.styleable.ImageAttachmentView_streamUiImageAttachmentMoreCountOverlayColor,
                    context.getColorCompat(R.color.stream_ui_overlay)
                )

                val moreCountTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.ImageAttachmentView_streamUiImageAttachmentMoreCountTextSize,
                        context.getDimension(R.dimen.stream_ui_message_image_attachment_more_count_text_size)
                    )
                    .color(
                        R.styleable.ImageAttachmentView_streamUiImageAttachmentMoreCountTextColor,
                        context.getColorCompat(R.color.stream_ui_literal_white)
                    )
                    .font(
                        R.styleable.ImageAttachmentView_streamUiImageAttachmentMoreCountFontAssets,
                        R.styleable.ImageAttachmentView_streamUiImageAttachmentMoreCountTextFont
                    )
                    .style(
                        R.styleable.ImageAttachmentView_streamUiImageAttachmentMoreCountTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val placeholderIcon =
                    a.getDrawable(R.styleable.ImageAttachmentView_streamUiImageAttachmentPlaceHolderIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_picture_placeholder)!!

                return ImageAttachmentViewStyle(
                    progressIcon = progressIcon,
                    imageBackgroundColor = imageBackgroundColor,
                    moreCountOverlayColor = moreCountOverlayColor,
                    moreCountTextStyle = moreCountTextStyle,
                    placeholderIcon = placeholderIcon,
                ).let(TransformStyle.imageAttachmentStyleTransformer::transform)
            }
        }
    }
}
