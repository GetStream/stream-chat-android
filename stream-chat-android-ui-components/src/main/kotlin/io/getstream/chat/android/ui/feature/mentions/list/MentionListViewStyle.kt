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

package io.getstream.chat.android.ui.feature.mentions.list

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.feature.messages.preview.MessagePreviewStyle
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.helper.ViewStyle
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat
import io.getstream.chat.android.ui.utils.extensions.use

public data class MentionListViewStyle(
    @ColorInt public val backgroundColor: Int,
    public val emptyStateDrawable: Drawable,
    public val messagePreviewStyle: MessagePreviewStyle,
) : ViewStyle {

    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): MentionListViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.MentionListView,
                R.attr.streamUiMentionListStyle,
                R.style.StreamUi_MentionList,
            ).use { typedArray ->
                val backgroundColor = typedArray.getColor(
                    R.styleable.MentionListView_streamUiBackground,
                    context.getColorCompat(R.color.stream_ui_white_snow),
                )

                val emptyStateDrawable = typedArray.getDrawable(
                    R.styleable.MentionListView_streamUiEmptyStateDrawable,
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_mentions_empty)!!

                val senderTextStyle = TextStyle.Builder(typedArray)
                    .size(
                        R.styleable.MentionListView_streamUiSenderNameTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium),
                    )
                    .color(
                        R.styleable.MentionListView_streamUiSenderNameTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary),
                    )
                    .font(
                        R.styleable.MentionListView_streamUiSenderNameTextFontAssets,
                        R.styleable.MentionListView_streamUiSenderNameTextFont,
                    )
                    .style(
                        R.styleable.MentionListView_streamUiSenderNameTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val messageTextStyle = TextStyle.Builder(typedArray)
                    .size(
                        R.styleable.MentionListView_streamUiMessageTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium),
                    )
                    .color(
                        R.styleable.MentionListView_streamUiMessageTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary),
                    )
                    .font(
                        R.styleable.MentionListView_streamUiMessageTextFontAssets,
                        R.styleable.MentionListView_streamUiMessageTextFont,
                    )
                    .style(
                        R.styleable.MentionListView_streamUiMessageTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val messageTimeTextStyle = TextStyle.Builder(typedArray)
                    .size(
                        R.styleable.MentionListView_streamUiMessageTimeTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium),
                    )
                    .color(
                        R.styleable.MentionListView_streamUiMessageTimeTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary),
                    )
                    .font(
                        R.styleable.MentionListView_streamUiMessageTimeTextFontAssets,
                        R.styleable.MentionListView_streamUiMessageTimeTextFont,
                    )
                    .style(
                        R.styleable.MentionListView_streamUiMessageTimeTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                return MentionListViewStyle(
                    backgroundColor = backgroundColor,
                    emptyStateDrawable = emptyStateDrawable,
                    messagePreviewStyle = MessagePreviewStyle(
                        messageSenderTextStyle = senderTextStyle,
                        messageTextStyle = messageTextStyle,
                        messageTimeTextStyle = messageTimeTextStyle,
                    ),
                ).let(TransformStyle.mentionListViewStyleTransformer::transform)
            }
        }
    }
}
