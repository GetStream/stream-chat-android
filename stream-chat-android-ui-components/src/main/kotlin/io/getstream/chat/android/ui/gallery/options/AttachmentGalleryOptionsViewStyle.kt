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

package io.getstream.chat.android.ui.gallery.options

import android.app.ActionBar.LayoutParams
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getColorOrNull
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.android.ui.gallery.options.internal.AttachmentGalleryOptionsView

/**
 * Style for [AttachmentGalleryOptionsView].
 *
 * @param optionTextStyle The text style of each option.
 * @param backgroundColor The background color of the options dialog.
 * @param replyOptionEnabled If the "reply" option is present in the list.
 * @param replyOptionDrawable  The icon to the "reply" option.
 * @param showInChatOptionEnabled If the "show in chat" option present in the list.
 * @param showInChatOptionDrawable The icon for the "show in chat" option.
 * @param saveMediaOptionEnabled If the "save media" option is present in the list.
 * @param saveMediaOptionDrawable The icon for the "save media" option.
 * @param deleteOptionEnabled If the "delete" option is present in the list.
 * @param deleteOptionDrawable The icon for the "delete" option.
 * @param deleteOptionTextColor The text color of the "delete" option.
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
 */
public data class AttachmentGalleryOptionsViewStyle(
    val optionTextStyle: TextStyle,
    @ColorInt val backgroundColor: Int,
    val replyOptionEnabled: Boolean,
    val replyOptionDrawable: Drawable,
    val showInChatOptionEnabled: Boolean,
    val showInChatOptionDrawable: Drawable,
    val saveMediaOptionEnabled: Boolean,
    // TODO - see with the team if we want to keep using
    // TODO - the terminology icon or drawable for this.
    val saveMediaOptionDrawable: Drawable,
    val deleteOptionEnabled: Boolean,
    val deleteOptionDrawable: Drawable,
    @ColorInt val deleteOptionTextColor: Int,
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
) {

    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): AttachmentGalleryOptionsViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.AttachmentOptionsView,
                R.attr.streamUiAttachmentGalleryOptionsStyle,
                R.style.StreamUi_AttachmentGallery_Options
            ).use {
                return AttachmentGalleryOptionsViewStyle(context, it)
            }
        }

        operator fun invoke(context: Context, it: TypedArray): AttachmentGalleryOptionsViewStyle {
            val optionTextStyle = TextStyle.Builder(it)
                .size(
                    R.styleable.AttachmentOptionsView_streamUiAttachmentOptionTextSize,
                    context.getDimension(R.dimen.stream_ui_text_medium)
                )
                .color(
                    R.styleable.AttachmentOptionsView_streamUiAttachmentOptionTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_primary)
                )
                .font(
                    R.styleable.AttachmentOptionsView_streamUiAttachmentOptionTextFontAssets,
                    R.styleable.AttachmentOptionsView_streamUiAttachmentOptionTextFont,
                    ResourcesCompat.getFont(context, R.font.stream_roboto_medium) ?: Typeface.DEFAULT
                )
                .style(
                    R.styleable.AttachmentOptionsView_streamUiAttachmentOptionTextStyle,
                    Typeface.NORMAL
                )
                .build()

            val backgroundColor = it.getColor(
                R.styleable.AttachmentOptionsView_streamUiAttachmentOptionsBackgroundColor,
                context.getColorCompat(R.color.stream_ui_white_snow)
            )

            val replyOptionEnabled = it.getBoolean(
                R.styleable.AttachmentOptionsView_streamUiAttachmentReplyEnabled,
                true
            )

            val replyOptionDrawable = it.getDrawable(
                R.styleable.AttachmentOptionsView_streamUiReplyIcon
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_arrow_curve_left_grey)!!

            val showInChatOptionEnabled = it.getBoolean(
                R.styleable.AttachmentOptionsView_streamUiShowInChatEnabled,
                true
            )

            val showInChatOptionDrawable = it.getDrawable(
                R.styleable.AttachmentOptionsView_streamUiShowInChatIcon
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_show_in_chat)!!

            val saveMediaOptionEnabled = it.getBoolean(
                R.styleable.AttachmentOptionsView_streamUiSaveMediaEnabled,
                true
            )

            val saveMediaOptionDrawable = it.getDrawable(
                R.styleable.AttachmentOptionsView_streamUiSaveMediaIcon,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_download)!!

            val deleteOptionEnabled = it.getBoolean(
                R.styleable.AttachmentOptionsView_streamUiDeleteEnabled,
                true
            )

            val deleteOptionDrawable = it.getDrawable(
                R.styleable.AttachmentOptionsView_streamUiDeleteIcon,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_delete)!!

            val deleteOptionTextColor = it.getColor(
                R.styleable.AttachmentOptionsView_streamUiDeleteTextTint,
                context.getColorCompat(R.color.stream_ui_accent_red)
            )

            val viewMediaPlayVideoButtonIcon = it.getDrawable(
                R.styleable.AttachmentOptionsView_streamUiAttachmentGalleryViewMediaPlayVideoButtonIcon
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_play)

            val viewMediaPlayVideoIconTint = it.getColorOrNull(
                R.styleable.AttachmentOptionsView_streamUiAttachmentGalleryViewMediaPlayVideoIconTint
            )

            val viewMediaPlayVideoIconBackgroundColor =
                it.getColor(
                    R.styleable.AttachmentOptionsView_streamUiAttachmentGalleryViewMediaPlayVideoIconBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_literal_white)
                )

            val viewMediaPlayVideoIconCornerRadius =
                it.getDimension(
                    R.styleable.AttachmentOptionsView_streamUiAttachmentGalleryViewMediaPlayVideoIconCornerRadius,
                    0f
                )

            val viewMediaPlayVideoIconElevation =
                it.getDimension(
                    R.styleable.AttachmentOptionsView_streamUiAttachmentGalleryViewMediaPlayVideoIconElevation,
                    0f
                )

            val viewMediaPlayVideoIconPaddingTop =
                it.getDimensionPixelSize(
                    R.styleable.AttachmentOptionsView_streamUiAttachmentGalleryViewMediaPlayVideoIconPaddingTop,
                    0
                )

            val viewMediaPlayVideoIconPaddingBottom =
                it.getDimensionPixelSize(
                    R.styleable.AttachmentOptionsView_streamUiAttachmentGalleryViewMediaPlayVideoIconPaddingBottom,
                    0
                )

            val viewMediaPlayVideoIconPaddingStart =
                it.getDimensionPixelSize(
                    R.styleable.AttachmentOptionsView_streamUiAttachmentGalleryViewMediaPlayVideoIconPaddingStart,
                    0
                )

            val viewMediaPlayVideoIconPaddingEnd =
                it.getDimensionPixelSize(
                    R.styleable.AttachmentOptionsView_streamUiAttachmentGalleryViewMediaPlayVideoIconPaddingEnd,
                    0
                )

            val viewMediaPlayVideoIconWidth =
                it.getLayoutDimension(
                    R.styleable.AttachmentOptionsView_streamUiAttachmentGalleryViewMediaPlayIconWidth,
                    LayoutParams.WRAP_CONTENT
                )

            val viewMediaPlayVideoIconHeight =
                it.getLayoutDimension(
                    R.styleable.AttachmentOptionsView_streamUiAttachmentGalleryViewMediaPlayIconHeight,
                    LayoutParams.WRAP_CONTENT
                )

            return AttachmentGalleryOptionsViewStyle(
                optionTextStyle = optionTextStyle,
                backgroundColor = backgroundColor,
                replyOptionEnabled = replyOptionEnabled,
                replyOptionDrawable = replyOptionDrawable,
                showInChatOptionEnabled = showInChatOptionEnabled,
                showInChatOptionDrawable = showInChatOptionDrawable,
                saveMediaOptionEnabled = saveMediaOptionEnabled,
                saveMediaOptionDrawable = saveMediaOptionDrawable,
                deleteOptionEnabled = deleteOptionEnabled,
                deleteOptionDrawable = deleteOptionDrawable,
                deleteOptionTextColor = deleteOptionTextColor,
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
                viewMediaPlayVideoIconHeight = viewMediaPlayVideoIconHeight
            ).let(TransformStyle.attachmentGalleryOptionsStyleTransformer::transform)
        }
    }
}
