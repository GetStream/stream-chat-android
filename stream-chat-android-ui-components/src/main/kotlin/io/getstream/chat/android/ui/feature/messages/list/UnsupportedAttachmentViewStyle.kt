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
import io.getstream.chat.android.ui.utils.extensions.use

/**
 * Style for unsupported attachments.
 *
 * @param backgroundColor Unsupported attachment background color.
 * @param strokeColor Unsupported attachment stroke color.
 * @param strokeWidth Unsupported attachment stroke width.
 * @param cornerRadius Unsupported attachment corner radius.
 * @param titleTextStyle Text appearance for unsupported attachment title.
 */
public data class UnsupportedAttachmentViewStyle(
    @ColorInt val backgroundColor: Int,
    @ColorInt val strokeColor: Int,
    @Px val strokeWidth: Int,
    @Px val cornerRadius: Int,
    val titleTextStyle: TextStyle,
) : ViewStyle {
    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): UnsupportedAttachmentViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.UnsupportedAttachmentView,
                R.attr.streamUiMessageListUnsupportedAttachmentStyle,
                R.style.StreamUi_MessageList_UnsupportedAttachment,
            ).use { a ->
                val backgroundColor = a.getColor(
                    R.styleable.UnsupportedAttachmentView_streamUiUnsupportedAttachmentBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_white),
                )

                val strokeColor = a.getColor(
                    R.styleable.UnsupportedAttachmentView_streamUiUnsupportedAttachmentStrokeColor,
                    context.getColorCompat(R.color.stream_ui_grey_whisper),
                )

                val strokeWidth = a.getDimensionPixelSize(
                    R.styleable.UnsupportedAttachmentView_streamUiUnsupportedAttachmentStrokeWidth,
                    1.dpToPx(),
                )

                val cornerRadius = a.getDimensionPixelSize(
                    R.styleable.UnsupportedAttachmentView_streamUiUnsupportedAttachmentCornerRadius,
                    12.dpToPx(),
                )

                val titleTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.UnsupportedAttachmentView_streamUiUnsupportedAttachmentTitleTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium),
                    )
                    .color(
                        R.styleable.UnsupportedAttachmentView_streamUiUnsupportedAttachmentTitleTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary),
                    )
                    .font(
                        R.styleable.UnsupportedAttachmentView_streamUiUnsupportedAttachmentTitleFontAssets,
                        R.styleable.UnsupportedAttachmentView_streamUiUnsupportedAttachmentTitleTextFont,
                    )
                    .style(
                        R.styleable.UnsupportedAttachmentView_streamUiUnsupportedAttachmentTitleTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                return UnsupportedAttachmentViewStyle(
                    backgroundColor = backgroundColor,
                    titleTextStyle = titleTextStyle,
                    strokeColor = strokeColor,
                    strokeWidth = strokeWidth,
                    cornerRadius = cornerRadius,
                ).let(TransformStyle.unsupportedAttachmentStyleTransformer::transform)
            }
        }
    }
}
