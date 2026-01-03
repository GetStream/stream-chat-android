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

package io.getstream.chat.android.ui.widgets.avatar

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.Px
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.helper.ViewStyle
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.getEnum
import io.getstream.chat.android.ui.utils.extensions.use

/**
 * Style for [UserAvatarView] and [ChannelAvatarView].
 */
public data class AvatarStyle(
    @Px public val avatarBorderWidth: Int,
    @ColorInt public val avatarBorderColor: Int,
    public val avatarInitialsTextStyle: TextStyle,
    public val groupAvatarInitialsTextStyle: TextStyle,
    public val onlineIndicatorEnabled: Boolean,
    public val onlineIndicatorPosition: OnlineIndicatorPosition,
    @ColorInt public val onlineIndicatorColor: Int,
    @ColorInt public val onlineIndicatorBorderColor: Int,
    public val avatarShape: AvatarShape,
    @Px public val borderRadius: Float,
) : ViewStyle {

    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): AvatarStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.AvatarView,
                0,
                0,
            ).use {
                val avatarBorderWidth = it.getDimensionPixelSize(
                    R.styleable.AvatarView_streamUiAvatarBorderWidth,
                    context.getDimension(R.dimen.stream_ui_avatar_border_width),
                )

                val avatarBorderColor = it.getColor(
                    R.styleable.AvatarView_streamUiAvatarBorderColor,
                    context.getColorCompat(R.color.stream_ui_black),
                )

                val avatarInitialsTextStyle = TextStyle.Builder(it)
                    .size(
                        R.styleable.AvatarView_streamUiAvatarTextSize,
                        context.getDimension(R.dimen.stream_ui_avatar_initials),
                    )
                    .color(
                        R.styleable.AvatarView_streamUiAvatarTextColor,
                        context.getColorCompat(R.color.stream_ui_white),
                    )
                    .font(
                        R.styleable.AvatarView_streamUiAvatarTextFontAssets,
                        R.styleable.AvatarView_streamUiAvatarTextFont,
                    )
                    .style(
                        R.styleable.AvatarView_streamUiAvatarTextStyle,
                        Typeface.BOLD,
                    )
                    .build()

                val groupAvatarInitialsTextStyle = TextStyle.Builder(it)
                    .size(
                        R.styleable.AvatarView_streamUiGroupAvatarTextSize,
                        context.getDimension(R.dimen.stream_ui_group_avatar_initials),
                    )
                    .color(
                        R.styleable.AvatarView_streamUiGroupAvatarTextColor,
                        context.getColorCompat(R.color.stream_ui_white),
                    )
                    .font(
                        R.styleable.AvatarView_streamUiGroupAvatarTextFontAssets,
                        R.styleable.AvatarView_streamUiGroupAvatarTextFont,
                    )
                    .style(
                        R.styleable.AvatarView_streamUiGroupAvatarTextStyle,
                        Typeface.BOLD,
                    )
                    .build()

                val onlineIndicatorEnabled = it.getBoolean(
                    R.styleable.AvatarView_streamUiAvatarOnlineIndicatorEnabled,
                    false,
                )

                val onlineIndicatorPosition = it.getEnum(
                    R.styleable.AvatarView_streamUiAvatarOnlineIndicatorPosition,
                    OnlineIndicatorPosition.TOP_END,
                )
                val onlineIndicatorColor =
                    it.getColor(R.styleable.AvatarView_streamUiAvatarOnlineIndicatorColor, Color.GREEN)

                val onlineIndicatorBorderColor =
                    it.getColor(
                        R.styleable.AvatarView_streamUiAvatarOnlineIndicatorBorderColor,
                        context.getColorCompat(R.color.stream_ui_white),
                    )

                val avatarShape =
                    it.getEnum(R.styleable.AvatarView_streamUiAvatarShape, AvatarShape.CIRCLE)

                val borderRadius =
                    it.getDimensionPixelSize(
                        R.styleable.AvatarView_streamUiAvatarBorderRadius,
                        4.dpToPx(),
                    ).toFloat()

                return AvatarStyle(
                    avatarBorderWidth = avatarBorderWidth,
                    avatarBorderColor = avatarBorderColor,
                    avatarInitialsTextStyle = avatarInitialsTextStyle,
                    groupAvatarInitialsTextStyle = groupAvatarInitialsTextStyle,
                    onlineIndicatorEnabled = onlineIndicatorEnabled,
                    onlineIndicatorPosition = onlineIndicatorPosition,
                    onlineIndicatorColor = onlineIndicatorColor,
                    onlineIndicatorBorderColor = onlineIndicatorBorderColor,
                    avatarShape = avatarShape,
                    borderRadius = borderRadius,
                ).let(TransformStyle.avatarStyleTransformer::transform)
            }
        }
    }
}
