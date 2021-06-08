package io.getstream.chat.android.ui.message.list

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.content.res.ResourcesCompat
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder

/**
 * Style for [GiphyViewHolder].
 * Use this class together with [TransformStyle.giphyViewHolderStyleTransformer] to change styles programmatically.
 *
 * @param cardBackgroundColor - card's background color. Default - [R.color.stream_ui_white]
 * @param cardElevation - card's elevation. Default - [R.dimen.stream_ui_elevation_small]
 * @param cardButtonDividerColor - color for dividers placed between action buttons. Default - [R.color.stream_ui_border]
 * @param giphyIcon - Giphy icon. Default - [R.drawable.stream_ui_ic_giphy]
 * @param labelTextStyle - appearance for label text
 * @param queryTextStyle - appearance for query text
 * @param cancelButtonTextStyle - appearance for cancel button text
 * @param shuffleButtonTextStyle - appearance for shuffle button text
 * @param sendButtonTextStyle - appearance for send button text
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
) {

    internal companion object {
        operator fun invoke(context: Context, attributes: TypedArray): GiphyViewHolderStyle {
            val boldTypeface = ResourcesCompat.getFont(context, R.font.roboto_bold) ?: Typeface.DEFAULT_BOLD
            val mediumTypeface = ResourcesCompat.getFont(context, R.font.roboto_medium) ?: Typeface.DEFAULT

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
