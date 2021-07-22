package io.getstream.chat.android.ui.typing

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.annotation.LayoutRes
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.common.style.TextStyle

public data class TypingIndicatorViewStyle(
    @LayoutRes public val typingIndicatorAnimationView: Int,
    public val typingIndicatorUsersTextStyle: TextStyle,
) {
    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): TypingIndicatorViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.TypingIndicatorView,
                R.attr.streamUiTypingIndicatorView,
                0, // TODO
            ).use { a ->
                val typingIndicatorAnimationView = a.getResourceId(
                    R.styleable.TypingIndicatorView_streamUiTypingIndicatorAnimationView,
                    R.layout.stream_ui_typing_indicator_animation_view,
                )

                val typingIndicatorUsersTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.TypingIndicatorView_streamUiTypingIndicatorUsersTextSize,
                        context.getDimension(R.dimen.stream_ui_text_small)
                    )
                    .color(
                        R.styleable.TypingIndicatorView_streamUiTypingIndicatorUsersTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary)
                    )
                    .font(
                        R.styleable.TypingIndicatorView_streamUiTypingIndicatorUsersTextFontAssets,
                        R.styleable.TypingIndicatorView_streamUiTypingIndicatorUsersTextFont
                    )
                    .style(
                        R.styleable.TypingIndicatorView_streamUiTypingIndicatorUsersTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                return TypingIndicatorViewStyle(
                    typingIndicatorAnimationView = typingIndicatorAnimationView,
                    typingIndicatorUsersTextStyle = typingIndicatorUsersTextStyle,
                )
            }
        }
    }
}
