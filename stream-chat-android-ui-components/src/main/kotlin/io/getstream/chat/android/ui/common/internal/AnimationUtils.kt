package io.getstream.chat.android.ui.common.internal

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.graphics.ColorUtils
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat

private const val HIGHLIGHT_ANIM_DURATION = 3000L

internal fun View.animateHighlight(
    highlightColor: Int = context.getColorCompat(R.color.stream_ui_highlight),
): ValueAnimator {
    val endColor = ColorUtils.setAlphaComponent(highlightColor, 0)
    return ValueAnimator
        .ofObject(ArgbEvaluator(), highlightColor, highlightColor, highlightColor, endColor)
        .apply {
            duration = HIGHLIGHT_ANIM_DURATION
            addUpdateListener {
                setBackgroundColor(it.animatedValue as Int)
            }
            doOnEnd {
                setBackgroundColor(endColor)
            }
            start()
        }
}
