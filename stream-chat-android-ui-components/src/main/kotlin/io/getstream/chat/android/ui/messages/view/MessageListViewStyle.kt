package io.getstream.chat.android.ui.messages.view

import android.content.Context
import android.util.AttributeSet
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat
import io.getstream.chat.android.ui.utils.extensions.use

internal class MessageListViewStyle(context: Context, attrs: AttributeSet?) {

    internal val scrollButtonViewStyle: ScrollButtonViewStyle
    internal val itemStyle: MessageListItemStyle

    init {
        context.obtainStyledAttributes(
            attrs,
            R.styleable.MessageListView,
            0,
            0
        ).use { attributes ->
            scrollButtonViewStyle = ScrollButtonViewStyle.Builder(attributes)
                .scrollButtonEnabled(
                    R.styleable.MessageListView_streamUiScrollButtonEnabled,
                    true
                )
                .scrollButtonUnreadEnabled(
                    R.styleable.MessageListView_streamUiScrollButtonUnreadEnabled,
                    true
                )
                .scrollButtonColor(
                    R.styleable.MessageListView_streamUiScrollButtonColor,
                    context.getColorCompat(R.color.stream_ui_white)
                )
                .scrollButtonRippleColor(
                    R.styleable.MessageListView_streamUiScrollButtonRippleColor,
                    context.getColorCompat(R.color.stream_ui_white_smoke)
                )
                .scrollButtonBadgeColor(
                    R.styleable.MessageListView_streamUiScrollButtonBadgeColor,
                    context.getColorCompat(R.color.stream_ui_accent_blue)
                )
                .scrollButtonIcon(
                    R.styleable.MessageListView_streamUiScrollButtonIcon,
                    context.getDrawableCompat(R.drawable.stream_ui_ic_down)
                ).build()

            itemStyle = MessageListItemStyle.Builder(attributes)
                .messageBackgroundColorMine(R.styleable.MessageListView_streamUiMessageBackgroundColorMine)
                .messageBackgroundColorTheirs(R.styleable.MessageListView_streamUiMessageBackgroundColorTheirs)
                .messageTextColorMine(R.styleable.MessageListView_streamUiMessageTextColorMine)
                .messageTextColorTheirs(R.styleable.MessageListView_streamUiMessageTextColorTheirs)
                .messageLinkTextColorMine(R.styleable.MessageListView_streamUiMessageLinkColorMine)
                .messageLinkTextColorTheirs(R.styleable.MessageListView_streamUiMessageLinkColorTheirs)
                .build()
        }
    }
}
