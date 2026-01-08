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

package io.getstream.chat.android.ai.assistant

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import io.getstream.chat.android.compose.ui.theme.ChatTheme

private const val delayUnit = 200

/**
 * A typing indicator that reflects the current states of the [TypingState].
 *
 * @param modifier The modifier to be applied to this composable.
 * @param text A text that represents the current state of the [TypingState].
 * @param textStyle A test style that will be applied to the Text.
 */
@Composable
public fun AiTypingIndicator(
    modifier: Modifier = Modifier,
    text: String,
    textStyle: TextStyle = TextStyle(
        fontSize = 16.sp,
        color = ChatTheme.colors.textHighEmphasis,
    ),
) {
    val scale1 by animateScaleWithDelay(0)
    val scale2 by animateScaleWithDelay(delayUnit)
    val scale3 by animateScaleWithDelay(delayUnit * 2)
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.View)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.shimmer(shimmerInstance),
            text = text,
            style = textStyle,
        )
        Spacer(modifier = Modifier.width(4.dp))
        SingleDot(scale = scale1, shimmerInstance = shimmerInstance)
        SingleDot(scale = scale2, shimmerInstance = shimmerInstance)
        SingleDot(scale = scale3, shimmerInstance = shimmerInstance)
    }
}

@Composable
private fun SingleDot(
    scale: Float,
    shimmerInstance: Shimmer,
) {
    Spacer(
        Modifier
            .size(11.dp)
            .scale(scale.coerceAtLeast(0.55f))
            .shimmer(shimmerInstance)
            .background(
                color = ChatTheme.colors.textLowEmphasis,
                shape = CircleShape,
            ),
    )
}

@Composable
private fun animateScaleWithDelay(delay: Int): State<Float> {
    val infiniteTransition = rememberInfiniteTransition()
    return infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = delayUnit * 4
                0f at delay using LinearEasing
                1f at delay + delayUnit using LinearEasing
                0f at delay + delayUnit * 2
            },
        ),
    )
}
