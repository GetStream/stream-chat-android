package io.getstream.chat.android.ui.search.list

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.android.ui.message.preview.MessagePreviewStyle

public data class SearchResultListViewStyle(
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
                0, // TODO
            ).use { a ->

                val searchInfoBarBackground =
                    a.getDrawable(R.styleable.SearchResultListView_streamUiSearchResultSearchInfoBarBackground)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_bg_gradient)!!
                val searchInfoBarTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.SearchResultListView_streamUiSearchResultSearchInfoBarTextSize,
                        context.getDimension(R.dimen.stream_ui_text_small)
                    )
                    .color(
                        R.styleable.SearchResultListView_streamUiSearchResultSearchInfoBarTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.SearchResultListView_streamUiSearchResultSearchInfoBarTextFontAssets,
                        R.styleable.SearchResultListView_streamUiSearchResultSearchInfoBarTextFont
                    )
                    .style(
                        R.styleable.SearchResultListView_streamUiSearchResultSearchInfoBarTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val emptyStateIcon = a.getDrawable(R.styleable.SearchResultListView_streamUiSearchResultEmptyStateIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_search_empty)!!
                val emptyStateTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.SearchResultListView_streamUiSearchResultEmptyStateTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.SearchResultListView_streamUiSearchResultEmptyStateTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary)
                    )
                    .font(
                        R.styleable.SearchResultListView_streamUiSearchResultEmptyStateTextFontAssets,
                        R.styleable.SearchResultListView_streamUiSearchResultEmptyStateTextFont
                    )
                    .style(
                        R.styleable.SearchResultListView_streamUiSearchResultEmptyStateTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val progressBarIcon = a.getDrawable(R.styleable.SearchResultListView_streamUiSearchResultProgressBarIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_rotating_indeterminate_progress_gradient)!!

                val senderTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.SearchResultListView_streamUiSearchResultSenderNameTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.SearchResultListView_streamUiSearchResultSenderNameTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.SearchResultListView_streamUiSearchResultSenderNameTextFontAssets,
                        R.styleable.SearchResultListView_streamUiSearchResultSenderNameTextFont
                    )
                    .style(
                        R.styleable.SearchResultListView_streamUiSearchResultSenderNameTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val messageTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.SearchResultListView_streamUiSearchResultMessageTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.SearchResultListView_streamUiSearchResultMessageTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.SearchResultListView_streamUiSearchResultMessageTextFontAssets,
                        R.styleable.SearchResultListView_streamUiSearchResultMessageTextFont
                    )
                    .style(
                        R.styleable.SearchResultListView_streamUiSearchResultMessageTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val messageTimeTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.SearchResultListView_streamUiSearchResultMessageTimeTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.SearchResultListView_streamUiSearchResultMessageTimeTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.SearchResultListView_streamUiSearchResultMessageTimeTextFontAssets,
                        R.styleable.SearchResultListView_streamUiSearchResultMessageTimeTextFont
                    )
                    .style(
                        R.styleable.SearchResultListView_streamUiSearchResultMessageTimeTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                return SearchResultListViewStyle(
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
                )
            }
        }
    }
}
