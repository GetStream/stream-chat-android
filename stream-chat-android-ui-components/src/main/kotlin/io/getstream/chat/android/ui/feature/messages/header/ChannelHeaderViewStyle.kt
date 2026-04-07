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

package io.getstream.chat.android.ui.feature.messages.header

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.helper.ViewStyle
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat
import io.getstream.chat.android.ui.utils.extensions.use

/**
 * Style for [ChannelHeaderView].
 * Use this class together with [TransformStyle.channelHeaderStyleTransformer] to change [ChannelHeaderView] styles programmatically.
 *
 * @property titleTextStyle Appearance for title text.
 * @property offlineTextStyle Appearance for offline text.
 * @property searchingForNetworkTextStyle Appearance for searching for network text.
 * @property onlineTextStyle Appearance for online text.
 * @property showUserAvatar Shows/hides user avatar. Shown by default.
 * @property backButtonIcon Icon for back button. Default value is [R.drawable.stream_ui_arrow_left].
 * @property showBackButton Shows/hides back button. Shown by default.
 * @property showBackButtonBadge Shows/hides unread badge. Hidden by default.
 * @property backButtonBadgeBackgroundColor Unread badge color. Default value is [R.color.stream_ui_accent_red].
 * @property showSearchingForNetworkProgressBar Shows/hides searching for network progress bar. Shown by default.
 * @property searchingForNetworkProgressBarTint Progress bar tint color. Default value is [R.color.stream_ui_accent_blue].
 * @property separatorBackgroundDrawable Background drawable of the separator at the bottom of [ChannelHeaderView].
 */
