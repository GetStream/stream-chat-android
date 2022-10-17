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

package io.getstream.chat.android.ui.gallery

import android.app.ActionBar
import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getColorOrNull
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat

/**
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
 * @param mediaOverviewPlayVideoButtonIcon The drawable for the play button icon overlaid above video attachments in
 * the media overview segment of the gallery.
 * @param mediaOverviewPlayVideoIconTint Tints the icon overlaid on top of video attachment previews in the media
 * overview segment of the gallery.
 * @param mediaOverviewPlayVideoIconBackgroundColor The background color of the View containing the play button in
 * the media overview segment of the gallery.
 * @param mediaOverviewPlayVideoIconCornerRadius The corner radius of the play button in the media
 * overview segment of the gallery..
 * @param mediaOverviewPlayVideoIconElevation The elevation of the play button in the media
 * overview segment of the gallery
 * @param mediaOverviewPlayVideoIconPaddingTop Sets the top padding of the play video icon in the media
 * overview segment of the gallery.
 * @param mediaOverviewPlayVideoIconPaddingBottom Sets the bottom padding of the play video icon in the media
 * overview segment of the gallery.
 * @param mediaOverviewPlayVideoIconPaddingStart Sets the start padding of the play video icon in the media
 * overview segment of the gallery.
 * @param mediaOverviewPlayVideoIconPaddingEnd  Sets the end padding of the play video icon in the media
 * overview segment of the gallery.

 */
