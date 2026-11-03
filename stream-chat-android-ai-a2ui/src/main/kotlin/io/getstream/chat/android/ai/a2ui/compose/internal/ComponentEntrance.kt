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

package io.getstream.chat.android.ai.a2ui.compose.internal

import android.content.ContentResolver
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * How the components of one surface come in: whether a component that shows now shows together
 * with the one around it, which brings it in; whether the surface is still being written, as only
 * then do components come in on their own; and the cascade they follow.
 */
internal data class EntranceStage(val isCarried: Boolean, val isWritten: Boolean, val cascade: EntranceCascade)

private val LocalEntranceStage = compositionLocalOf<EntranceStage?> { null }

/**
 * The components of one surface. What shows with the surface comes in with it, so a surface that
 * was written before it showed shows at once. While [isWritten], a component that shows later
 * comes in on its own.
 */
@Composable
internal fun SurfaceEntrances(isWritten: Boolean, content: @Composable () -> Unit) {
    val cascade = remember { EntranceCascade() }
    val still = LocalInspectionMode.current
    Shows { hasShown ->
        val stage = EntranceStage(isCarried = !hasShown, isWritten = isWritten && !still, cascade = cascade)
        CompositionLocalProvider(LocalEntranceStage provides stage, content = content)
    }
}

/**
 * One component of a surface. It comes in on its own when it shows after the component around it,
 * while the surface is written: it fades in, rising into place and coming into focus, after the
 * components that came in just before it. What shows with it comes in with it. With the system's
 * animations off, it only fades.
 */
@Composable
internal fun ComponentEntrance(content: @Composable () -> Unit) {
    val stage = LocalEntranceStage.current
    val animates = remember { stage != null && stage.isWritten && !stage.isCarried }
    val progress = remember { Animatable(if (animates) 0f else 1f) }
    val reduceMotion by rememberAnimationsOff()
    if (animates && stage != null) {
        LaunchedEffect(Unit) {
            delay(stage.cascade.delay())
            progress.animateTo(1f, entranceSpec(reduceMotion))
        }
    }
    Shows { hasShown ->
        CompositionLocalProvider(LocalEntranceStage provides stage?.copy(isCarried = !hasShown)) {
            Box(modifier = Modifier.entrance(progress::value, moves = !reduceMotion)) {
                content()
            }
        }
    }
}

// Gives [content] whether it has shown before: false while it first shows.
@Composable
private fun Shows(content: @Composable (hasShown: Boolean) -> Unit) {
    var hasShown by remember { mutableStateOf(false) }
    if (!hasShown) SideEffect { hasShown = true }
    content(hasShown)
}

// How a component looks on its way in: clear, a little lower and smaller, and out of focus.
// Without motion it only fades.
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
        translationY = 0f
        scaleX = 1f
        scaleY = 1f
        renderEffect = null
    }
}

private fun entranceSpec(reduceMotion: Boolean): AnimationSpec<Float> = if (reduceMotion) {
    tween(durationMillis = ReducedEntranceMillis, easing = FastOutSlowInEasing)
} else {
    spring(dampingRatio = EntranceDamping, stiffness = EntranceStiffness)
}

/**
 * Spaces out the components that come in at once, so they follow one another, without keeping
 * any of them waiting long.
 *
 * @param now The time in milliseconds.
 */
internal class EntranceCascade(private val now: () -> Long = SystemClock::uptimeMillis) {
    private var next = 0L

    /** How long, in milliseconds, a component that comes in now waits for the ones before it. */
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

// Whether the system's animations are off, following the setting while it changes.
@Composable
private fun rememberAnimationsOff(): State<Boolean> {
    val resolver = LocalContext.current.contentResolver
    val off = remember(resolver) { mutableStateOf(animationsOff(resolver)) }
    DisposableEffect(resolver) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                off.value = animationsOff(resolver)
            }
        }
        val uri = Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE)
        resolver.registerContentObserver(uri, false, observer)
        onDispose { resolver.unregisterContentObserver(observer) }
    }
    return off
}

private fun animationsOff(resolver: ContentResolver) =
    Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f

private val EntranceRise = 16.dp
private val EntranceBlur = 8.dp
private const val EntranceScale = 0.97f
private val TopCenter = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 0f)
private const val EntranceDamping = 0.88f

// A spring that settles in about 0.7 s.
private const val EntranceStiffness = 80f
private const val ReducedEntranceMillis = 300
