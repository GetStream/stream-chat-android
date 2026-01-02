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

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import dev.jeziellago.compose.markdowntext.MarkdownText
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.utils.extensions.isMine
import kotlinx.coroutines.delay

internal typealias AnnotationTag = String

/**
 * The tag used to annotate URLs in the message text.
 */
internal const val AnnotationTagUrl: AnnotationTag = "URL"

/**
 * The tag used to annotate emails in the message text.
 */
internal const val AnnotationTagEmail: AnnotationTag = "EMAIL"

@Composable
public fun AiMessageText(
    message: Message,
    currentUser: User?,
    typingState: TypingState,
    modifier: Modifier = Modifier,
    onAnimationState: (Boolean) -> Unit,
    onLongItemClick: (Message) -> Unit,
    onLinkClick: ((Message, String) -> Unit)? = null,
) {
    val context = LocalContext.current

    val completedMessage by rememberSaveable(message) { mutableStateOf(message.text) }
    var displayedText by rememberSaveable { mutableStateOf("") }
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }

    // Launch the effect to update the displayedText value over time
    LaunchedEffect(completedMessage, typingState) {
        if (typingState is TypingState.Nothing) {
            onAnimationState.invoke(false)
            displayedText = completedMessage
        } else if ((typingState is TypingState.Generating && message.id == typingState.messageId) ||
            displayedText != completedMessage
        ) {
            onAnimationState.invoke(true)
            displayedText = completedMessage

            val textLength = completedMessage.length

            while (currentIndex < textLength) {
                currentIndex = minOf(currentIndex + 1, textLength)
                displayedText = completedMessage.substring(startIndex = 0, endIndex = currentIndex)
                delay(10)
            }

            onAnimationState.invoke(false)
        }
    }

    val styledText =
        ChatTheme.messageTextFormatter.format(message.copy(text = displayedText), currentUser)

    val annotations = styledText.getStringAnnotations(0, styledText.lastIndex)

    val style = if (message.isMine(currentUser)) {
        ChatTheme.ownMessageTheme.textStyle
    } else {
        ChatTheme.otherMessageTheme.textStyle
    }

    if (annotations.fastAny { it.tag == AnnotationTagUrl || it.tag == AnnotationTagEmail }) {
        ClickableText(
            modifier = modifier
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = 8.dp,
                    bottom = 8.dp,
                ),
            text = styledText,
            style = style,
            onLongPress = { onLongItemClick(message) },
        ) { position ->
            val targetUrl = annotations.firstOrNull {
                position in it.start..it.end
            }?.item

            if (!targetUrl.isNullOrEmpty()) {
                onLinkClick?.invoke(message, targetUrl) ?: run {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(targetUrl),
                        ),
                    )
                }
            }
        }
    } else {
        val horizontalPadding = 12.dp
        val verticalPadding = 8.dp
        MarkdownText(
            modifier = modifier
                .padding(
                    horizontal = horizontalPadding,
                    vertical = verticalPadding,
                )
                .clipToBounds(),
            markdown = styledText.text,
            style = style,
        )
    }
}

/**
 * A spin-off of a Foundation component that allows calling long press handlers.
 * Contains only one additional parameter.
 *
 * @param onLongPress Handler called on long press.
 *
 * @see androidx.compose.foundation.text.ClickableText
 */
@Composable
private fun ClickableText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    maxLines: Int = Int.MAX_VALUE,
    onLongPress: () -> Unit,
    onClick: (Int) -> Unit,
) {
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val pressIndicator = Modifier.pointerInput(onClick, onLongPress) {
        detectTapGestures(
            onLongPress = { onLongPress() },
            onTap = { pos ->
                layoutResult.value?.let { layoutResult ->
                    onClick(layoutResult.getOffsetForPosition(pos))
                }
            },
        )
    }

    MarkdownText(
        markdown = text.text,
        modifier = modifier.then(pressIndicator),
        style = style,
        maxLines = maxLines,
    )
}
