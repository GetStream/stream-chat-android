package io.getstream.chat.android.ui.search.list

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.android.ui.message.preview.MessagePreviewStyle

/**
 * Style for [SearchResultListView].
 * Use this class together with [TransformStyle.searchResultListViewStyleTransformer] to change [SearchResultListView] styles programmatically.
 *
 * @property searchInfoBarBackground - background for search info bar. Default - [R.drawable.stream_ui_bg_gradient]
 * @property searchInfoBarTextStyle - appearance for text displayed in search info bar
 * @property emptyStateIcon - icon for empty state view. Default - [R.drawable.stream_ui_ic_search_empty]
 * @property emptyStateTextStyle - appearance for empty state text
 * @property progressBarIcon - animated progress drawable. Default - [R.drawable.stream_ui_rotating_indeterminate_progress_gradient]
 * @property messagePreviewStyle - style for single search result item
 */
public data class SearchResultListViewStyle(
    @ColorInt public val backgroundColor: Int,
    public val searchInfoBarBackground: Drawable,
    public val searchInfoBarTextStyle: TextStyle,
    public val emptyStateIcon: Drawable,
    public val emptyStateTextStyle: TextStyle,
    public val progressBarIcon: Drawable,
    public val messagePreviewStyle: MessagePreviewStyle,
) {
    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): SearchResultListViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.SearchResultListView,
                R.attr.streamUiSearchResultListViewStyle,
                R.style.StreamUi_SearchResultListView,
            ).use { a ->
                val backgroundColor =
                    a.getColor(
                        R.styleable.SearchResultListView_streamUiSearchResultListBackground,
                        context.getColorCompat(R.color.stream_ui_white)
                    )

                val searchInfoBarBackground =
                    a.getDrawable(R.styleable.SearchResultListView_streamUiSearchResultListSearchInfoBarBackground)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_bg_gradient)!!
                val searchInfoBarTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.SearchResultListView_streamUiSearchResultListSearchInfoBarTextSize,
                        context.getDimension(R.dimen.stream_ui_text_small)
                    )
                    .color(
                        R.styleable.SearchResultListView_streamUiSearchResultListSearchInfoBarTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.SearchResultListView_streamUiSearchResultListSearchInfoBarTextFontAssets,
                        R.styleable.SearchResultListView_streamUiSearchResultListSearchInfoBarTextFont
                    )
                    .style(
                        R.styleable.SearchResultListView_streamUiSearchResultListSearchInfoBarTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val emptyStateIcon =
                    a.getDrawable(R.styleable.SearchResultListView_streamUiSearchResultListEmptyStateIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_ic_search_empty)!!
                val emptyStateTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.SearchResultListView_streamUiSearchResultListEmptyStateTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.SearchResultListView_streamUiSearchResultListEmptyStateTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary)
                    )
                    .font(
                        R.styleable.SearchResultListView_streamUiSearchResultListEmptyStateTextFontAssets,
                        R.styleable.SearchResultListView_streamUiSearchResultListEmptyStateTextFont
                    )
                    .style(
                        R.styleable.SearchResultListView_streamUiSearchResultListEmptyStateTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val progressBarIcon =
                    a.getDrawable(R.styleable.SearchResultListView_streamUiSearchResultListProgressBarIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_rotating_indeterminate_progress_gradient)!!

                val senderTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.SearchResultListView_streamUiSearchResultListSenderNameTextSize,
                        context.getDimension(R.dimen.stream_ui_channel_item_title)
                    )
                    .color(
                        R.styleable.SearchResultListView_streamUiSearchResultListSenderNameTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.SearchResultListView_streamUiSearchResultListSenderNameTextFontAssets,
                        R.styleable.SearchResultListView_streamUiSearchResultListSenderNameTextFont
                    )
                    .style(
                        R.styleable.SearchResultListView_streamUiSearchResultListSenderNameTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val messageTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.SearchResultListView_streamUiSearchResultListMessageTextSize,
                        context.getDimension(R.dimen.stream_ui_channel_item_message)
                    )
                    .color(
                        R.styleable.SearchResultListView_streamUiSearchResultListMessageTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary)
                    )
                    .font(
                        R.styleable.SearchResultListView_streamUiSearchResultListMessageTextFontAssets,
                        R.styleable.SearchResultListView_streamUiSearchResultListMessageTextFont
                    )
                    .style(
                        R.styleable.SearchResultListView_streamUiSearchResultListMessageTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val messageTimeTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.SearchResultListView_streamUiSearchResultListMessageTimeTextSize,
                        context.getDimension(R.dimen.stream_ui_channel_item_message)
                    )
                    .color(
                        R.styleable.SearchResultListView_streamUiSearchResultListMessageTimeTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary)
                    )
                    .font(
                        R.styleable.SearchResultListView_streamUiSearchResultListMessageTimeTextFontAssets,
                        R.styleable.SearchResultListView_streamUiSearchResultListMessageTimeTextFont
                    )
                    .style(
                        R.styleable.SearchResultListView_streamUiSearchResultListMessageTimeTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                return SearchResultListViewStyle(
                    backgroundColor = backgroundColor,
                    searchInfoBarBackground = searchInfoBarBackground,
                    searchInfoBarTextStyle = searchInfoBarTextStyle,
                    emptyStateIcon = emptyStateIcon,
                    emptyStateTextStyle = emptyStateTextStyle,
                    progressBarIcon = progressBarIcon,
                    messagePreviewStyle = MessagePreviewStyle(
                        messageSenderTextStyle = senderTextStyle,
                        messageTextStyle = messageTextStyle,
                        messageTimeTextStyle = messageTimeTextStyle,
                    ),
                ).let(TransformStyle.searchResultListViewStyleTransformer::transform)
            }
        }
    }
}
