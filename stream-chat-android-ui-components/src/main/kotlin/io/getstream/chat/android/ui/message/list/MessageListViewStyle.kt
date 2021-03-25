package io.getstream.chat.android.ui.message.list

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorInt
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.use

public data class MessageListViewStyle(
    public val scrollButtonViewStyle: ScrollButtonViewStyle,
    public val itemStyle: MessageListItemStyle,
    public val reactionsEnabled: Boolean,
    @ColorInt public val backgroundColor: Int,
) {

    internal companion object {
        private val DEFAULT_BACKGROUND_COLOR = R.color.stream_ui_white_snow

        operator fun invoke(context: Context, attrs: AttributeSet?): MessageListViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.MessageListView,
                0,
                0
            ).use { attributes ->
                val scrollButtonViewStyle = ScrollButtonViewStyle.Builder(attributes)
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

                val reactionsEnabled = attributes.getBoolean(
                    R.styleable.MessageListView_streamUiReactionsEnabled,
                    true
                )

                val backgroundColor = attributes.getColor(
                    R.styleable.MessageListView_streamUiBackgroundColor,
                    context.getColorCompat(DEFAULT_BACKGROUND_COLOR)
                )

                val itemStyle = MessageListItemStyle.Builder(attributes)
                    .messageBackgroundColorMine(R.styleable.MessageListView_streamUiMessageBackgroundColorMine)
                    .messageBackgroundColorTheirs(R.styleable.MessageListView_streamUiMessageBackgroundColorTheirs)
                    .messageTextColorMine(R.styleable.MessageListView_streamUiMessageTextColorMine)
                    .messageTextColorTheirs(R.styleable.MessageListView_streamUiMessageTextColorTheirs)
                    .messageLinkTextColorMine(R.styleable.MessageListView_streamUiMessageLinkColorMine)
                    .messageLinkTextColorTheirs(R.styleable.MessageListView_streamUiMessageLinkColorTheirs)
                    .reactionsEnabled(R.styleable.MessageListView_streamUiReactionsEnabled)
                    .threadsEnabled(R.styleable.MessageListView_streamUiThreadsEnabled)
                    .linkDescriptionMaxLines(R.styleable.MessageListView_streamUiLinkDescriptionMaxLines)
                    .build()

                return MessageListViewStyle(
                    scrollButtonViewStyle = scrollButtonViewStyle,
                    reactionsEnabled = reactionsEnabled,
                    itemStyle = itemStyle,
                    backgroundColor = backgroundColor,
                ).let(TransformStyle.messageListStyleTransformer::transform)
            }
        }
    }
}
