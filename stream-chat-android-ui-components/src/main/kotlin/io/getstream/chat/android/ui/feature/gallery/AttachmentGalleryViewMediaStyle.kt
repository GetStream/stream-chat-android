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

package io.getstream.chat.android.ui.feature.gallery

import android.app.ActionBar
import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.helper.ViewStyle
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getColorOrNull
import io.getstream.chat.android.ui.utils.extensions.getDimensionOrNull
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat

/**
 * Controls the appearance of the main portion of the attachment gallery used to view media.
 *
 * @param viewMediaPlayVideoButtonIcon The drawable for the play button icon displayed above videos in the main viewing
 * area of the gallery.
 * @param viewMediaPlayVideoButtonIcon The drawable for the play button icon displayed above videos in the main viewing
 * area of the gallery.
 * @param viewMediaPlayVideoIconTint Tints the icon overlaid above videos in the main viewing area of the gallery.
 * @param viewMediaPlayVideoIconBackgroundColor The background color of the View containing the play button overlaid
 * above the main viewing area of the gallery.
 * @param viewMediaPlayVideoIconCornerRadius The corner radius of the play button in the main viewing area of the
 * gallery.
 * @param viewMediaPlayVideoIconElevation The elevation of the play button in the main viewing area of the gallery.
 * @param viewMediaPlayVideoIconPaddingTop Sets the top padding of the play video icon displayed above the main viewing
 * area of the gallery.
 * @param viewMediaPlayVideoIconPaddingBottom Sets the bottom padding of the play video icon in the main viewing
 * area of the gallery.
 * @param viewMediaPlayVideoIconPaddingStart Sets the start padding of the play video icon in the main viewing
 * area of the gallery.
 * @param viewMediaPlayVideoIconPaddingEnd  Sets the end padding of the play video icon in the main viewing
 * area of the gallery.
 * @param viewMediaPlayVideoIconWidth Sets the width of the play video button in the main viewing area of the gallery.
 * @param viewMediaPlayVideoIconHeight Sets the width of the play video button in the main viewing area of the gallery.
 * @param imagePlaceholder A placeholder drawable used before the image is loaded.
 */
public data class AttachmentGalleryViewMediaStyle(
    val viewMediaPlayVideoButtonIcon: Drawable?,
    @ColorInt val viewMediaPlayVideoIconTint: Int?,
    @ColorInt val viewMediaPlayVideoIconBackgroundColor: Int,
    val viewMediaPlayVideoIconCornerRadius: Float,
    val viewMediaPlayVideoIconElevation: Float,
    val viewMediaPlayVideoIconPaddingTop: Int,
    val viewMediaPlayVideoIconPaddingBottom: Int,
    val viewMediaPlayVideoIconPaddingStart: Int,
    val viewMediaPlayVideoIconPaddingEnd: Int,
    val viewMediaPlayVideoIconWidth: Int,
    val viewMediaPlayVideoIconHeight: Int,
    val imagePlaceholder: Drawable?,
) : ViewStyle {

    internal companion object {

        operator fun invoke(context: Context, attrs: AttributeSet?): AttachmentGalleryViewMediaStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.AttachmentGalleryVideoAttachments,
                R.attr.streamUiAttachmentGalleryVideoAttachmentsStyle,
                R.style.StreamUi_AttachmentGallery_VideoAttachments,
            ).let { styledAttributes ->
                val style = AttachmentGalleryViewMediaStyle(context, styledAttributes)
                styledAttributes.recycle()
                return style
            }
        }

        operator fun invoke(context: Context, it: TypedArray): AttachmentGalleryViewMediaStyle {
            val viewMediaPlayVideoButtonIcon = it.getDrawable(
                R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryViewMediaPlayVideoButtonIcon,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_play)

            val viewMediaPlayVideoIconTint = it.getColorOrNull(
                R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryViewMediaPlayVideoIconTint,
            )

            val viewMediaPlayVideoIconBackgroundColor =
                it.getColor(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryViewMediaPlayVideoIconBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_literal_white),
                )

            val viewMediaPlayVideoIconCornerRadius =
                it.getDimension(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryViewMediaPlayVideoIconCornerRadius,
                    0f,
                )

            val viewMediaPlayVideoIconElevation =
                it.getDimension(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryViewMediaPlayVideoIconElevation,
                    0f,
                )

            val viewMediaPlayVideoIconPaddingTop =
                it.getDimensionPixelSize(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryViewMediaPlayVideoIconPaddingTop,
                    0,
                )

            val viewMediaPlayVideoIconPaddingBottom =
                it.getDimensionPixelSize(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryViewMediaPlayVideoIconPaddingBottom,
                    0,
                )

            val viewMediaPlayVideoIconPaddingStart =
                it.getDimensionPixelSize(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryViewMediaPlayVideoIconPaddingStart,
                    0,
                )

            val viewMediaPlayVideoIconPaddingEnd =
                it.getDimensionPixelSize(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryViewMediaPlayVideoIconPaddingEnd,
                    0,
                )

            val viewMediaPlayVideoIconPadding =
                it.getDimensionOrNull(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryViewMediaPlayVideoIconPadding,
                )?.toInt()

            val viewMediaPlayVideoIconWidth =
                it.getLayoutDimension(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryViewMediaPlayIconWidth,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                )

            val viewMediaPlayVideoIconHeight =
                it.getLayoutDimension(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryViewMediaPlayIconHeight,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                )

            val imagePlaceholder = it.getDrawable(
                R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryViewMediaImagePlaceholder,
            ) ?: ContextCompat.getDrawable(
                context,
                R.drawable.stream_ui_picture_placeholder,
            )

            return AttachmentGalleryViewMediaStyle(
                viewMediaPlayVideoButtonIcon = viewMediaPlayVideoButtonIcon,
                viewMediaPlayVideoIconTint = viewMediaPlayVideoIconTint,
                viewMediaPlayVideoIconBackgroundColor = viewMediaPlayVideoIconBackgroundColor,
                viewMediaPlayVideoIconCornerRadius = viewMediaPlayVideoIconCornerRadius,
                viewMediaPlayVideoIconElevation = viewMediaPlayVideoIconElevation,
                viewMediaPlayVideoIconPaddingTop = viewMediaPlayVideoIconPadding ?: viewMediaPlayVideoIconPaddingTop,
                viewMediaPlayVideoIconPaddingBottom = viewMediaPlayVideoIconPadding
                    ?: viewMediaPlayVideoIconPaddingBottom,
                viewMediaPlayVideoIconPaddingStart = viewMediaPlayVideoIconPadding
                    ?: viewMediaPlayVideoIconPaddingStart,
                viewMediaPlayVideoIconPaddingEnd = viewMediaPlayVideoIconPadding ?: viewMediaPlayVideoIconPaddingEnd,
                viewMediaPlayVideoIconWidth = viewMediaPlayVideoIconWidth,
                viewMediaPlayVideoIconHeight = viewMediaPlayVideoIconHeight,
                imagePlaceholder = imagePlaceholder,
            ).let(TransformStyle.attachmentGalleryViewMediaStyle::transform)
        }
    }
}
