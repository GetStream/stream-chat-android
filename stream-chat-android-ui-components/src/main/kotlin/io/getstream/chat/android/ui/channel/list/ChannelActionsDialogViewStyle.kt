package io.getstream.chat.android.ui.channel.list

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.channel.actions.internal.ChannelActionsDialogFragment
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.common.style.TextStyle

/**
 * Style for [ChannelActionsDialogFragment].
 *
 * @property channelActionItemTextStyle - text style for action buttons
 */
public data class ChannelActionsDialogViewStyle(
    val channelActionItemTextStyle: TextStyle,
) {
    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): ChannelActionsDialogViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.ChannelListView,
                0,
                0
            ).use { channelListStyleAttributes ->

                val channelActionsDialogStyleAttributes = context.obtainStyledAttributes(
                    channelListStyleAttributes.getResourceId(
                        R.styleable.ChannelListView_streamUiChannelActionsDialogStyle,
                        -1
                    ),
                    R.styleable.ChannelActionsDialog
                )

                val channelActionItemTextStyle = TextStyle.Builder(channelActionsDialogStyleAttributes)
                    .size(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsItemTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsItemTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsItemTextFontAssets,
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsItemTextFont
                    )
                    .style(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsItemTextStyle,
                        Typeface.BOLD
                    )
                    .build()

                return ChannelActionsDialogViewStyle(
                    channelActionItemTextStyle = channelActionItemTextStyle
                ).let(TransformStyle.channelActionsDialogStyleTransformer::transform)
            }
        }
    }
}
