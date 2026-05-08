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

package io.getstream.chat.android.compose.ui.components.messages

import android.content.Intent
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import androidx.core.net.toUri
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageStyling
import io.getstream.chat.android.compose.ui.util.AnnotationTagEmail
import io.getstream.chat.android.compose.ui.util.AnnotationTagMention
import io.getstream.chat.android.compose.ui.util.AnnotationTagUrl
import io.getstream.chat.android.compose.ui.util.isFewEmoji
import io.getstream.chat.android.compose.ui.util.isSingleEmoji
import io.getstream.chat.android.compose.ui.util.showOriginalTextAsState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.utils.extensions.getUserByNameOrId
import io.getstream.chat.android.ui.common.utils.extensions.isMine
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Default text element for messages, with extra styling and padding for the chat bubble.
 *
 * It detects if we have any annotations/links in the message, and if so, it uses the [ClickableText]
 * component to allow for clicks on said links, that will open the link.
 *
 * Alternatively, it just shows a basic [Text] element.
 *
 * @param message Message to show.
 * @param currentUser The currently logged in user.
 * @param modifier Modifier for styling.
 * @param onLongItemClick Handler used for long pressing on the message text.
 * @param onLinkClick Handler used for clicking on a link in the message.
 */
@Composable
@Suppress("LongMethod", "detekt:ForbiddenComment")
public fun MessageText(
    message: Message,
    currentUser: User?,
    modifier: Modifier = Modifier,
    onLongItemClick: (Message) -> Unit,
    onLinkClick: ((Message, String) -> Unit)? = null,
    onUserMentionClick: (User) -> Unit = {},
) {
    val context = LocalContext.current

    val formatter = ChatTheme.messageTextFormatter
    val styledText = if (ChatTheme.config.translation.enabled) {
        val showOriginalText by showOriginalTextAsState(message.cid, message.id)
        remember(message, currentUser, showOriginalText) {
            formatter.format(message, currentUser)
        }
    } else {
        remember(message, currentUser) {
            formatter.format(message, currentUser)
        }
    }

    // TODO: Fix emoji font padding once this is resolved and exposed: https://issuetracker.google.com/issues/171394808
    // TODO: introduce dedicated emoji text style tokens in the design system
    val style = when {
        message.isSingleEmoji() -> TextStyle(fontSize = 64.sp)
        message.isFewEmoji() -> TextStyle(fontSize = 64.sp)
        else -> MessageStyling.textStyle(outgoing = message.isMine(currentUser))
    }

    val annotations = styledText.getStringAnnotations(0, styledText.lastIndex)
    if (annotations.fastAny {
            it.tag == AnnotationTagUrl || it.tag == AnnotationTagEmail || it.tag == AnnotationTagMention
        }
    ) {
        ClickableText(
            modifier = modifier
                .padding(MessageStyling.textPadding)
                .testTag("Stream_MessageClickableText"),
            text = styledText,
            style = style,
            onLongPress = { onLongItemClick(message) },
            isInteractiveAt = { offset ->
                annotations.fastAny { ann ->
                    (ann.tag == AnnotationTagUrl || ann.tag == AnnotationTagEmail || ann.tag == AnnotationTagMention) &&
                        offset in ann.start..ann.end
                }
            },
        ) { position ->
            val annotation = annotations.firstOrNull { position in it.start..it.end }
            if (annotation?.tag == AnnotationTagMention) {
                message.mentionedUsers.getUserByNameOrId(annotation.item)?.let { onUserMentionClick.invoke(it) }
            } else {
                val targetUrl = annotation?.item
                if (!targetUrl.isNullOrEmpty()) {
                    onLinkClick?.invoke(message, targetUrl) ?: context.startActivity(
                        Intent(Intent.ACTION_VIEW, targetUrl.toUri()),
                    )
                }
            }
        }
    } else {
        Text(
            modifier = modifier
                .padding(MessageStyling.textPadding)
                .clipToBounds()
                .testTag("Stream_MessageText"),
            text = styledText,
            style = style,
        )
    }
}

/**
 * A spin-off of a Foundation component that allows calling long press handlers and only claims
 * the gesture when the press lands on an interactive character (link, mention, email).
 * Non-interactive presses are left untouched so the surrounding bubble can render its passive
 * ripple and the cell can still fire its click / long-press handler.
 *
 * Follow-up: migrate to Compose Foundation's `LinkAnnotation` API (`AnnotatedString.Builder.addLink`
 * with `LinkAnnotation.Url` / `LinkAnnotation.Clickable`). Native handling propagates non-link
 * taps to the parent for free, removing the need for this custom gesture detector and the
 * `isInteractiveAt` plumbing. Requires reworking `TextUtils.linkify` / `tagUser` to emit link
 * annotations instead of legacy string annotations and updating `MessageTextFormatter` to expose
 * a `LinkInteractionListener` hook for click routing.
 *
 * @param onLongPress Handler called on long press of an interactive character.
 * @param isInteractiveAt Returns whether the given character offset has an interactive annotation
 * (link, mention, email). When `false`, the gesture is not consumed and propagates to ancestors.
 * @param onClick Handler called on tap-up of an interactive character; receives the character offset.
 *
 * @see androidx.compose.foundation.text.ClickableText
 */
@Composable
private fun ClickableText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    onLongPress: () -> Unit,
    isInteractiveAt: (Int) -> Boolean,
    onClick: (Int) -> Unit,
) {
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val pressIndicator = Modifier.pointerInput(onClick, onLongPress, isInteractiveAt) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = true)
            val layout = layoutResult.value ?: return@awaitEachGesture
            val charAt = layout.getOffsetForPosition(down.position)
            if (!isInteractiveAt(charAt)) {
                return@awaitEachGesture
            }
            down.consume()
            val up: PointerInputChange? = try {
                withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                    waitForUpOrCancellation()
                }
            } catch (_: PointerEventTimeoutCancellationException) {
                onLongPress()
                consumeUntilUp()
                return@awaitEachGesture
            }
            if (up != null) {
                up.consume()
                onClick(charAt)
            }
        }
    }

    BasicText(
        text = text,
        modifier = modifier.then(pressIndicator),
        style = style,
        softWrap = softWrap,
        overflow = overflow,
        maxLines = maxLines,
        onTextLayout = {
            layoutResult.value = it
            onTextLayout(it)
        },
    )
}

private suspend fun AwaitPointerEventScope.consumeUntilUp() {
    do {
        val event = awaitPointerEvent()
        event.changes.fastForEach { it.consume() }
    } while (event.changes.fastAny { it.pressed })
}

@Preview
@Composable
private fun MessageTextPreview() {
    ChatTheme {
        MessageText(
            message = Message(text = "Hello World!"),
            currentUser = null,
            onLongItemClick = {},
        )
    }
}
