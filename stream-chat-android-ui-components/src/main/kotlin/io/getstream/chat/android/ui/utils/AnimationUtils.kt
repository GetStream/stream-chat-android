package io.getstream.chat.android.ui.utils

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.view.View
import androidx.core.animation.doOnEnd
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.extensions.getColorCompat

private const val HIGHLIGHT_ANIM_DURATION = 3000L

internal fun View.animateHighlight(
    startColor: Int = context.getColorCompat(R.color.stream_ui_highlight),
    endColor: Int = context.getColorCompat(R.color.stream_ui_transparent_white),
): ValueAnimator {
    return ValueAnimator
        .ofObject(ArgbEvaluator(), startColor, startColor, startColor, endColor)
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
