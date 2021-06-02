package io.getstream.chat.android.ui.channel.list

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.channel.actions.internal.ChannelActionsDialogFragment
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.common.style.TextStyle

/**
 * Style for [ChannelActionsDialogFragment].
 * Use this class together with [TransformStyle.channelActionsDialogStyleTransformer] to change [ChannelActionsDialogFragment] styles programmatically.
 *
 * @property itemTextStyle - text appearance for action item
 * @property viewInfoIcon - icon for view info action. Default - [R.drawable.stream_ui_ic_single_user]
 * @property leaveGroupIcon - icon for leave group action. Default - [R.drawable.stream_ui_ic_leave_group]
 * @property deleteConversationIcon - icon for delete conversation action. Default - [R.drawable.stream_ui_ic_delete]
 * @property cancelIcon - icon for dismiss dialog action. Default - [R.drawable.stream_ui_ic_clear]
 * @property iconsTint - message options icon's tint. Default - [R.color.stream_ui_grey]
 * @property warningActionsTint - color of dangerous actions such as delete conversation. Default - [R.color.stream_ui_accent_red].
 */
public data class ChannelActionsDialogViewStyle(
    public val itemTextStyle: TextStyle,
    public val viewInfoIcon: Drawable,
    public val leaveGroupIcon: Drawable,
    public val deleteConversationIcon: Drawable,
    public val cancelIcon: Drawable,
    @ColorInt public val iconsTint: Int,
    @ColorInt public val warningActionsTint: Int,
) {
    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): ChannelActionsDialogViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.ChannelListView,
                0,
                0
            ).use {

                val a = context.obtainStyledAttributes(
                    it.getResourceId(
                        R.styleable.ChannelListView_streamUiChannelActionsDialogStyle,
                        -1
                    ),
                    R.styleable.ChannelActionsDialog
                )

                val itemTextStyle = TextStyle.Builder(a)
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

                val viewInfoIcon = a.getDrawable(R.styleable.ChannelActionsDialog_streamUiChannelActionsViewInfoIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_single_user)!!

                val leaveGroupIcon =
                    a.getDrawable(R.styleable.ChannelActionsDialog_streamUiChannelActionsLeaveGroupIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_ic_leave_group)!!

                val deleteConversationIcon =
                    a.getDrawable(R.styleable.ChannelActionsDialog_streamUiChannelActionsDeleteConversationIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_ic_delete)!!

                val cancelIcon = a.getDrawable(R.styleable.ChannelActionsDialog_streamUiChannelActionsCancelIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_clear)!!

                val iconsTint = a.getColor(
                    R.styleable.ChannelActionsDialog_streamUiChannelActionsItemTint,
                    context.getColorCompat(R.color.stream_ui_grey)
                )

                val warningActionsTint = a.getColor(
                    R.styleable.ChannelActionsDialog_streamUiChannelActionsWarningItemTint,
                    context.getColorCompat(R.color.stream_ui_accent_red)
                )

                return ChannelActionsDialogViewStyle(
                    itemTextStyle = itemTextStyle,
                    viewInfoIcon = viewInfoIcon,
                    leaveGroupIcon = leaveGroupIcon,
                    deleteConversationIcon = deleteConversationIcon,
                    cancelIcon = cancelIcon,
                    iconsTint = iconsTint,
                    warningActionsTint = warningActionsTint,
                ).let(TransformStyle.channelActionsDialogStyleTransformer::transform)
            }
        }
    }
}
