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

package io.getstream.chat.android.ui.feature.gallery.options

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.feature.gallery.AttachmentGalleryActivity
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.helper.ViewStyle
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat
import io.getstream.chat.android.ui.utils.extensions.use

/**
 * Controls how video attachments are displayed inside of the [AttachmentGalleryActivity].
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
 */
public data class AttachmentGalleryOptionsViewStyle(
    val optionTextStyle: TextStyle,
    @ColorInt val backgroundColor: Int,
    val replyOptionEnabled: Boolean,
    val replyOptionDrawable: Drawable,
    val showInChatOptionEnabled: Boolean,
    val showInChatOptionDrawable: Drawable,
    val saveMediaOptionEnabled: Boolean,
    val saveMediaOptionDrawable: Drawable,
    val deleteOptionEnabled: Boolean,
    val deleteOptionDrawable: Drawable,
    @ColorInt val deleteOptionTextColor: Int,
) : ViewStyle {

    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): AttachmentGalleryOptionsViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.AttachmentOptionsView,
                R.attr.streamUiAttachmentGalleryOptionsStyle,
                R.style.StreamUi_AttachmentGallery_Options,
            ).use {
                return AttachmentGalleryOptionsViewStyle(context, it)
            }
        }

        operator fun invoke(context: Context, it: TypedArray): AttachmentGalleryOptionsViewStyle {
            val optionTextStyle = TextStyle.Builder(it)
                .size(
                    R.styleable.AttachmentOptionsView_streamUiAttachmentOptionTextSize,
                    context.getDimension(R.dimen.stream_ui_text_medium),
                )
                .color(
                    R.styleable.AttachmentOptionsView_streamUiAttachmentOptionTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_primary),
                )
                .font(
                    R.styleable.AttachmentOptionsView_streamUiAttachmentOptionTextFontAssets,
                    R.styleable.AttachmentOptionsView_streamUiAttachmentOptionTextFont,
                    ResourcesCompat.getFont(context, R.font.stream_roboto_medium) ?: Typeface.DEFAULT,
                )
                .style(
                    R.styleable.AttachmentOptionsView_streamUiAttachmentOptionTextStyle,
                    Typeface.NORMAL,
                )
                .build()

            val backgroundColor = it.getColor(
                R.styleable.AttachmentOptionsView_streamUiAttachmentOptionsBackgroundColor,
                context.getColorCompat(R.color.stream_ui_white_snow),
            )

            val replyOptionEnabled = it.getBoolean(
                R.styleable.AttachmentOptionsView_streamUiAttachmentReplyEnabled,
                true,
            )

            val replyOptionDrawable = it.getDrawable(
                R.styleable.AttachmentOptionsView_streamUiReplyIcon,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_arrow_curve_left_grey)!!

            val showInChatOptionEnabled = it.getBoolean(
                R.styleable.AttachmentOptionsView_streamUiShowInChatEnabled,
                true,
            )

            val showInChatOptionDrawable = it.getDrawable(
                R.styleable.AttachmentOptionsView_streamUiShowInChatIcon,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_show_in_chat)!!

            val saveMediaOptionEnabled = it.getBoolean(
                R.styleable.AttachmentOptionsView_streamUiSaveMediaEnabled,
                true,
            )

            val saveMediaOptionDrawable = it.getDrawable(
                R.styleable.AttachmentOptionsView_streamUiSaveMediaIcon,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_download)!!

            val deleteOptionEnabled = it.getBoolean(
                R.styleable.AttachmentOptionsView_streamUiDeleteEnabled,
                true,
            )

            val deleteOptionDrawable = it.getDrawable(
                R.styleable.AttachmentOptionsView_streamUiDeleteIcon,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_delete)!!

            val deleteOptionTextColor = it.getColor(
                R.styleable.AttachmentOptionsView_streamUiDeleteTextTint,
                context.getColorCompat(R.color.stream_ui_accent_red),
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
            ).let(TransformStyle.attachmentGalleryOptionsStyleTransformer::transform)
        }
    }
}
