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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.R
import io.getstream.chat.android.ai.compose.ui.component.AIConversationColors

/**
 * [ScrollToLatestButton] at the bottom of the conversation, above what [contentPadding] covers,
 * fading in while [visible].
 */
@Composable
internal fun BoxScope.ScrollToLatestOverlay(
    visible: Boolean,
    contentPadding: PaddingValues,
    onClick: () -> Unit,
    colors: AIConversationColors,
    icon: Painter,
) {
    val layoutDirection = LocalLayoutDirection.current
    AnimatedVisibility(
        visible = visible,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(
                start = contentPadding.calculateStartPadding(layoutDirection),
                end = contentPadding.calculateEndPadding(layoutDirection),
                bottom = contentPadding.calculateBottomPadding() + 8.dp,
            ),
        enter = fadeIn(tween(FadeMillis)) + scaleIn(tween(FadeMillis), initialScale = 0.8f),
        exit = fadeOut(tween(FadeMillis)) + scaleOut(tween(FadeMillis), targetScale = 0.8f),
    ) {
        ScrollToLatestButton(onClick = onClick, colors = colors, icon = icon)
    }
}

/** A round button over the bottom of the conversation that brings its end back into view. */
@Composable
internal fun ScrollToLatestButton(
    onClick: () -> Unit,
    colors: AIConversationColors,
    icon: Painter,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.minimumInteractiveComponentSize(), contentAlignment = Alignment.Center) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = colors.scrollButtonBackground,
            contentColor = colors.scrollButtonIcon,
            border = BorderStroke(1.dp, colors.scrollButtonBorder),
            shadowElevation = 2.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = icon,
                    contentDescription = stringResource(R.string.stream_ai_compose_conversation_scroll_to_latest),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

private const val FadeMillis = 200
