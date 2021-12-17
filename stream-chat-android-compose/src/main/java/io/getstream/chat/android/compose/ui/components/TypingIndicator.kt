package io.getstream.chat.android.compose.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
 * Represents a simple typing indicator that consists of three animated dots.
 *
 * @param modifier Modifier for styling.
 */
@Composable
public fun TypingIndicator(modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        TypingIndicatorAnimatedDot(0 * DotAnimationDurationMillis)
        TypingIndicatorAnimatedDot(1 * DotAnimationDurationMillis)
        TypingIndicatorAnimatedDot(2 * DotAnimationDurationMillis)
    }
}

/**
 * Show a dot with infinite alpha animation.
 *
 * @param initialDelayMillis The delay before the animation is started in millis.
 */
@Composable
private fun TypingIndicatorAnimatedDot(
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
private const val DotAnimationDurationMillis = 200
