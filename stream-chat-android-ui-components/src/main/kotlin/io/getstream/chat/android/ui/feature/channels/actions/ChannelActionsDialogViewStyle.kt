/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ui.feature.channels.actions

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.feature.channels.actions.internal.ChannelActionsDialogFragment
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.helper.ViewStyle
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat
import io.getstream.chat.android.ui.utils.extensions.use

/**
 * Style for [ChannelActionsDialogFragment].
 * Use this class together with [TransformStyle.channelActionsDialogStyleTransformer] to change [ChannelActionsDialogFragment] styles programmatically.
 *
 * @property memberNamesTextStyle Text appearance for dialog title with member names.
 * @property memberInfoTextStyle Text appearance for dialog subtitle with member info.
 * @property itemTextStyle Text appearance for action item.
 * @property itemTextStyle Text appearance for warning action item.
 * @property viewInfoIcon Icon for view info action. Default value is [R.drawable.stream_ui_ic_single_user].
 * @property viewInfoEnabled Shows/hides view info action. Hidden by default.
 * @property leaveGroupIcon Icon for leave group action. Default value is [R.drawable.stream_ui_ic_leave_group].
 * @property leaveGroupEnabled Shows/hides leave group action. Shown by default.
 * @property deleteConversationIcon Icon for delete conversation action. Default value is [R.drawable.stream_ui_ic_delete].
 * @property deleteConversationEnabled Shows/hides delete conversation action. Shown by default.
 * @property cancelIcon Icon for dismiss dialog action. Default value is [R.drawable.stream_ui_ic_clear].
 * @property cancelEnabled Shows/hides dismiss dialog action. Shown by default.
 * @property background Dialog's background.
 */
public data class ChannelActionsDialogViewStyle(
    public val memberNamesTextStyle: TextStyle,
    public val memberInfoTextStyle: TextStyle,
    public val itemTextStyle: TextStyle,
    public val warningItemTextStyle: TextStyle,
    public val viewInfoIcon: Drawable,
    public val viewInfoEnabled: Boolean,
    public val leaveGroupIcon: Drawable,
    public val leaveGroupEnabled: Boolean,
    public val deleteConversationIcon: Drawable,
    public val deleteConversationEnabled: Boolean,
    public val cancelIcon: Drawable,
    public val cancelEnabled: Boolean,
    public val background: Drawable,
) : ViewStyle {
    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): ChannelActionsDialogViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.ChannelListView,
                0,
                0,
            ).use {
                val a = context.obtainStyledAttributes(
                    it.getResourceId(
                        R.styleable.ChannelListView_streamUiChannelActionsDialogStyle,
                        R.style.StreamUi_ChannelList_ActionsDialog,
                    ),
                    R.styleable.ChannelActionsDialog,
                )

                val memberNamesTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsMemberNamesTextSize,
                        context.getDimension(R.dimen.stream_ui_text_large),
                    )
                    .color(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsMemberNamesTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary),
                    )
                    .font(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsMemberNamesTextFontAssets,
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsMemberNamesTextFont,
                    )
                    .style(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsMemberNamesTextStyle,
                        Typeface.BOLD,
                    )
                    .build()

                val memberInfoTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsMemberInfoTextSize,
                        context.getDimension(R.dimen.stream_ui_text_small),
                    )
                    .color(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsMemberInfoTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary),
                    )
                    .font(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsMemberInfoTextFontAssets,
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsMemberInfoTextFont,
                    )
                    .style(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsMemberInfoTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val itemTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsItemTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium),
                    )
                    .color(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsItemTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary),
                    )
                    .font(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsItemTextFontAssets,
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsItemTextFont,
                    )
                    .style(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsItemTextStyle,
                        Typeface.BOLD,
                    )
                    .build()

                val warningItemTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsWarningItemTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium),
                    )
                    .color(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsWarningItemTextColor,
                        context.getColorCompat(R.color.stream_ui_accent_red),
                    )
                    .font(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsWarningItemTextFontAssets,
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsWarningItemTextFont,
                    )
                    .style(
                        R.styleable.ChannelActionsDialog_streamUiChannelActionsWarningItemTextStyle,
                        Typeface.BOLD,
                    )
                    .build()

                val viewInfoIcon = a.getDrawable(R.styleable.ChannelActionsDialog_streamUiChannelActionsViewInfoIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_single_user)!!

                val viewInfoEnabled = a.getBoolean(
                    R.styleable.ChannelActionsDialog_streamUiChannelActionsViewInfoEnabled,
                    false,
                )

                val leaveGroupIcon =
                    a.getDrawable(R.styleable.ChannelActionsDialog_streamUiChannelActionsLeaveGroupIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_ic_leave_group)!!

                val leaveGroupEnabled = a.getBoolean(
                    R.styleable.ChannelActionsDialog_streamUiChannelActionsLeaveGroupEnabled,
                    true,
                )

                val deleteConversationIcon =
                    a.getDrawable(R.styleable.ChannelActionsDialog_streamUiChannelActionsDeleteConversationIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_ic_delete)!!

                val deleteConversationEnabled = a.getBoolean(
                    R.styleable.ChannelActionsDialog_streamUiChannelActionsDeleteConversationEnabled,
                    true,
                )

                val cancelIcon = a.getDrawable(R.styleable.ChannelActionsDialog_streamUiChannelActionsCancelIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_clear)!!

                val cancelEnabled = a.getBoolean(
                    R.styleable.ChannelActionsDialog_streamUiChannelActionsCancelEnabled,
                    true,
                )

                val background = a.getDrawable(R.styleable.ChannelActionsDialog_streamUiChannelActionsBackground)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_round_bottom_sheet)!!

                return ChannelActionsDialogViewStyle(
                    memberNamesTextStyle = memberNamesTextStyle,
                    memberInfoTextStyle = memberInfoTextStyle,
                    itemTextStyle = itemTextStyle,
                    warningItemTextStyle = warningItemTextStyle,
                    viewInfoIcon = viewInfoIcon,
                    viewInfoEnabled = viewInfoEnabled,
                    leaveGroupIcon = leaveGroupIcon,
                    leaveGroupEnabled = leaveGroupEnabled,
                    deleteConversationIcon = deleteConversationIcon,
                    deleteConversationEnabled = deleteConversationEnabled,
                    cancelIcon = cancelIcon,
                    cancelEnabled = cancelEnabled,
                    background = background,
                ).let(TransformStyle.channelActionsDialogStyleTransformer::transform)
            }
        }
    }
}
