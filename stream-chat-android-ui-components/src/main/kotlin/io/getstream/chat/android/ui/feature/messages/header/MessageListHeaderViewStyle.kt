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
 * Style for [MessageListHeaderView].
 * Use this class together with [TransformStyle.messageListHeaderStyleTransformer] to change [MessageListHeaderView] styles programmatically.
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
 * @property separatorBackgroundDrawable Background drawable of the separator at the bottom of [MessageListHeaderView].
 */
public data class MessageListHeaderViewStyle(
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
        operator fun invoke(context: Context, attrs: AttributeSet?): MessageListHeaderViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.MessageListHeaderView,
                R.attr.streamUiMessageListHeaderStyle,
                R.style.StreamUi_MessageListHeader,
            ).use { a ->
                val background = a.getColor(
                    R.styleable.MessageListHeaderView_streamUiMessageListHeaderBackground,
                    context.getColorCompat(R.color.stream_ui_white),
                )

                val showUserAvatar =
                    a.getBoolean(R.styleable.MessageListHeaderView_streamUiMessageListHeaderShowUserAvatar, true)

                val backButtonIcon =
                    a.getDrawable(R.styleable.MessageListHeaderView_streamUiMessageListHeaderBackButtonIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_arrow_left)!!

                val titleTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageListHeaderView_streamUiMessageListHeaderTitleTextSize,
                        context.getDimension(R.dimen.stream_ui_text_large),
                    )
                    .color(
                        R.styleable.MessageListHeaderView_streamUiMessageListHeaderTitleTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary),
                    )
                    .font(
                        R.styleable.MessageListHeaderView_streamUiMessageListHeaderTitleFontAssets,
                        R.styleable.MessageListHeaderView_streamUiMessageListHeaderTitleTextFont,
                    )
                    .style(
                        R.styleable.MessageListHeaderView_streamUiMessageListHeaderTitleTextStyle,
                        Typeface.BOLD,
                    ).build()

                val showBackButton =
                    a.getBoolean(R.styleable.MessageListHeaderView_streamUiMessageListHeaderShowBackButton, true)

                val showBackButtonBadge =
                    a.getBoolean(R.styleable.MessageListHeaderView_streamUiMessageListHeaderShowBackButtonBadge, false)

                val backButtonBadgeBackgroundColor = a.getColor(
                    R.styleable.MessageListHeaderView_streamUiMessageListHeaderBackButtonBadgeBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_accent_red),
                )

                val offlineTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageListHeaderView_streamUiMessageListHeaderOfflineLabelTextSize,
                        context.getDimension(R.dimen.stream_ui_text_small),
                    )
                    .color(
                        R.styleable.MessageListHeaderView_streamUiMessageListHeaderOfflineLabelTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary),
                    )
                    .font(
                        R.styleable.MessageListHeaderView_streamUiMessageListHeaderOfflineLabelFontAssets,
                        R.styleable.MessageListHeaderView_streamUiMessageListHeaderOfflineLabelTextFont,
                    )
                    .style(
                        R.styleable.MessageListHeaderView_streamUiMessageListHeaderOfflineLabelTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val searchingForNetworkTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageListHeaderView_streamUiMessageListHeaderSearchingForNetworkLabelTextSize,
                        context.getDimension(R.dimen.stream_ui_text_small),
                    )
                    .color(
                        R.styleable.MessageListHeaderView_streamUiMessageListHeaderSearchingForNetworkLabelColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary),
                    )
                    .font(
                        R.styleable.MessageListHeaderView_streamUiMessageListHeaderSearchingForNetworkLabelFontAssets,
                        R.styleable.MessageListHeaderView_streamUiMessageListHeaderSearchingForNetworkLabelTextFont,
                    )
                    .style(
                        R.styleable.MessageListHeaderView_streamUiMessageListHeaderSearchingForNetworkLabelTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val showSearchingForNetworkProgressBar = a.getBoolean(
                    R.styleable.MessageListHeaderView_streamUiMessageListHeaderShowSearchingForNetworkProgressBar,
                    true,
                )

                val searchingForNetworkProgressBarTint = a.getColorStateList(
                    R.styleable.MessageListHeaderView_streamUiMessageListHeaderSearchingForNetworkProgressBarTint,
                ) ?: ContextCompat.getColorStateList(context, R.color.stream_ui_accent_blue)!!

                val onlineTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageListHeaderView_streamUiMessageListHeaderDefaultLabelTextSize,
                        context.getDimension(R.dimen.stream_ui_text_small),
                    )
                    .color(
                        R.styleable.MessageListHeaderView_streamUiMessageListHeaderDefaultLabelTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary),
                    )
                    .font(
                        R.styleable.MessageListHeaderView_streamUiMessageListHeaderDefaultLabelFontAssets,
                        R.styleable.MessageListHeaderView_streamUiMessageListHeaderDefaultLabelTextFont,
                    )
                    .style(
                        R.styleable.MessageListHeaderView_streamUiMessageListHeaderDefaultLabelTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val separatorBackgroundDrawable =
                    a.getDrawable(
                        R.styleable.MessageListHeaderView_streamUiMessageListHeaderSeparatorBackgroundDrawable,
                    )

                return MessageListHeaderViewStyle(
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
                ).let(TransformStyle.messageListHeaderStyleTransformer::transform)
            }
        }
    }
}
