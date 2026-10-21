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

@file:Suppress("MagicNumber")

package io.getstream.chat.android.ai.compose.ui.component.internal

import io.getstream.chat.android.ai.compose.ui.component.AIVoiceAgentState
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * The motion of a voice visualization: where it is in time, how it moves, and its colors.
 *
 * Each value eases toward the state's motion by a fixed share per frame at 60 Hz, converted to
 * time, so it moves the same at any refresh rate. The shaders and the Canvas drawing read the same
 * values, so the two renderers move alike.
 */
internal class VoiceVisualizationDriver(input: Input) {

    internal data class Input(
        val state: AIVoiceAgentState,
        /** One to five colors. */
        val colors: List<Hsv>,
        /** From 0 to 2. */
        val expressivity: Float,
        /** From 0 to 1, or null when no live audio drives the motion. */
        val audioLevel: Float?,
        val reduceMotion: Boolean,
    )

    var input: Input = input
        set(value) {
            field = value
            if (value.reduceMotion) isHovering = false
        }

    private var speed: Float
    var bright: Float
        private set
    var sat: Float
        private set
    var load: Float
        private set
    var flow: Float
        private set
    private var baseReact: Float
    var expressivity: Float
        private set
    var time: Float = 0f
        private set
    var flowSpin: Float = 0f
        private set
    var orbSpin: Float = 0f
        private set
    var count: Float
        private set
    var tapX: Float = 0f
        private set
    var tapY: Float = 0f
        private set
    var tapAge: Float = NO_TAP
        private set
    var hoverX: Float = 0f
        private set
    var hoverY: Float = 0f
        private set
    var hoverAmount: Float = 0f
        private set
    var mic: Float = 0f
        private set
    var voice: Float = 0f
        private set

    private val colors: Array<Hsv>
    private var isHovering = false
    private val bounce = VoiceVisualizationBounce()

    // Starts settled on the input, so the first frame shows it rather than easing into it.
    init {
        val motion = input.state.motion
        val scales = scales(input.expressivity)
        speed = motion.speed * scales.speed
        bright = motion.bright
        sat = motion.sat
        load = motion.load * scales.amplitude
        flow = motion.flow * scales.amplitude
        baseReact = motion.react * scales.amplitude
        expressivity = input.expressivity
        colors = slots(input.colors).toTypedArray()
        count = input.colors.size.toFloat()
    }

    /** Live audio lifts the motion to its level, whatever the state. */
    val react: Float get() = baseReact + (max(baseReact, mic) - baseReact) * voice

    /** The scale of a tap's bounce. */
    val scale: Float get() = bounce.scale

    /** Color slot [index], 0 to 4; past the palette the slots repeat its first color. */
    fun color(index: Int): Hsv = colors[index]

    fun tap(x: Float, y: Float) {
        if (input.reduceMotion) return
        tapX = x
        tapY = y
        tapAge = 0f
        bounce.start()
    }

    /** Where a pointer hovers, or null once it has left. */
    fun hover(x: Float?, y: Float?) {
        if (x == null || y == null || input.reduceMotion) {
            isHovering = false
            return
        }
        hoverX = x
        hoverY = y
        isHovering = true
    }