public data class AttachmentGalleryVideoAttachmentsStyle(
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
    val mediaOverviewPlayVideoButtonIcon: Drawable?,
    @ColorInt val mediaOverviewPlayVideoIconTint: Int?,
    @ColorInt val mediaOverviewPlayVideoIconBackgroundColor: Int,
    val mediaOverviewPlayVideoIconCornerRadius: Float,
    val mediaOverviewPlayVideoIconElevation: Float,
    val mediaOverviewPlayVideoIconPaddingTop: Int,
    val mediaOverviewPlayVideoIconPaddingBottom: Int,
    val mediaOverviewPlayVideoIconPaddingStart: Int,
    val mediaOverviewPlayVideoIconPaddingEnd: Int,
) {

    internal companion object {

        operator fun invoke(context: Context, attrs: AttributeSet?): AttachmentGalleryVideoAttachmentsStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.AttachmentGalleryVideoAttachments,
                R.attr.streamUiAttachmentGalleryVideoAttachmentsStyle,
                R.style.StreamUi_AttachmentGallery_VideoAttachments
            ).let { styledAttributes ->
                val style = AttachmentGalleryVideoAttachmentsStyle(context, styledAttributes)
                styledAttributes.recycle()
                return style
            }
        }

        operator fun invoke(context: Context, it: TypedArray): AttachmentGalleryVideoAttachmentsStyle {

            // Parse attributes for the play button inside the main viewing area in the gallery

            val viewMediaPlayVideoButtonIcon = it.getDrawable(
                R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryViewMediaPlayVideoButtonIcon
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_play)

            val viewMediaPlayVideoIconTint = it.getColorOrNull(
                R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryViewMediaPlayVideoIconTint
            )

            val viewMediaPlayVideoIconBackgroundColor =
                it.getColor(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryViewMediaPlayVideoIconBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_literal_white)
                )

            val viewMediaPlayVideoIconCornerRadius =
                it.getDimension(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryViewMediaPlayVideoIconCornerRadius,
                    0f
                )

            val viewMediaPlayVideoIconElevation =
                it.getDimension(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryViewMediaPlayVideoIconElevation,
                    0f
                )

            val viewMediaPlayVideoIconPaddingTop =
                it.getDimensionPixelSize(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryViewMediaPlayVideoIconPaddingTop,
                    0
                )

            val viewMediaPlayVideoIconPaddingBottom =
                it.getDimensionPixelSize(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryViewMediaPlayVideoIconPaddingBottom,
                    0
                )

            val viewMediaPlayVideoIconPaddingStart =
                it.getDimensionPixelSize(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryViewMediaPlayVideoIconPaddingStart,
                    0
                )

            val viewMediaPlayVideoIconPaddingEnd =
                it.getDimensionPixelSize(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryViewMediaPlayVideoIconPaddingEnd,
                    0
                )

            val viewMediaPlayVideoIconWidth =
                it.getLayoutDimension(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryViewMediaPlayIconWidth,
                    ActionBar.LayoutParams.WRAP_CONTENT
                )

            val viewMediaPlayVideoIconHeight =
                it.getLayoutDimension(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryViewMediaPlayIconHeight,
                    ActionBar.LayoutParams.WRAP_CONTENT
                )

            // Parse attributes for the play button inside the overview segment of the attachment gallery

            val mediaOverviewPlayVideoButtonIcon = it.getDrawable(
                R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryMediaOverviewPlayVideoButtonIcon
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_play)

            val mediaOverviewPlayVideoIconTint = it.getColorOrNull(
                R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryMediaOverviewPlayVideoIconTint
            )

            val mediaOverviewPlayVideoIconBackgroundColor =
                it.getColor(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryMediaOverviewPlayVideoIconBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_literal_white)
                )

            val mediaOverviewPlayVideoIconCornerRadius =
                it.getDimension(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryMediaOverviewPlayVideoIconCornerRadius,
                    0f
                )

            val mediaOverviewPlayVideoIconElevation =
                it.getDimension(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryMediaOverviewPlayVideoIconElevation,
                    0f
                )

            val mediaOverviewPlayVideoIconPaddingTop =
                it.getDimensionPixelSize(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryMediaOverviewPlayVideoIconPaddingTop,
                    0
                )

            val mediaOverviewPlayVideoIconPaddingBottom =
                it.getDimensionPixelSize(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryMediaOverviewPlayVideoIconPaddingBottom,
                    0
                )

            val mediaOverviewPlayVideoIconPaddingStart =
                it.getDimensionPixelSize(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryMediaOverviewPlayVideoIconPaddingStart,
                    0
                )

            val mediaOverviewPlayVideoIconPaddingEnd =
                it.getDimensionPixelSize(
                    R.styleable.AttachmentGalleryVideoAttachments_streamUiAttachmentGalleryMediaOverviewPlayVideoIconPaddingEnd,
                    0
                )

            return AttachmentGalleryVideoAttachmentsStyle(
                viewMediaPlayVideoButtonIcon = viewMediaPlayVideoButtonIcon,
                viewMediaPlayVideoIconTint = viewMediaPlayVideoIconTint,
                viewMediaPlayVideoIconBackgroundColor = viewMediaPlayVideoIconBackgroundColor,
                viewMediaPlayVideoIconCornerRadius = viewMediaPlayVideoIconCornerRadius,
                viewMediaPlayVideoIconElevation = viewMediaPlayVideoIconElevation,
                viewMediaPlayVideoIconPaddingTop = viewMediaPlayVideoIconPaddingTop,
                viewMediaPlayVideoIconPaddingBottom = viewMediaPlayVideoIconPaddingBottom,
                viewMediaPlayVideoIconPaddingStart = viewMediaPlayVideoIconPaddingStart,
                viewMediaPlayVideoIconPaddingEnd = viewMediaPlayVideoIconPaddingEnd,
                viewMediaPlayVideoIconWidth = viewMediaPlayVideoIconWidth,
                viewMediaPlayVideoIconHeight = viewMediaPlayVideoIconHeight,
                mediaOverviewPlayVideoButtonIcon = mediaOverviewPlayVideoButtonIcon,
                mediaOverviewPlayVideoIconTint = mediaOverviewPlayVideoIconTint,
                mediaOverviewPlayVideoIconBackgroundColor = mediaOverviewPlayVideoIconBackgroundColor,
                mediaOverviewPlayVideoIconCornerRadius = mediaOverviewPlayVideoIconCornerRadius,
                mediaOverviewPlayVideoIconElevation = mediaOverviewPlayVideoIconElevation,
                mediaOverviewPlayVideoIconPaddingTop = mediaOverviewPlayVideoIconPaddingTop,
                mediaOverviewPlayVideoIconPaddingBottom = mediaOverviewPlayVideoIconPaddingBottom,
                mediaOverviewPlayVideoIconPaddingStart = mediaOverviewPlayVideoIconPaddingStart,
                mediaOverviewPlayVideoIconPaddingEnd = mediaOverviewPlayVideoIconPaddingEnd,
            )
        }
    }
}
