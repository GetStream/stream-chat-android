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

/**
 * The bounce of a tap: a dip to 93% and an overshoot to 103% before settling, over 340 ms.
 *
 * These are authored keyframes rather than a spring: the ease-out curve times the whole run and
 * the keyframes are linear between their stops.
 */
internal class VoiceVisualizationBounce {
    private var age = Float.POSITIVE_INFINITY

    fun start() {
        age = 0f
    }

    fun advance(seconds: Float) {
        age += seconds
    }

    val scale: Float
        get() = if (age >= DURATION) 1f else scaleAt(easeOut(age.coerceAtLeast(0f) / DURATION))

    internal companion object {
        const val DURATION = 0.34f

        private val offsets = floatArrayOf(0f, 0.3f, 0.62f, 1f)
        private val scales = floatArrayOf(1f, 0.93f, 1.03f, 1f)

        fun scaleAt(progress: Float): Float {
            for (i in 1 until offsets.size) {
                if (progress <= offsets[i]) {
                    val fraction = (progress - offsets[i - 1]) / (offsets[i] - offsets[i - 1])
                    return scales[i - 1] + (scales[i] - scales[i - 1]) * fraction
                }
            }
            return 1f
        }

        /** CSS `ease-out`, `cubic-bezier(0, 0, 0.58, 1)`. */
        @Suppress("MagicNumber")
        fun easeOut(x: Float): Float {
            val x2 = 0.58f
            val cx = 0f
            val bx = 3 * x2 - cx
            val ax = 1 - cx - bx
            val cy = 0f
            val by = 3f - cy
            val ay = 1 - cy - by
            fun sampleX(t: Float) = ((ax * t + bx) * t + cx) * t
            fun sampleY(t: Float) = ((ay * t + by) * t + cy) * t
            var low = 0f
            var high = 1f
            repeat(24) {
                val middle = (low + high) / 2
                if (sampleX(middle) < x) low = middle else high = middle
            }
            return sampleY((low + high) / 2)
        }
    }
}
