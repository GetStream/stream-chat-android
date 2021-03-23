package io.getstream.chat.android.ui.channel.list

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.Px
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.use

public class ChannelListViewStyle(
    @Px public val channelTitleTextSize: Float,
    @Px public val lastMessageSize: Float,
    @Px public val lastMessageDateTextSize: Float,
) {

    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): ChannelListViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.ChannelListView,
                0,
                0
            ).use { a ->
                val resources = context.resources

                val channelTitleTextSize = a.getDimensionPixelSize(
                    R.styleable.ChannelListView_streamUiChannelTitleTextSize,
                    resources.getDimensionPixelSize(R.dimen.stream_ui_channel_item_title)
                ).toFloat()

                val lastMessageSize = a.getDimensionPixelSize(
                    R.styleable.ChannelListView_streamUiLastMessageTextSize,
                    resources.getDimensionPixelSize(R.dimen.stream_ui_channel_item_message)
                ).toFloat()

                val lastMessageDateTextSize = a.getDimensionPixelSize(
                    R.styleable.ChannelListView_streamUiLastMessageDateTextSize,
                    resources.getDimensionPixelSize(R.dimen.stream_ui_channel_item_message_date)
                ).toFloat()

                return ChannelListViewStyle(
                    channelTitleTextSize = channelTitleTextSize,
                    lastMessageSize = lastMessageSize,
                    lastMessageDateTextSize = lastMessageDateTextSize
                ).let(TransformStyle.channelListStyleTransformer::transform)
            }
        }
    }
}
