package io.getstream.chat.android.ui.search.list

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.android.ui.message.preview.MessagePreviewStyle

public data class SearchResultListViewStyle(
    public val messagePreviewStyle: MessagePreviewStyle,
) {

    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): SearchResultListViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.SearchResultListView,
                R.attr.streamUiSearchResultListViewStyle,
                0, // TODO
            ).use { typedArray ->

                val senderTextStyle = TextStyle.Builder(typedArray)
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

                val messageTextStyle = TextStyle.Builder(typedArray)
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

                val messageTimeTextStyle = TextStyle.Builder(typedArray)
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
