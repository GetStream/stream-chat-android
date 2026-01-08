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
import android.content.res.TypedArray
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.content.res.ResourcesCompat
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.GiphyViewHolder
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.helper.ViewStyle
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat

/**
 * Style for [GiphyViewHolder].
 * Use this class together with [TransformStyle.giphyViewHolderStyleTransformer] to change styles programmatically.
 *
 * @param cardBackgroundColor Card's background color. Default value is [R.color.stream_ui_white].
 * @param cardElevation Card's elevation. Default value is [R.dimen.stream_ui_elevation_small].
 * @param cardButtonDividerColor Color for dividers placed between action buttons. Default value is [R.color.stream_ui_border].
 * @param giphyIcon Giphy icon. Default value is [R.drawable.stream_ui_ic_giphy].
 * @param labelTextStyle Appearance for label text.
 * @param queryTextStyle Appearance for query text.
 * @param cancelButtonTextStyle Appearance for cancel button text.
 * @param shuffleButtonTextStyle Appearance for shuffle button text.
 * @param sendButtonTextStyle Appearance for send button text.
 */
public data class GiphyViewHolderStyle(
    @ColorInt val cardBackgroundColor: Int,
    @Px val cardElevation: Float,
    @ColorInt val cardButtonDividerColor: Int,
    val giphyIcon: Drawable,
    val labelTextStyle: TextStyle,
    val queryTextStyle: TextStyle,
    val cancelButtonTextStyle: TextStyle,
    val shuffleButtonTextStyle: TextStyle,
    val sendButtonTextStyle: TextStyle,
) : ViewStyle {

    internal companion object {
        operator fun invoke(context: Context, attributes: TypedArray): GiphyViewHolderStyle {
            val boldTypeface = ResourcesCompat.getFont(context, R.font.stream_roboto_bold) ?: Typeface.DEFAULT_BOLD
            val mediumTypeface = ResourcesCompat.getFont(context, R.font.stream_roboto_medium) ?: Typeface.DEFAULT

            val cardBackgroundColor = attributes.getColor(
                R.styleable.MessageListView_streamUiGiphyCardBackgroundColor,
                context.getColorCompat(R.color.stream_ui_white),
            )
            val cardElevation = attributes.getDimension(
                R.styleable.MessageListView_streamUiGiphyCardElevation,
                context.getDimension(R.dimen.stream_ui_elevation_small).toFloat(),
            )

            val cardButtonDividerColor = attributes.getColor(
                R.styleable.MessageListView_streamUiGiphyCardButtonDividerColor,
                context.getColorCompat(R.color.stream_ui_border),
            )

            val giphyIcon = attributes.getDrawable(R.styleable.MessageListView_streamUiGiphyIcon)
                ?: context.getDrawableCompat(R.drawable.stream_ui_ic_giphy)!!

            val labelTextStyle = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiGiphyLabelTextSize,
                    context.getDimension(R.dimen.stream_ui_text_medium),
                )
                .color(
                    R.styleable.MessageListView_streamUiGiphyLabelTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_primary),
                )
                .font(
                    R.styleable.MessageListView_streamUiGiphyLabelTextFontAssets,
                    R.styleable.MessageListView_streamUiGiphyLabelTextFont,
                    boldTypeface,
                )
                .style(
                    R.styleable.MessageListView_streamUiGiphyLabelTextStyle,
                    Typeface.NORMAL,
                )
                .build()

            val queryTextStyle = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiGiphyQueryTextSize,
                    context.getDimension(R.dimen.stream_ui_text_medium),
                )
                .color(
                    R.styleable.MessageListView_streamUiGiphyQueryTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_primary),
                )
                .font(
                    R.styleable.MessageListView_streamUiGiphyQueryTextFontAssets,
                    R.styleable.MessageListView_streamUiGiphyQueryTextFont,
                    mediumTypeface,
                )
                .style(
                    R.styleable.MessageListView_streamUiGiphyQueryTextStyle,
                    Typeface.NORMAL,
                )
                .build()

            val cancelButtonTextStyle = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiGiphyCancelButtonTextSize,
                    context.getDimension(R.dimen.stream_ui_text_medium),
                )
                .color(
                    R.styleable.MessageListView_streamUiGiphyCancelButtonTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_secondary),
                )
                .font(
                    R.styleable.MessageListView_streamUiGiphyCancelButtonTextFontAssets,
                    R.styleable.MessageListView_streamUiGiphyCancelButtonTextFont,
                    boldTypeface,
                )
                .style(
                    R.styleable.MessageListView_streamUiGiphyCancelButtonTextStyle,
                    Typeface.NORMAL,
                )
                .build()

            val shuffleButtonTextStyle = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiGiphyShuffleButtonTextSize,
                    context.getDimension(R.dimen.stream_ui_text_medium),
                )
                .color(
                    R.styleable.MessageListView_streamUiGiphyShuffleButtonTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_secondary),
                )
                .font(
                    R.styleable.MessageListView_streamUiGiphyShuffleButtonTextFontAssets,
                    R.styleable.MessageListView_streamUiGiphyShuffleButtonTextFont,
                    boldTypeface,
                )
                .style(
                    R.styleable.MessageListView_streamUiGiphyShuffleButtonTextStyle,
                    Typeface.NORMAL,
                )
                .build()

            val sendButtonTextStyle = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiGiphySendButtonTextSize,
                    context.getDimension(R.dimen.stream_ui_text_medium),
                )
                .color(
                    R.styleable.MessageListView_streamUiGiphySendButtonTextColor,
                    context.getColorCompat(R.color.stream_ui_accent_blue),
                )
                .font(
                    R.styleable.MessageListView_streamUiGiphySendButtonTextFontAssets,
                    R.styleable.MessageListView_streamUiGiphySendButtonTextFont,
                    boldTypeface,
                )
                .style(
                    R.styleable.MessageListView_streamUiGiphySendButtonTextStyle,
                    Typeface.NORMAL,
                )
                .build()

            return GiphyViewHolderStyle(
                cardBackgroundColor = cardBackgroundColor,
                cardElevation = cardElevation,
                cardButtonDividerColor = cardButtonDividerColor,
                giphyIcon = giphyIcon,
                labelTextStyle = labelTextStyle,
                queryTextStyle = queryTextStyle,
                cancelButtonTextStyle = cancelButtonTextStyle,
                shuffleButtonTextStyle = shuffleButtonTextStyle,
                sendButtonTextStyle = sendButtonTextStyle,
            ).let(TransformStyle.giphyViewHolderStyleTransformer::transform)
        }
    }
}
