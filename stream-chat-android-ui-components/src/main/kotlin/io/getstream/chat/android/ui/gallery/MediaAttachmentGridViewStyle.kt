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

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getColorOrNull
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.gallery.overview.MediaAttachmentGridView

/**
 * Controls the appearance of [MediaAttachmentGridView].
 *
 * @param showUserAvatars Controls whether the avatar of the user who had sent the attachment is displayed
 * over the attachment preview or not.
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
 **/
public data class MediaAttachmentGridViewStyle(
    val showUserAvatars: Boolean,
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

        operator fun invoke(context: Context, attrs: AttributeSet?): MediaAttachmentGridViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.MediaAttachmentGridView,
                R.attr.streamUiMediaAttachmentGridViewStyle,
                R.style.StreamUi_MediaAttachmentGridView
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
                    true
                )

            val mediaOverviewPlayVideoButtonIcon = it.getDrawable(
                R.styleable.MediaAttachmentGridView_streamUiMediaAttachmentGridViewPlayVideoButtonIcon
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_play)

            val mediaOverviewPlayVideoIconTint = it.getColorOrNull(
                R.styleable.MediaAttachmentGridView_streamUiMediaAttachmentGridViewPlayVideoIconTint
            )

            val mediaOverviewPlayVideoIconBackgroundColor =
                it.getColor(
                    R.styleable.MediaAttachmentGridView_streamUiMediaAttachmentGridViewPlayVideoIconBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_literal_white)
                )

            val mediaOverviewPlayVideoIconCornerRadius =
                it.getDimension(
                    R.styleable.MediaAttachmentGridView_streamUiMediaAttachmentGridViewPlayVideoIconCornerRadius,
                    0f
                )

            val mediaOverviewPlayVideoIconElevation =
                it.getDimension(
                    R.styleable.MediaAttachmentGridView_streamUiMediaAttachmentGridViewPlayVideoIconElevation,
                    0f
                )

            val mediaOverviewPlayVideoIconPaddingTop =
                it.getDimensionPixelSize(
                    R.styleable.MediaAttachmentGridView_streamUiMediaAttachmentGridViewPlayVideoIconPaddingTop,
                    0
                )

            val mediaOverviewPlayVideoIconPaddingBottom =
                it.getDimensionPixelSize(
                    R.styleable.MediaAttachmentGridView_streamUiMediaAttachmentGridViewPlayVideoIconPaddingBottom,
                    0
                )

            val mediaOverviewPlayVideoIconPaddingStart =
                it.getDimensionPixelSize(
                    R.styleable.MediaAttachmentGridView_streamUiMediaAttachmentGridViewPlayVideoIconPaddingStart,
                    0
                )

            val mediaOverviewPlayVideoIconPaddingEnd =
                it.getDimensionPixelSize(
                    R.styleable.MediaAttachmentGridView_streamUiMediaAttachmentGridViewPlayVideoIconPaddingEnd,
                    0
                )

            return MediaAttachmentGridViewStyle(
                showUserAvatars = showUserAvatars,
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
