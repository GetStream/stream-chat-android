package io.getstream.chat.android.compose.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import kotlinx.coroutines.delay

/**
 * Show a dot with infinite alpha animation.
 *
 * @param initialDelayMillis The delay before the animation is started in millis.
 */
@Composable
public fun TypingIndicatorAnimatedDot(
    initialDelayMillis: Int,
) {
    val alpha = remember { Animatable(0.5f) }

    LaunchedEffect(initialDelayMillis) {
        delay(initialDelayMillis.toLong())
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = DotAnimationDurationMillis,
                    delayMillis = DotAnimationDurationMillis,
                ),
                repeatMode = RepeatMode.Reverse,
            ),
        )
    }

    val color: Color = ChatTheme.colors.textLowEmphasis.copy(alpha = alpha.value)

    Box(
        Modifier
            .background(color, CircleShape)
            .size(5.dp)
    )
}

/**
 * The animation duration of each dot.
 */
public const val DotAnimationDurationMillis: Int = 200
