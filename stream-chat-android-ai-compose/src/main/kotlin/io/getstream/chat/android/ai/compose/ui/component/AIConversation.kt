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

package io.getstream.chat.android.ai.compose.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.ui.component.internal.AIConversationScroll
import io.getstream.chat.android.ai.compose.ui.component.internal.AIMessageTurn
import io.getstream.chat.android.ai.compose.ui.component.internal.AIThinkingClock
import io.getstream.chat.android.ai.compose.ui.component.internal.AIThinkingTurn
import io.getstream.chat.android.ai.compose.ui.component.internal.LocalAIThinkingTurn
import io.getstream.chat.android.ai.compose.ui.component.internal.OlderMessagesLoading
import io.getstream.chat.android.ai.compose.ui.component.internal.ScrollToLatestButton
import io.getstream.chat.android.ai.compose.ui.component.internal.conversationScroll
import io.getstream.chat.android.ai.compose.ui.component.internal.distanceToEnd
import io.getstream.chat.android.ai.compose.ui.component.internal.messageTurns
import io.getstream.chat.android.ai.compose.ui.component.internal.scrollToEnd
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * The messages of a conversation with an AI agent, oldest first, shown your own way.
 *
 * ```
 * AIConversation(
 *     messages = messages,
 *     key = { it.id },
 *     isFromUser = { it.isMine },
 *     isThinking = isWaitingForReply,
 *     footer = { error?.let { RetryButton(it) } },
 *     emptyContent = { Text("How can I help?") },
 * ) { message ->
 *     MessageRow(message)
 * }
 * ```
 *
 * Each message the person sends starts a turn: that message and the replies after it. The latest
 * turn is at least as tall as the list, so when the person sends a message, the list scrolls it to
 * the top and the reply grows below it without moving anything on screen. A conversation that
 * loads, or replaces the one shown, opens at its end, and a button brings the end back into view
 * whenever it is out of it.
 *
 * Place the composer below the list, so the list knows how much of it is visible.
 *
 * @param messages The conversation, oldest message first.
 * @param key A stable, unique key for a message. A turn is known by its first message's key, so
 * what is on screen stays in place as messages come and go around it. The key must be saveable in
 * a Bundle, as with any lazy list key.
 * @param isFromUser Whether the person using the app sent a message. Each of their messages starts
 * a turn.
 * @param modifier The modifier to apply to the list.
 * @param isThinking Whether the agent is answering the latest message and its reply shows nothing
 * yet. Meanwhile the latest turn ends with [AIThinking].
 * @param loadOlderMessages Loads the messages before the first one, which the list asks for as the
 * person scrolls to its start. It shows a spinner meanwhile, and what is on screen stays in place
 * as they arrive: the message that was first keeps starting a turn, even when the person's message
 * before it arrives. Pass `null` once there are none left.
 * @param footer Content under the latest turn's messages, such as what the agent is doing, or an
 * error with a way to retry.
 * @param emptyContent What the list shows while there are no messages, centered in it.
 * @param colors The palette of the list's own views: the button that brings the end back into view,
 * and the spinner while older messages load.
 * @param messageContent The view of a message.
 */
