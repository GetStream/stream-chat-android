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

package io.getstream.chat.android.ui.feature.messages.list

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.Px
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.helper.ViewStyle
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat
import io.getstream.chat.android.ui.utils.extensions.use

public data class FileAttachmentViewStyle(
    @ColorInt val backgroundColor: Int,
    @ColorInt val strokeColor: Int,
    @Px val strokeWidth: Int,
    @Px val cornerRadius: Int,
    val progressBarDrawable: Drawable,
    public val actionButtonIcon: Drawable,
    public val failedAttachmentIcon: Drawable,
    val titleTextStyle: TextStyle,
    val fileSizeTextStyle: TextStyle,
) : ViewStyle {
    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): FileAttachmentViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.FileAttachmentView,
                R.attr.streamUiMessageListFileAttachmentStyle,
                R.style.StreamUi_MessageList_FileAttachment,
            ).use { a ->
                val progressBarDrawable =
                    a.getDrawable(R.styleable.FileAttachmentView_streamUiFileAttachmentProgressBarDrawable)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_rotating_indeterminate_progress_gradient)!!

                val backgroundColor = a.getColor(
                    R.styleable.FileAttachmentView_streamUiFileAttachmentBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_white),
                )

                val actionIcon = a.getDrawable(R.styleable.FileAttachmentView_streamUiFileAttachmentActionButton)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_icon_download)!!

                val titleTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.FileAttachmentView_streamUiFileAttachmentTitleTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium),
                    )
                    .color(
                        R.styleable.FileAttachmentView_streamUiFileAttachmentTitleTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary),
                    )
                    .font(
                        R.styleable.FileAttachmentView_streamUiFileAttachmentTitleFontAssets,
                        R.styleable.FileAttachmentView_streamUiFileAttachmentTitleTextFont,
                    )
                    .style(
                        R.styleable.FileAttachmentView_streamUiFileAttachmentTitleTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val fileSizeTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.FileAttachmentView_streamUiFileAttachmentFileSizeTextSize,
                        context.getDimension(R.dimen.stream_ui_text_small),
                    )
                    .color(
                        R.styleable.FileAttachmentView_streamUiFileAttachmentFileSizeTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary),
                    )
                    .font(
                        R.styleable.FileAttachmentView_streamUiFileAttachmentFileSizeFontAssets,
                        R.styleable.FileAttachmentView_streamUiFileAttachmentFileSizeTextFont,
                    )
                    .style(
                        R.styleable.FileAttachmentView_streamUiFileAttachmentFileSizeTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val failedAttachmentIcon =
                    a.getDrawable(R.styleable.FileAttachmentView_streamUiFileAttachmentFailedAttachmentIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_ic_warning)!!

                val strokeColor = a.getColor(
                    R.styleable.FileAttachmentView_streamUiFileAttachmentStrokeColor,
                    context.getColorCompat(R.color.stream_ui_grey_whisper),
                )

                val strokeWidth = a.getDimensionPixelSize(
                    R.styleable.FileAttachmentView_streamUiFileAttachmentStrokeWidth,
                    1.dpToPx(),
                )

                val cornerRadius = a.getDimensionPixelSize(
                    R.styleable.FileAttachmentView_streamUiFileAttachmentCornerRadius,
                    12.dpToPx(),
                )

                return FileAttachmentViewStyle(
                    backgroundColor = backgroundColor,
                    progressBarDrawable = progressBarDrawable,
                    actionButtonIcon = actionIcon,
                    titleTextStyle = titleTextStyle,
                    fileSizeTextStyle = fileSizeTextStyle,
                    failedAttachmentIcon = failedAttachmentIcon,
                    strokeColor = strokeColor,
                    strokeWidth = strokeWidth,
                    cornerRadius = cornerRadius,
                ).let(TransformStyle.fileAttachmentStyleTransformer::transform)
            }
        }
    }
}