    fun advance(seconds: Float) {
        // Clamped so a frame after a stall cannot leap the motion.
        val dt = min(max(seconds, 0f), MAX_FRAME)
        val motion = input.state.motion
        val scales = scales(input.expressivity)
        val drivers = ease(DRIVERS, dt)
        speed += (motion.speed * scales.speed - speed) * drivers
        bright += (motion.bright - bright) * drivers
        sat += (motion.sat - sat) * drivers
        load += (motion.load * scales.amplitude - load) * drivers
        flow += (motion.flow * scales.amplitude - flow) * drivers
        baseReact += (motion.react * scales.amplitude - baseReact) * drivers
        expressivity += (input.expressivity - expressivity) * drivers
        // Integrated rather than computed as time times a weight, so a change of state eases the
        // turns instead of jumping their angles.
        val step = dt * (if (input.reduceMotion) REDUCED else 1f) * speed
        time += step
        flowSpin += step * flow
        orbSpin += step * (1 + 0.5f * flow + 0.1f * baseReact)
        tapAge += dt
        bounce.advance(dt)
        val gentle = ease(GENTLE, dt)
        hoverAmount += ((if (isHovering) 1f else 0f) - hoverAmount) * gentle
        voice += ((if (input.audioLevel == null) 0f else 1f) - voice) * ease(VOICE, dt)
        // A meter's level jumps with every syllable, so it is followed like a VU meter: quickly
        // up to a louder reading, slowly down from it, and the motion swells with the phrase.
        val level = input.audioLevel ?: 0f
        mic += (level - mic) * ease(if (level > mic) MIC_RISE else MIC_FALL, dt)
        val targets = slots(input.colors)
        for (index in colors.indices) colors[index] = ease(colors[index], targets[index], gentle)
        count += (input.colors.size - count) * gentle
    }

    internal data class Scales(val amplitude: Float, val speed: Float)

    internal companion object {
        const val NO_TAP = 100f
        private const val MAX_FRAME = 0.05f
        private const val REDUCED = 0.07f
        private const val DRIVERS = 0.08f
        private const val GENTLE = 0.12f
        private const val VOICE = 0.1f
        private const val MIC_RISE = 0.16f
        private const val MIC_FALL = 0.05f

        /** Expressivity scales amplitude and speed, each with a floor so the motion breathes at 0. */
        fun scales(expressivity: Float) =
            Scales(amplitude = 0.15f + 0.85f * expressivity, speed = 0.6f + 0.4f * expressivity)

        /**
         * The share of the way a value moves in [dt] seconds when it moves [perFrame] of the way
         * each frame at 60 Hz.
         */
        fun ease(perFrame: Float, dt: Float): Float = 1f - (1f - perFrame).pow(dt * 60f)

        /** Eases an HSV color, its hue taking the shorter way round the wheel. */
        fun ease(color: Hsv, target: Hsv, fraction: Float): Hsv {
            val hueStep = ((target.hue - color.hue + 1.5f).rem(1f) - 0.5f) * fraction
            return Hsv(
                hue = (color.hue + hueStep + 1f).rem(1f),
                saturation = color.saturation + (target.saturation - color.saturation) * fraction,
                value = color.value + (target.value - color.value) * fraction,
            )
        }

        /** The five color slots the renderers read; past the palette they repeat its first color. */
        fun slots(colors: List<Hsv>): List<Hsv> {
            val first = colors.firstOrNull() ?: Hsv(0f, 0f, 0f)
            return List(VoiceVisualizationPalette.MAXIMUM_COUNT) { colors.getOrElse(it) { first } }
        }
    }
}

/**
 * How a state moves. `load`, `flow` and `react` each select a motion pattern, one per state, so
 * easing between states crossfades the patterns.
 */
internal data class VoiceMotion(
    val speed: Float,
    val bright: Float,
    val sat: Float,
    val load: Float,
    val flow: Float,
    val react: Float,
)

internal val AIVoiceAgentState.motion: VoiceMotion
    get() = when (this) {
        AIVoiceAgentState.Idle -> motion(speed = 0.4f, react = 0.12f)
        AIVoiceAgentState.Connecting -> motion(speed = 1f, load = 1f)
        AIVoiceAgentState.Listening -> motion(speed = 0.9f, react = 0.45f)
        AIVoiceAgentState.Thinking -> motion(speed = 1.25f, flow = 1f)
        AIVoiceAgentState.Speaking -> motion(speed = 1.25f, react = 1f)
    }

private fun motion(speed: Float, load: Float = 0f, flow: Float = 0f, react: Float = 0f) =
    VoiceMotion(speed = speed, bright = 1f, sat = 1f, load = load, flow = flow, react = react)