@Suppress("LongMethod", "LongParameterList")
@Composable
public fun <T> AIConversation(
    messages: List<T>,
    key: (T) -> Any,
    isFromUser: (T) -> Boolean,
    modifier: Modifier = Modifier,
    isThinking: Boolean = false,
    loadOlderMessages: (suspend () -> Unit)? = null,
    footer: @Composable () -> Unit = {},
    emptyContent: @Composable () -> Unit = {},
    colors: AIConversationColors = AIConversationDefaults.colors(),
    messageContent: @Composable (T) -> Unit,
) {
    // The first message keeps starting a turn as older messages arrive above it, so the turns on
    // screen keep their keys and stay in place.
    val turnStarts = rememberSaveable(saver = TurnStartsSaver) { mutableSetOf() }
    val turns = remember(messages, key, isFromUser) { messageTurns(messages, key, isFromUser, turnStarts) }
    val firstTurnId = turns.firstOrNull()?.id
    SideEffect { firstTurnId?.let { turnStarts += it } }
    val lastTurn = turns.lastOrNull()
    val clock = rememberSaveable(saver = AIThinkingClock.Saver) { AIThinkingClock() }
    // Repeats harmlessly on every composition: a turn's thinking starts and ends once.
    clock.update(
        latestTurn = lastTurn?.id,
        isThinking = isThinking,
        hasReplies = (lastTurn?.messages?.size ?: 0) > 1,
    )

    val loadsOlder = loadOlderMessages != null && turns.isNotEmpty()
    // The index of the first turn's item, after the spinner while older messages can load.
    val turnsStart = if (loadsOlder) 1 else 0
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = turnsStart + turns.lastIndex.coerceAtLeast(0))

    KeepsTurnsInPlace(listState, firstTurnId, loadsOlder, turns, turnsStart)
    ScrollsOnNewLastTurn(listState, turns, turnsStart)
    LoadsOlderMessages(listState, firstTurnId, loadOlderMessages.takeIf { loadsOlder })

    val slack = with(LocalDensity.current) { EndSlack.roundToPx() }
    val endInView by remember(listState, slack) {
        derivedStateOf { listState.layoutInfo.distanceToEnd() <= slack }
    }
    val scope = rememberCoroutineScope()

    BoxWithConstraints(modifier = modifier) {
        val viewportHeight = if (constraints.hasBoundedHeight) maxHeight else 0.dp
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(bottom = if (turns.isEmpty()) 0.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (turns.isEmpty()) {
                item(key = EmptyContentKey, contentType = EmptyContentKey) {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        emptyContent()
                    }
                }
            } else {
                if (loadsOlder) {
                    item(key = OlderMessagesKey, contentType = OlderMessagesKey) {
                        OlderMessagesLoading(
                            color = colors.loadingIndicator,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
                items(items = turns, key = { it.id }, contentType = { TurnContentType }) { turn ->
                    val isLatest = turn.id == lastTurn?.id
                    val thinkingTurn = remember(clock, turn.id) { AIThinkingTurn(clock, turn.id) }
                    CompositionLocalProvider(LocalAIThinkingTurn provides thinkingTurn) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = if (isLatest) viewportHeight else 0.dp)
                                .padding(start = 16.dp, top = 16.dp, end = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            turn.messages.forEach { message ->
                                key(key(message)) { messageContent(message) }
                            }
                            if (isLatest) {
                                if (isThinking) AIThinking()
                                footer()
                            }
                        }
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = turns.isNotEmpty() && !endInView,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            enter = fadeIn(tween(ButtonFadeMillis)) + scaleIn(tween(ButtonFadeMillis), initialScale = 0.8f),
            exit = fadeOut(tween(ButtonFadeMillis)) + scaleOut(tween(ButtonFadeMillis), targetScale = 0.8f),
        ) {
            ScrollToLatestButton(
                onClick = { scope.launch { listState.scrollToEnd(animated = true) } },
                colors = colors,
            )
        }
    }
}

/**
 * The palette of [AIConversation]'s own views.
 *
 * @param scrollButtonBackground Color of the button that brings the end of the conversation back
 * into view.
 * @param scrollButtonBorder Color of the outline of that button.
 * @param scrollButtonIcon Color of that button's arrow.
 * @param loadingIndicator Color of the spinner while older messages load.
 */
@Immutable
public data class AIConversationColors(
    val scrollButtonBackground: Color,
    val scrollButtonBorder: Color,
    val scrollButtonIcon: Color,
    val loadingIndicator: Color,
)

/** Defaults of [AIConversation]. */
public object AIConversationDefaults {

    /**
     * The default palette, from the current [MaterialTheme].
     *
     * @param scrollButtonBackground Color of the button that brings the end of the conversation
     * back into view.
     * @param scrollButtonBorder Color of the outline of that button.
     * @param scrollButtonIcon Color of that button's arrow.
     * @param loadingIndicator Color of the spinner while older messages load.
     */
    @Composable
    public fun colors(
        scrollButtonBackground: Color = MaterialTheme.colorScheme.surface,
        scrollButtonBorder: Color = MaterialTheme.colorScheme.outlineVariant,
        scrollButtonIcon: Color = MaterialTheme.colorScheme.onSurface,
        loadingIndicator: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    ): AIConversationColors = AIConversationColors(
        scrollButtonBackground = scrollButtonBackground,
        scrollButtonBorder = scrollButtonBorder,
        scrollButtonIcon = scrollButtonIcon,
        loadingIndicator = loadingIndicator,
    )
}

/**
 * Keeps the first turn where it is on screen when what is above it changes while the spinner is
 * the first item in view: older turns arriving below the spinner, or the spinner going away. The
 * list keeps its first visible item in place by itself, which here is the spinner, so the turns
 * under it would be pushed down or pulled up.
 */
@Composable
private fun KeepsTurnsInPlace(
    listState: LazyListState,
    firstTurnId: Any?,
    loadsOlder: Boolean,
    turns: List<AIMessageTurn<*>>,
    turnsStart: Int,
) {
    val above = remember { AboveTurns(firstTurnId, loadsOlder) }
    // Runs before the list measures the new turns, while its layout still shows the previous ones.
    SideEffect {
        if (above.firstTurnId == firstTurnId && above.loadsOlder == loadsOlder) return@SideEffect
        val previousFirstTurnId = above.firstTurnId
        above.firstTurnId = firstTurnId
        above.loadsOlder = loadsOlder
        val visible = listState.layoutInfo.visibleItemsInfo
        if (visible.firstOrNull()?.key != OlderMessagesKey) return@SideEffect
        val held = visible.firstOrNull { it.key == previousFirstTurnId } ?: return@SideEffect
        val index = turns.indexOfFirst { it.id == previousFirstTurnId }
        if (index >= 0) listState.requestScrollToItem(turnsStart + index, scrollOffset = -held.offset)
    }
}

/**
 * Scrolls when the last turn changes: a turn the person just started goes to the top of the list,
 * and a conversation that loads or is replaced shows its end.
 */
@Composable
private fun ScrollsOnNewLastTurn(listState: LazyListState, turns: List<AIMessageTurn<*>>, turnsStart: Int) {
    // Saved with the list's position, which a restored list keeps rather than showing its end.
    var shownId by rememberSaveable { mutableStateOf<Any?>(null) }
    val lastTurnId = turns.lastOrNull()?.id
    LaunchedEffect(listState, lastTurnId) {
        val scroll = conversationScroll(shownId, turns.map { it.id })
        shownId = lastTurnId
        if (scroll == AIConversationScroll.None) return@LaunchedEffect
        // Scrolls once the list has laid out the new turns, the latest at least as tall as the list.
        val itemCount = turnsStart + turns.size
        snapshotFlow { listState.layoutInfo.totalItemsCount }.first { it == itemCount }
        when (scroll) {
            is AIConversationScroll.ToTop -> listState.animateScrollToItem(turnsStart + turns.lastIndex)
            AIConversationScroll.ToEnd -> listState.scrollToEnd(animated = false)
            AIConversationScroll.None -> Unit
        }
    }
}

/**
 * Calls [loadOlderMessages] once the spinner has stayed in view a moment, rather than for the
 * frame a list that opens at its end can show its start, and again whenever the first turn
 * changes while it is in view.
 */
@Composable
private fun LoadsOlderMessages(
    listState: LazyListState,
    firstTurnId: Any?,
    loadOlderMessages: (suspend () -> Unit)?,
) {
    val load by rememberUpdatedState(loadOlderMessages)
    val spinnerInView by remember(listState) {
        derivedStateOf {
            val info = listState.layoutInfo
            info.visibleItemsInfo.any {
                it.key == OlderMessagesKey &&
                    it.offset < info.viewportEndOffset &&
                    it.offset + it.size > info.viewportStartOffset
            }
        }
    }
    val loads = loadOlderMessages != null && spinnerInView
    LaunchedEffect(loads, firstTurnId) {
        if (loads) {
            delay(LoadOlderDelayMillis)
            load?.invoke()
        }
    }
}

/** What was above the turns when the list last laid them out. */
private class AboveTurns(var firstTurnId: Any?, var loadsOlder: Boolean)

/** Saves the keys of the messages that start a turn whoever sent them. */
private val TurnStartsSaver = listSaver<MutableSet<Any>, Any>(
    save = { it.toList() },
    restore = { it.toMutableSet() },
)

// Keys of the list's own items, unlikely to match a message's key, and saveable in a Bundle.
private const val OlderMessagesKey = "io.getstream.chat.android.ai.compose.AIConversation.older"
private const val EmptyContentKey = "io.getstream.chat.android.ai.compose.AIConversation.empty"
private const val TurnContentType = "io.getstream.chat.android.ai.compose.AIConversation.turn"

private const val LoadOlderDelayMillis = 300L
private const val ButtonFadeMillis = 200
private val EndSlack = 24.dp
