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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.ai.compose.ui.component.internal.RichText
import kotlinx.coroutines.delay

/**
 * A composable that wraps content and provides streaming animation.
 * Progressively reveals text content word-by-word and whitespace chunk-by-chunk.
 *
 * Behavior when [animate] is true:
 * - Every change to the text animates progressively
 * - If the change is new text (doesn't start with previous), animates from the beginning
 * - If the change is a continuation (next text starts with previous text), continues from current position
 *
 * Behavior when [animate] is false:
 * - Displays full text immediately
 * - If transitioning from true to false mid-animation, completes the current animation first
 *
 * @param text The full text content to display
 * @param animate Whether to animate the text reveal. If false, displays full text immediately.
 * @param chunkDelayMs Delay in milliseconds between each chunk reveal. Lower values result in faster animation.
 * @param content The composable content that will receive the animated text as [displayedText]
 */
@Composable
public fun StreamingText(
    text: String,
    animate: Boolean = true,
    chunkDelayMs: Long = 30,
    content: @Composable (displayedText: String) -> Unit = { displayedText ->
        RichText(text = displayedText)
    },
) {
    // Track the displayed text for animation. When not animating, start with the full text so
    // the first frame shows it immediately instead of flashing empty before the effect runs.
    var displayedText by remember { mutableStateOf(if (animate) "" else text) }
    // Track the previous full text to detect new text vs continuation
    var previousText by remember { mutableStateOf("") }
    // Track previous animate value to detect transition from true to false
    var previousAnimate by remember { mutableStateOf(animate) }

    LaunchedEffect(text, animate) {
        val animateChangedFromTrueToFalse = previousAnimate && !animate
        previousAnimate = animate

        if (!animate) {
            handleNonAnimatedState(
                animateChangedFromTrueToFalse = animateChangedFromTrueToFalse,
                displayedText = displayedText,
                text = text,
                chunkDelayMs = chunkDelayMs,
                onTextUpdate = { displayedText = it },
                onPreviousTextUpdate = { previousText = it },
            )
        } else {
            handleAnimatedState(
                text = text,
                previousText = previousText,
                displayedText = displayedText,
                chunkDelayMs = chunkDelayMs,
                onTextUpdate = { displayedText = it },
                onPreviousTextUpdate = { previousText = it },
            )
        }
    }

    content(displayedText)
}

// Handles non-animated state. If transitioning from animated to non-animated mid-animation,
// completes the current animation first; otherwise displays full text immediately.
@Suppress("LongParameterList")
private suspend fun handleNonAnimatedState(
    animateChangedFromTrueToFalse: Boolean,
    displayedText: String,
    text: String,
    chunkDelayMs: Long,
    onTextUpdate: (String) -> Unit,
    onPreviousTextUpdate: (String) -> Unit,
) {
    if (animateChangedFromTrueToFalse && displayedText.length < text.length) {
        // Changed from true to false - continue animating until full text is displayed
        onPreviousTextUpdate(text)
        animateNewContent(displayedText, text, chunkDelayMs, onTextUpdate)
    } else {
        // Initial animate is false or already complete - display immediately
        onTextUpdate(text)
        onPreviousTextUpdate(text)
    }
}

// Handles animated state: resets for new text, continues for continuation, or updates tracking only.
@Suppress("LongParameterList")
private suspend fun handleAnimatedState(
    text: String,
    previousText: String,
    displayedText: String,
    chunkDelayMs: Long,
    onTextUpdate: (String) -> Unit,
    onPreviousTextUpdate: (String) -> Unit,
) {
    when {
        text.isEmpty() -> {
            onTextUpdate("")
            onPreviousTextUpdate(text)
        }
        previousText.isEmpty() || !text.startsWith(previousText) -> {
            // New text - reset and animate from the beginning
            onTextUpdate("")
            onPreviousTextUpdate(text)
            animateNewContent("", text, chunkDelayMs, onTextUpdate)
        }
        text.length > previousText.length -> {
            // Continuation - keep animating from where we left off
            onPreviousTextUpdate(text)
            animateNewContent(displayedText, text, chunkDelayMs, onTextUpdate)
        }
        // Text hasn't changed
        else -> onPreviousTextUpdate(text)
    }
}

// Progressively reveals chunks from currentText to fullText with the specified delay.
private suspend fun animateNewContent(
    currentText: String,
    fullText: String,
    chunkDelayMs: Long,
    onUpdate: (String) -> Unit,
) {
    val newContent = fullText.substring(currentText.length)
    val chunks = splitIntoWords(newContent)

    // Use StringBuilder for efficient string concatenation
    val builder = StringBuilder(currentText)
    for (chunk in chunks) {
        builder.append(chunk)
        onUpdate(builder.toString())
        delay(chunkDelayMs)
    }
}

// Splits text into words and whitespace chunks, preserving line breaks.
private fun splitIntoWords(text: String): List<String> {
    if (text.isEmpty()) {
        return emptyList()
    }

    val chunks = mutableListOf<String>()
    val lines = text.split('\n')

    for ((index, line) in lines.withIndex()) {
        // Split into words and whitespace chunks
        val words = WordSplitRegex.findAll(line).map { it.value }.filter { it.isNotEmpty() }
        chunks.addAll(words)
        // Preserve newline if not the last line
        if (index < lines.size - 1) {
            chunks.add("\n")
        }
    }

    return chunks
}

// Regex to match whitespace or non-whitespace sequences for chunking.
private val WordSplitRegex = Regex("""(\s+|\S+)""")

@Composable
internal fun StreamingTextRendered() {
    StreamingText(
        text = "Hello, I am the Stream AI assistant. How can I help you today?",
        animate = false,
    )
}

@Preview(showBackground = true)
@Composable
private fun StreamingTextPreview() {
    StreamingTextRendered()
}
