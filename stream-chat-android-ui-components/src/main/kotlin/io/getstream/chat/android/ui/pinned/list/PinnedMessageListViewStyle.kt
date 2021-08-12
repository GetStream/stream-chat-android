package io.getstream.chat.android.ui.pinned.list

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

public data class PinnedMessageListViewStyle(
    @ColorInt public val backgroundColor: Int,
    public val emptyStateDrawable: Drawable,
    public val messagePreviewStyle: MessagePreviewStyle,
) {

    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): PinnedMessageListViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.PinnedMessageListView,
                R.attr.streamUiPinnedMessageListStyle,
                R.style.StreamUi_PinnedMessageList,
            ).use { typedArray ->
                val backgroundColor = typedArray.getColor(
                    R.styleable.PinnedMessageListView_streamUiPinnedMessageListBackground,
                    context.getColorCompat(R.color.stream_ui_white_snow),
                )

                val emptyStateDrawable = typedArray.getDrawable(
                    R.styleable.PinnedMessageListView_streamUiPinnedMessageListEmptyStateDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_pinned_messages_empty)!!

                val senderTextStyle = TextStyle.Builder(typedArray)
                    .size(
                        R.styleable.PinnedMessageListView_streamUiPinnedMessageListSenderNameTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.PinnedMessageListView_streamUiPinnedMessageListSenderNameTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.PinnedMessageListView_streamUiPinnedMessageListSenderNameTextFontAssets,
                        R.styleable.PinnedMessageListView_streamUiPinnedMessageListSenderNameTextFont
                    )
                    .style(
                        R.styleable.PinnedMessageListView_streamUiPinnedMessageListSenderNameTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val messageTextStyle = TextStyle.Builder(typedArray)
                    .size(
                        R.styleable.PinnedMessageListView_streamUiPinnedMessageListMessageTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.PinnedMessageListView_streamUiPinnedMessageListMessageTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.PinnedMessageListView_streamUiPinnedMessageListMessageTextFontAssets,
                        R.styleable.PinnedMessageListView_streamUiPinnedMessageListMessageTextFont
                    )
                    .style(
                        R.styleable.PinnedMessageListView_streamUiPinnedMessageListMessageTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val messageTimeTextStyle = TextStyle.Builder(typedArray)
                    .size(
                        R.styleable.PinnedMessageListView_streamUiPinnedMessageListMessageTimeTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.PinnedMessageListView_streamUiPinnedMessageListMessageTimeTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.PinnedMessageListView_streamUiPinnedMessageListMessageTimeTextFontAssets,
                        R.styleable.PinnedMessageListView_streamUiPinnedMessageListMessageTimeTextFont
                    )
                    .style(
                        R.styleable.PinnedMessageListView_streamUiPinnedMessageListMessageTimeTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                return PinnedMessageListViewStyle(
                    backgroundColor = backgroundColor,
                    emptyStateDrawable = emptyStateDrawable,
                    messagePreviewStyle = MessagePreviewStyle(
                        messageSenderTextStyle = senderTextStyle,
                        messageTextStyle = messageTextStyle,
                        messageTimeTextStyle = messageTimeTextStyle,
                    ),
                ).let(TransformStyle.pinnedMessageListViewStyleTransformer::transform)
            }
        }
    }
}
