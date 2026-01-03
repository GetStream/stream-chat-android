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

package io.getstream.chat.android.ui.common.internal

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.graphics.ColorUtils
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.extensions.getColorCompat

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
