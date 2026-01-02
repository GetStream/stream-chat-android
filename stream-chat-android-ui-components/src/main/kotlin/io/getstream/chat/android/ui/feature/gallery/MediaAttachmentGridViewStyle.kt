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

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.feature.gallery.overview.MediaAttachmentGridView
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.helper.ViewStyle
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getColorOrNull
import io.getstream.chat.android.ui.utils.extensions.getDimensionOrNull
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat

/**
 * Controls the appearance of [MediaAttachmentGridView].
 *
 * @param showUserAvatars Controls whether the avatar of the user who had sent the attachment is displayed
 * over the attachment preview or not.
 * @param playVideoButtonIcon The drawable for the play button icon overlaid above video attachments in
 * the media overview segment of the gallery.
 * @param playVideoIconTint Tints the icon overlaid on top of video attachment previews in the media
 * overview segment of the gallery.
 * @param playVideoIconBackgroundColor The background color of the View containing the play button in
 * the media overview segment of the gallery.
 * @param playVideoIconCornerRadius The corner radius of the play button in the media
 * overview segment of the gallery..
 * @param playVideoIconElevation The elevation of the play button in the media
 * overview segment of the gallery
 * @param playVideoIconPaddingTop Sets the top padding of the play video icon in the media
 * overview segment of the gallery.
 * @param playVideoIconPaddingBottom Sets the bottom padding of the play video icon in the media
 * overview segment of the gallery.
 * @param playVideoIconPaddingStart Sets the start padding of the play video icon in the media
 * overview segment of the gallery.
 * @param playVideoIconPaddingEnd  Sets the end padding of the play video icon in the media
 * overview segment of the gallery.
 * @param imagePlaceholder A placeholder drawable used before the image is loaded.
 */
public data class MediaAttachmentGridViewStyle(
    val showUserAvatars: Boolean,
    val playVideoButtonIcon: Drawable?,
    @ColorInt val playVideoIconTint: Int?,
    @ColorInt val playVideoIconBackgroundColor: Int,
    val playVideoIconCornerRadius: Float,
    val playVideoIconElevation: Float,
    val playVideoIconPaddingTop: Int,
    val playVideoIconPaddingBottom: Int,
    val playVideoIconPaddingStart: Int,
    val playVideoIconPaddingEnd: Int,
    val imagePlaceholder: Drawable?,
) : ViewStyle {
    internal companion object {

        operator fun invoke(context: Context, attrs: AttributeSet?): MediaAttachmentGridViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.MediaAttachmentGridView,
                R.attr.streamUiMediaAttachmentGridViewStyle,
                R.style.StreamUi_MediaAttachmentGridView,
            ).let { styledAttributes ->
                val style = MediaAttachmentGridViewStyle(context, styledAttributes)
                styledAttributes.recycle()
                return style
            }
        }

        operator fun invoke(context: Context, it: TypedArray): MediaAttachmentGridViewStyle {
            val showUserAvatars =
                it.getBoolean(
                    R.styleable.MediaAttachmentGridView_streamUiMediaAttachmentGridViewShowUserAvatars,
                    true,
                )

            val playVideoButtonIcon = it.getDrawable(
                R.styleable.MediaAttachmentGridView_streamUiMediaAttachmentGridViewPlayVideoButtonIcon,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_play)

            val playVideoIconTint = it.getColorOrNull(
                R.styleable.MediaAttachmentGridView_streamUiMediaAttachmentGridViewPlayVideoIconTint,
            )

            val playVideoIconBackgroundColor =
                it.getColor(
                    R.styleable.MediaAttachmentGridView_streamUiMediaAttachmentGridViewPlayVideoIconBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_literal_white),
                )

            val playVideoIconCornerRadius =
                it.getDimension(
                    R.styleable.MediaAttachmentGridView_streamUiMediaAttachmentGridViewPlayVideoIconCornerRadius,
                    0f,
                )

            val playVideoIconElevation =
                it.getDimension(
                    R.styleable.MediaAttachmentGridView_streamUiMediaAttachmentGridViewPlayVideoIconElevation,
                    0f,
                )

            val playVideoIconPaddingTop =
                it.getDimensionPixelSize(
                    R.styleable.MediaAttachmentGridView_streamUiMediaAttachmentGridViewPlayVideoIconPaddingTop,
                    0,
                )

            val playVideoIconPaddingBottom =
                it.getDimensionPixelSize(
                    R.styleable.MediaAttachmentGridView_streamUiMediaAttachmentGridViewPlayVideoIconPaddingBottom,
                    0,
                )

            val playVideoIconPaddingStart =
                it.getDimensionPixelSize(
                    R.styleable.MediaAttachmentGridView_streamUiMediaAttachmentGridViewPlayVideoIconPaddingStart,
                    0,
                )

            val playVideoIconPaddingEnd =
                it.getDimensionPixelSize(
                    R.styleable.MediaAttachmentGridView_streamUiMediaAttachmentGridViewPlayVideoIconPaddingEnd,
                    0,
                )

            val playVideoIconPadding =
                it.getDimensionOrNull(
                    R.styleable.MediaAttachmentGridView_streamUiMediaAttachmentGridViewPlayVideoIconPadding,
                )?.toInt()

            val imagePlaceholder = it.getDrawable(
                R.styleable.MediaAttachmentGridView_streamUiMediaAttachmentGridViewImagePlaceholder,
            ) ?: ContextCompat.getDrawable(
                context,
                R.drawable.stream_ui_picture_placeholder,
            )

            return MediaAttachmentGridViewStyle(
                showUserAvatars = showUserAvatars,
                playVideoButtonIcon = playVideoButtonIcon,
                playVideoIconTint = playVideoIconTint,
                playVideoIconBackgroundColor = playVideoIconBackgroundColor,
                playVideoIconCornerRadius = playVideoIconCornerRadius,
                playVideoIconElevation = playVideoIconElevation,
                playVideoIconPaddingTop = playVideoIconPadding ?: playVideoIconPaddingTop,
                playVideoIconPaddingBottom = playVideoIconPadding ?: playVideoIconPaddingBottom,
                playVideoIconPaddingStart = playVideoIconPadding ?: playVideoIconPaddingStart,
                playVideoIconPaddingEnd = playVideoIconPadding ?: playVideoIconPaddingEnd,
                imagePlaceholder = imagePlaceholder,
            ).let(TransformStyle.mediaAttachmentGridViewStyle::transform)
        }
    }
}