public data class ChannelHeaderViewStyle(
    @ColorInt public val background: Int,
    public val titleTextStyle: TextStyle,
    public val offlineTextStyle: TextStyle,
    public val searchingForNetworkTextStyle: TextStyle,
    public val onlineTextStyle: TextStyle,
    public val showUserAvatar: Boolean,
    public val backButtonIcon: Drawable,
    public val showBackButton: Boolean,
    public val showBackButtonBadge: Boolean,
    @ColorInt public val backButtonBadgeBackgroundColor: Int,
    public val showSearchingForNetworkProgressBar: Boolean,
    public val searchingForNetworkProgressBarTint: ColorStateList,
    public val separatorBackgroundDrawable: Drawable?,
) : ViewStyle {

    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): ChannelHeaderViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.ChannelHeaderView,
                R.attr.streamUiChannelHeaderStyle,
                R.style.StreamUi_ChannelHeader,
            ).use { a ->
                val background = a.getColor(
                    R.styleable.ChannelHeaderView_streamUiChannelHeaderBackground,
                    context.getColorCompat(R.color.stream_ui_white),
                )

                val showUserAvatar =
                    a.getBoolean(R.styleable.ChannelHeaderView_streamUiChannelHeaderShowUserAvatar, true)

                val backButtonIcon =
                    a.getDrawable(R.styleable.ChannelHeaderView_streamUiChannelHeaderBackButtonIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_arrow_left)!!

                val titleTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.ChannelHeaderView_streamUiChannelHeaderTitleTextSize,
                        context.getDimension(R.dimen.stream_ui_text_large),
                    )
                    .color(
                        R.styleable.ChannelHeaderView_streamUiChannelHeaderTitleTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary),
                    )
                    .font(
                        R.styleable.ChannelHeaderView_streamUiChannelHeaderTitleFontAssets,
                        R.styleable.ChannelHeaderView_streamUiChannelHeaderTitleTextFont,
                    )
                    .style(
                        R.styleable.ChannelHeaderView_streamUiChannelHeaderTitleTextStyle,
                        Typeface.BOLD,
                    ).build()

                val showBackButton =
                    a.getBoolean(R.styleable.ChannelHeaderView_streamUiChannelHeaderShowBackButton, true)

                val showBackButtonBadge =
                    a.getBoolean(R.styleable.ChannelHeaderView_streamUiChannelHeaderShowBackButtonBadge, false)

                val backButtonBadgeBackgroundColor = a.getColor(
                    R.styleable.ChannelHeaderView_streamUiChannelHeaderBackButtonBadgeBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_accent_red),
                )

                val offlineTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.ChannelHeaderView_streamUiChannelHeaderOfflineLabelTextSize,
                        context.getDimension(R.dimen.stream_ui_text_small),
                    )
                    .color(
                        R.styleable.ChannelHeaderView_streamUiChannelHeaderOfflineLabelTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary),
                    )
                    .font(
                        R.styleable.ChannelHeaderView_streamUiChannelHeaderOfflineLabelFontAssets,
                        R.styleable.ChannelHeaderView_streamUiChannelHeaderOfflineLabelTextFont,
                    )
                    .style(
                        R.styleable.ChannelHeaderView_streamUiChannelHeaderOfflineLabelTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val searchingForNetworkTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.ChannelHeaderView_streamUiChannelHeaderSearchingForNetworkLabelTextSize,
                        context.getDimension(R.dimen.stream_ui_text_small),
                    )
                    .color(
                        R.styleable.ChannelHeaderView_streamUiChannelHeaderSearchingForNetworkLabelColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary),
                    )
                    .font(
                        R.styleable.ChannelHeaderView_streamUiChannelHeaderSearchingForNetworkLabelFontAssets,
                        R.styleable.ChannelHeaderView_streamUiChannelHeaderSearchingForNetworkLabelTextFont,
                    )
                    .style(
                        R.styleable.ChannelHeaderView_streamUiChannelHeaderSearchingForNetworkLabelTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val showSearchingForNetworkProgressBar = a.getBoolean(
                    R.styleable.ChannelHeaderView_streamUiChannelHeaderShowSearchingForNetworkProgressBar,
                    true,
                )

                val searchingForNetworkProgressBarTint = a.getColorStateList(
                    R.styleable.ChannelHeaderView_streamUiChannelHeaderSearchingForNetworkProgressBarTint,
                ) ?: ContextCompat.getColorStateList(context, R.color.stream_ui_accent_blue)!!

                val onlineTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.ChannelHeaderView_streamUiChannelHeaderDefaultLabelTextSize,
                        context.getDimension(R.dimen.stream_ui_text_small),
                    )
                    .color(
                        R.styleable.ChannelHeaderView_streamUiChannelHeaderDefaultLabelTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary),
                    )
                    .font(
                        R.styleable.ChannelHeaderView_streamUiChannelHeaderDefaultLabelFontAssets,
                        R.styleable.ChannelHeaderView_streamUiChannelHeaderDefaultLabelTextFont,
                    )
                    .style(
                        R.styleable.ChannelHeaderView_streamUiChannelHeaderDefaultLabelTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val separatorBackgroundDrawable =
                    a.getDrawable(
                        R.styleable.ChannelHeaderView_streamUiChannelHeaderSeparatorBackgroundDrawable,
                    )

                return ChannelHeaderViewStyle(
                    background = background,
                    titleTextStyle = titleTextStyle,
                    offlineTextStyle = offlineTextStyle,
                    searchingForNetworkTextStyle = searchingForNetworkTextStyle,
                    onlineTextStyle = onlineTextStyle,
                    showUserAvatar = showUserAvatar,
                    backButtonIcon = backButtonIcon,
                    showBackButton = showBackButton,
                    showBackButtonBadge = showBackButtonBadge,
                    backButtonBadgeBackgroundColor = backButtonBadgeBackgroundColor,
                    showSearchingForNetworkProgressBar = showSearchingForNetworkProgressBar,
                    searchingForNetworkProgressBarTint = searchingForNetworkProgressBarTint,
                    separatorBackgroundDrawable = separatorBackgroundDrawable,
                ).let(TransformStyle.channelHeaderStyleTransformer::transform)
            }
        }
    }
}
