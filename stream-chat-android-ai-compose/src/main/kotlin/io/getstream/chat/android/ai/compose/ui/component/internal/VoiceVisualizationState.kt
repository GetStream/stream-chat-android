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

package io.getstream.chat.android.ai.compose.ui.component.internal

import android.content.ContentResolver
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.LongState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.IntSize
import io.getstream.chat.android.ai.compose.R
import io.getstream.chat.android.ai.compose.ui.component.AIVoiceAgentState
import io.getstream.chat.android.ai.compose.ui.component.AIVoiceVisualizationDefaults
import kotlin.math.min

/**
 * A voice visualization's motion, advanced once a frame.
 *
 * Reading [frame] in a draw lambda redraws it each frame without recomposing. [audioLevel] is read
 * in the frame loop, so a level changing many times a second recomposes nothing either.
 */
internal class VoiceVisualizationState(val driver: VoiceVisualizationDriver, val frame: LongState)

@Composable
internal fun rememberVoiceVisualization(
    state: AIVoiceAgentState,
    colors: List<Color>,
    expressivity: Float,
    audioLevel: (() -> Float?)?,
): VoiceVisualizationState {
    val reduceMotion by rememberAnimationsOff()
    val fallback = AIVoiceVisualizationDefaults.colors()
    val palette = remember(colors, fallback) { VoiceVisualizationPalette.hsv(colors.ifEmpty { fallback }) }
    fun input(level: Float?) = VoiceVisualizationDriver.Input(
        state = state,
        colors = palette,
        expressivity = expressivityInput(expressivity),
        audioLevel = audioLevelInput(level),
        reduceMotion = reduceMotion,
    )
    val driver = remember { VoiceVisualizationDriver(input(audioLevel?.invoke())) }
    val level = rememberUpdatedState(audioLevel)
    val frame = remember { mutableLongStateOf(0L) }
    SideEffect { driver.input = input(driver.input.audioLevel) }
    // A preview or a snapshot shows the first frame and does not run the clock.
    if (!LocalInspectionMode.current) {
        LaunchedEffect(driver) {
            var last = 0L
            while (true) {
                withFrameNanos { now ->
                    if (last != 0L) {
                        val current = driver.input
                        driver.input = current.copy(audioLevel = audioLevelInput(level.value?.invoke()))
                        driver.advance((now - last) / NANOS)
                    }
                    last = now
                    frame.longValue++
                }
            }
        }
    }
    return remember(driver) { VoiceVisualizationState(driver, frame) }
}

/** [expressivity] from 0 to 2; one that is not a number gets the default, 1. */
internal fun expressivityInput(expressivity: Float): Float =
    if (expressivity.isFinite()) expressivity.coerceIn(0f, 2f) else 1f

/** [level] from 0 to 1; one that is not a number counts as no live audio, so it can't stall the motion. */
internal fun audioLevelInput(level: Float?): Float? = level?.takeIf { it.isFinite() }?.coerceIn(0f, 1f)

/** Whether the system's animations are off, following the setting while the screen is shown. */
@Composable
private fun rememberAnimationsOff(): State<Boolean> {
    val context = LocalContext.current
    val resolver = context.contentResolver
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

/** Whether to draw for a dark background, from the surface the visualization sits on. */
internal fun Color.isDark() = luminance() < DarkLuminance

/**
 * Names the visualization and its state for accessibility, bounces it where it is tapped inside
 * [contains], and ripples it under a hovering mouse or stylus. It consumes no pointer events, so a
 * clickable on the view or a parent works as usual. [contains] takes the point in the
 * renderers' coordinates: centred, normalized to the shorter side, y up.
 */
@Composable
internal fun Modifier.voiceVisualization(
    state: AIVoiceAgentState,
    visualization: VoiceVisualizationState,
    contains: (x: Float, y: Float) -> Boolean,
): Modifier {
    val label = stringResource(R.string.stream_ai_compose_voice_agent)
    val value = stringResource(state.description)
    return semantics {
        contentDescription = label
        stateDescription = value
    }.pointerInput(visualization) {
        // Watches taps without consuming them, so a clickable on the view or a parent still gets them.
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            val up = waitForUpOrCancellation() ?: return@awaitEachGesture
            val x = normalizedX(up.position.x, size)
            val y = normalizedY(up.position.y, size)
            if (contains(x, y)) visualization.driver.tap(x, y)
        }
    }.pointerInput(visualization) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull() ?: continue
                val x = normalizedX(change.position.x, size)
                val y = normalizedY(change.position.y, size)
                // Enter and Move without a press only come from a hovering mouse or stylus.
                val moving = event.type == PointerEventType.Enter || event.type == PointerEventType.Move
                val hovering = moving && !change.pressed && contains(x, y)
                if (hovering) visualization.driver.hover(x, y) else visualization.driver.hover(null, null)
            }
        }
    }
}

private fun normalizedX(x: Float, size: IntSize) = (x - size.width / 2f) / min(size.width, size.height)

private fun normalizedY(y: Float, size: IntSize) = (size.height / 2f - y) / min(size.width, size.height)

private val AIVoiceAgentState.description: Int
    get() = when (this) {
        AIVoiceAgentState.Idle -> R.string.stream_ai_compose_voice_state_idle
        AIVoiceAgentState.Connecting -> R.string.stream_ai_compose_voice_state_connecting
        AIVoiceAgentState.Listening -> R.string.stream_ai_compose_voice_state_listening
        AIVoiceAgentState.Thinking -> R.string.stream_ai_compose_voice_state_thinking
        AIVoiceAgentState.Speaking -> R.string.stream_ai_compose_voice_state_speaking
    }

private const val NANOS = 1_000_000_000f
private const val DarkLuminance = 0.5f
