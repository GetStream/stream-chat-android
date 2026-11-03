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

package io.getstream.chat.android.ai.compose.generativeui

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.ui.component.internal.rememberAnimationsOff
import kotlinx.coroutines.delay

/**
 * Brings a part of a reply's generated UI in when it shows, such as when the agent writes it: the
 * part fades in, rising into place and coming into focus, after the parts that came in just before
 * it. What shows together with the reply's UI, or with a part around it, comes in with that, so a
 * reply written before it showed shows at once. With the system's animations off, parts only
 * fade.
 *
 * Renderers give it to each part they place, and to every app component. Outside a reply's
 * generated UI it shows [content] as it is.
 *
 * @param modifier The modifier to apply to the part.
 * @param content The part.
 */
@Composable
public fun AIUIEntrance(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val stage = LocalAIUIStage.current
    // A part that shows with the one around it is carried in by it.
    val carried = remember { stage == null || stage.isCarried || stage.isStill }
    Entrance(
        stage = stage,
        animates = !carried,
        modifier = modifier,
        content = content,
    )
}

/**
 * A reply's generated UI as a whole. It comes in when it first shows while the agent writes it,
 * and what shows with it comes in with it. A part that shows after it comes in on its own.
 */
@Composable
internal fun AIUIEntranceStage(isStreaming: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val cascade = remember { AIUICascade() }
    // A snapshot or a preview shows the UI as it ends up.
    val still = LocalInspectionMode.current
    val animates = remember { isStreaming && !still }
    Entrance(
        stage = AIUIStage(isCarried = false, isStill = still, cascade = cascade),
        animates = animates,
        modifier = modifier,
        content = content,
    )
}

@Composable
private fun Entrance(stage: AIUIStage?, animates: Boolean, modifier: Modifier, content: @Composable () -> Unit) {
    var hasShown by remember { mutableStateOf(false) }
    val progress = remember { Animatable(if (animates) 0f else 1f) }
    val reduceMotion by rememberAnimationsOff()
    if (!hasShown) {
        if (animates && stage != null) {
            LaunchedEffect(Unit) {
                val wait = stage.cascade.delay()
                hasShown = true
                delay(wait)
                progress.animateTo(1f, entranceSpec(reduceMotion))
            }
        } else {
            SideEffect { hasShown = true }
        }
    }
    // What shows with the part while it first shows comes in with it.
    CompositionLocalProvider(LocalAIUIStage provides stage?.carrying(!hasShown)) {
        Box(modifier = modifier.entrance(progress::value, moves = !reduceMotion)) {
            content()
        }
    }
}

// How a part looks on its way in: clear, a little lower and smaller, and out of focus. Without
// motion it only fades.
private fun Modifier.entrance(progress: () -> Float, moves: Boolean): Modifier = graphicsLayer {
    val remaining = 1f - progress()
    alpha = 1f - remaining
    if (moves && remaining > 0f) {
        translationY = EntranceRise.toPx() * remaining
        val scale = 1f - (1f - EntranceScale) * remaining
        scaleX = scale
        scaleY = scale
        transformOrigin = TopCenter
        val blur = EntranceBlur.toPx() * remaining
        // Blurs from Android 12; earlier versions skip it.
        renderEffect = if (blur > 0f) BlurEffect(blur, blur) else null
    } else {
        renderEffect = null
    }
}

private fun entranceSpec(reduceMotion: Boolean): AnimationSpec<Float> = if (reduceMotion) {
    tween(durationMillis = ReducedEntranceMillis, easing = FastOutSlowInEasing)
} else {
    spring(dampingRatio = EntranceDamping, stiffness = EntranceStiffness)
}

/**
 * How the parts of a reply's generated UI come in: whether one shows together with a part around
 * it, which brings it in, whether the UI is still, as in a snapshot, and the cascade the parts that
 * come in on their own follow.
 */
internal data class AIUIStage(val isCarried: Boolean, val isStill: Boolean = false, val cascade: AIUICascade) {
    /** The stage of what a part holds, which comes in with the part while it first shows. */
    fun carrying(carries: Boolean): AIUIStage = copy(isCarried = isCarried || carries)
}

internal val LocalAIUIStage = staticCompositionLocalOf<AIUIStage?> { null }

/**
 * Spaces out the parts of a reply's generated UI that come in at once, so they follow one
 * another, without keeping any of them waiting long.
 *
 * @param now The time in milliseconds.
 */
internal class AIUICascade(private val now: () -> Long = SystemClock::uptimeMillis) {
    private var next = 0L

    /** How long, in milliseconds, a part that comes in now waits for the parts that came in before it. */
    fun delay(): Long {
        val now = now()
        val start = minOf(maxOf(now, next), now + LONGEST_WAIT_MILLIS)
        next = start + STEP_MILLIS
        return start - now
    }

    companion object {
        const val STEP_MILLIS = 100L
        const val LONGEST_WAIT_MILLIS = 400L
    }
}

private val EntranceRise = 16.dp
private val EntranceBlur = 8.dp
private const val EntranceScale = 0.97f
private val TopCenter = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 0f)
private const val EntranceDamping = 0.88f

// A spring that settles in about 0.7 s.
private const val EntranceStiffness = 80f
private const val ReducedEntranceMillis = 300
