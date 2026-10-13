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

import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The reasoning, one paragraph at a time. It grows with the text up to [maxHeight], then scrolls,
 * fading at the top so it reads as continuing above. While the reasoning is live, new text is
 * revealed smoothly and the newest stays in view unless the reader scrolls up.
 *
 * Paragraphs render lazily and each recomposes only when its own text changes, so as the
 * reasoning grows only the paragraph still being written is laid out again.
 */
@Composable
internal fun ReasoningPanel(
    text: String,
    isLive: Boolean,
    maxHeight: Dp,
    color: Color,
    style: TextStyle,
) {
    val reveal = rememberTextReveal(text, isLive)
    val paragraphs = remember(reveal.shown) { ReasoningParagraph.split(reveal.shown) }
    val listState = rememberLazyListState()
    val overflowing by remember { derivedStateOf { listState.canScrollForward || listState.canScrollBackward } }
    val following = rememberFollowing(listState)
    val live by rememberUpdatedState(isLive)
    var appeared by remember { mutableStateOf(false) }

    // A live trace opens at its newest thoughts and follows them; a finished one opens at its start.
    LaunchedEffect(reveal.shown) {
        val end = paragraphs.size
        val scrolls = if (appeared) following.value else live
        appeared = true
        if (scrolls) listState.scrollToItem(end)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight)
            .fadingTop(overflowing),
        verticalArrangement = Arrangement.spacedBy(PARAGRAPH_SPACING),
    ) {
        items(paragraphs, key = { it.id }) { paragraph ->
            ReasoningParagraphText(text = paragraph.text, color = color, style = style)
        }
        item(key = BottomKey) { Spacer(Modifier.height(1.dp)) }
    }
}

/** Reveals [text] steadily while [isLive], and at once otherwise. */
@Composable
private fun rememberTextReveal(text: String, isLive: Boolean): TextReveal {
    val reveal = remember { TextReveal(initial = text) }
    val live by rememberUpdatedState(isLive)
    LaunchedEffect(text) { reveal.update(text, animated = live) }
    LaunchedEffect(reveal.revealing) {
        while (reveal.revealing) {
            delay(TextReveal.TICK_MILLIS)
            reveal.tick()
        }
    }
    return reveal
}

/**
 * Whether the panel follows new text. Only the reader's own scrolling stops it following: the
 * panel growing, or the list around it moving, never does. It resumes once they are back at the
 * end.
 */
@Composable
private fun rememberFollowing(listState: LazyListState): State<Boolean> {
    val following = remember { mutableStateOf(true) }
    val slack = with(LocalDensity.current) { FOLLOW_SLACK.roundToPx() }
    LaunchedEffect(listState) {
        var dragged = false
        launch {
            listState.interactionSource.interactions.collect { if (it is DragInteraction.Start) dragged = true }
        }
        snapshotFlow { listState.isScrollInProgress }.collect { scrolling ->
            if (!scrolling && dragged) {
                dragged = false
                following.value = listState.isAtEnd(slack)
            }
        }
    }
    return following
}

private fun LazyListState.isAtEnd(slack: Int): Boolean {
    val info = layoutInfo
    val last = info.visibleItemsInfo.lastOrNull() ?: return false
    return last.index == info.totalItemsCount - 1 && last.offset + last.size <= info.viewportEndOffset + slack
}

@Composable
private fun ReasoningParagraphText(text: String, color: Color, style: TextStyle) {
    val annotated = remember(text) { ReasoningParagraph.annotated(text) }
    SelectionContainer {
        Text(
            text = annotated,
            modifier = Modifier.fillMaxWidth(),
            color = color,
            style = style,
        )
    }
}

private fun Modifier.fadingTop(active: Boolean): Modifier = if (!active) {
    this
} else {
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            drawRect(
                brush = Brush.verticalGradient(0f to Color.Transparent, FadeEnd to Color.Black),
                blendMode = BlendMode.DstIn,
            )
        }
}

private val PARAGRAPH_SPACING = 10.dp
private val FOLLOW_SLACK = 24.dp
private const val FadeEnd = 0.14f
private const val BottomKey = "reasoning.bottom"
