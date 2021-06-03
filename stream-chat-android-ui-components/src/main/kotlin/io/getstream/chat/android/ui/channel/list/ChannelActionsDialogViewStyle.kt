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
 * @property memberNamesTextStyle - test appearance for dialog title with member names
 * @property memberInfoTextStyle - test appearance for dialog subtitle with member info
 * @property itemTextStyle - text appearance for action item
 * @property viewInfoIcon - icon for view info action. Default - [R.drawable.stream_ui_ic_single_user]
 * @property viewInfoEnabled - shows/hides view info action. Hidden by default
 * @property leaveGroupIcon - icon for leave group action. Default - [R.drawable.stream_ui_ic_leave_group]
 * @property leaveGroupEnabled - shows/hides leave group action. Shown by default
 * @property deleteConversationIcon - icon for delete conversation action. Default - [R.drawable.stream_ui_ic_delete]
 * @property deleteConversationEnabled - shows/hides delete conversation action. Shown by default
 * @property cancelIcon - icon for dismiss dialog action. Default - [R.drawable.stream_ui_ic_clear]
 * @property cancelEnabled - shows/hides dismiss dialog action. Shown by default
 * @property iconsTint - message options icon's tint. Default - [R.color.stream_ui_grey]
 * @property warningActionsTint - color of dangerous actions such as delete conversation. Default - [R.color.stream_ui_accent_red].
 */
public data class ChannelActionsDialogViewStyle(
    public val memberNamesTextStyle: TextStyle,
    public val memberInfoTextStyle: TextStyle,
    public val itemTextStyle: TextStyle,
    public val viewInfoIcon: Drawable,
    public val viewInfoEnabled: Boolean,
    public val leaveGroupIcon: Drawable,
    public val leaveGroupEnabled: Boolean,
    public val deleteConversationIcon: Drawable,
    public val deleteConversationEnabled: Boolean,
    public val cancelIcon: Drawable,
    public val cancelEnabled: Boolean,
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

                val memberNamesTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsMemberNamesTextSize,
                        context.getDimension(R.dimen.stream_ui_text_large)
                    )
                    .color(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsMemberNamesTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsMemberNamesTextFontAssets,
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsMemberNamesTextFont
                    )
                    .style(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsMemberNamesTextStyle,
                        Typeface.BOLD
                    )
                    .build()

                val memberInfoTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsMemberInfoTextSize,
                        context.getDimension(R.dimen.stream_ui_text_small)
                    )
                    .color(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsMemberInfoTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary)
                    )
                    .font(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsMemberInfoTextFontAssets,
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsMemberInfoTextFont
                    )
                    .style(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsMemberInfoTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

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

                val viewInfoEnabled = a.getBoolean(
                    R.styleable.ChannelActionsDialog_streamUiChannelActionsViewInfoEnabled,
                    false
                )

                val leaveGroupIcon =
                    a.getDrawable(R.styleable.ChannelActionsDialog_streamUiChannelActionsLeaveGroupIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_ic_leave_group)!!

                val leaveGroupEnabled = a.getBoolean(
                    R.styleable.ChannelActionsDialog_streamUiChannelActionsLeaveGroupEnabled,
                    true
                )

                val deleteConversationIcon =
                    a.getDrawable(R.styleable.ChannelActionsDialog_streamUiChannelActionsDeleteConversationIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_ic_delete)!!

                val deleteConversationEnabled = a.getBoolean(
                    R.styleable.ChannelActionsDialog_streamUiChannelActionsDeleteConversationEnabled,
                    true
                )

                val cancelIcon = a.getDrawable(R.styleable.ChannelActionsDialog_streamUiChannelActionsCancelIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_clear)!!

                val cancelEnabled = a.getBoolean(
                    R.styleable.ChannelActionsDialog_streamUiChannelActionsCancelEnabled,
                    true
                )

                val iconsTint = a.getColor(
                    R.styleable.ChannelActionsDialog_streamUiChannelActionsIconsTint,
                    context.getColorCompat(R.color.stream_ui_grey)
                )

                val warningActionsTint = a.getColor(
                    R.styleable.ChannelActionsDialog_streamUiChannelActionsWarningActionsTint,
                    context.getColorCompat(R.color.stream_ui_accent_red)
                )

                return ChannelActionsDialogViewStyle(
                    memberNamesTextStyle = memberNamesTextStyle,
                    memberInfoTextStyle = memberInfoTextStyle,
                    itemTextStyle = itemTextStyle,
                    viewInfoIcon = viewInfoIcon,
                    viewInfoEnabled = viewInfoEnabled,
                    leaveGroupIcon = leaveGroupIcon,
                    leaveGroupEnabled = leaveGroupEnabled,
                    deleteConversationIcon = deleteConversationIcon,
                    deleteConversationEnabled = deleteConversationEnabled,
                    cancelIcon = cancelIcon,
                    cancelEnabled = cancelEnabled,
                    iconsTint = iconsTint,
                    warningActionsTint = warningActionsTint,
                ).let(TransformStyle.channelActionsDialogStyleTransformer::transform)
            }
        }
    }
}
