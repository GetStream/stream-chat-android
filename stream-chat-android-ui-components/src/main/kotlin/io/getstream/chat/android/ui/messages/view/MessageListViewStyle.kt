package io.getstream.chat.android.ui.messages.view

import android.content.Context
import android.util.AttributeSet
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat
import io.getstream.chat.android.ui.utils.extensions.use

internal class MessageListViewStyle(context: Context, attrs: AttributeSet?) {

    internal val scrollButtonViewStyle: ScrollButtonViewStyle

    init {
        context.obtainStyledAttributes(
            attrs,
            R.styleable.MessageListView,
            0,
            0
        ).use {
            scrollButtonViewStyle = ScrollButtonViewStyle.Builder(it)
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
                    context.getColorCompat(R.color.stream_ui_grey_light)
                )
                .scrollButtonBadgeColor(
                    R.styleable.MessageListView_streamUiScrollButtonBadgeColor,
                    context.getColorCompat(R.color.stream_ui_blue)
                )
                .scrollButtonIcon(
                    R.styleable.MessageListView_streamUiScrollButtonIcon,
                    context.getDrawableCompat(R.drawable.stream_ui_ic_down)
                ).build()
        }
    }
}
