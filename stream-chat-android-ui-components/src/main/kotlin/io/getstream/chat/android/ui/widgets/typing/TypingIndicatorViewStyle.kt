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

package io.getstream.chat.android.ui.widgets.typing

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.annotation.LayoutRes
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.helper.ViewStyle
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.use

/**
 * Style for [TypingIndicatorView].
 * Use this class together with [TransformStyle.typingIndicatorViewStyleTransformer] to change [TypingIndicatorView] style programmatically.
 *
 * @property typingIndicatorAnimationView Typing animation view. Default value is [R.layout.stream_ui_typing_indicator_animation_view].
 * @property typingIndicatorUsersTextStyle Appearance for typing users text.
 */
public data class TypingIndicatorViewStyle(
    @LayoutRes public val typingIndicatorAnimationView: Int,
    public val typingIndicatorUsersTextStyle: TextStyle,
) : ViewStyle {
    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): TypingIndicatorViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.TypingIndicatorView,
                R.attr.streamUiTypingIndicatorView,
                R.style.StreamUi_TypingIndicatorView,
            ).use { a ->
                val typingIndicatorAnimationView = a.getResourceId(
                    R.styleable.TypingIndicatorView_streamUiTypingIndicatorAnimationView,
                    R.layout.stream_ui_typing_indicator_animation_view,
                )

                val typingIndicatorUsersTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.TypingIndicatorView_streamUiTypingIndicatorUsersTextSize,
                        context.getDimension(R.dimen.stream_ui_text_small),
                    )
                    .color(
                        R.styleable.TypingIndicatorView_streamUiTypingIndicatorUsersTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary),
                    )
                    .font(
                        R.styleable.TypingIndicatorView_streamUiTypingIndicatorUsersTextFontAssets,
                        R.styleable.TypingIndicatorView_streamUiTypingIndicatorUsersTextFont,
                    )
                    .style(
                        R.styleable.TypingIndicatorView_streamUiTypingIndicatorUsersTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                return TypingIndicatorViewStyle(
                    typingIndicatorAnimationView = typingIndicatorAnimationView,
                    typingIndicatorUsersTextStyle = typingIndicatorUsersTextStyle,
                ).let(TransformStyle.typingIndicatorViewStyleTransformer::transform)
            }
        }
    }
}
