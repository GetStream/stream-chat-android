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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.util.fastAny
import androidx.core.net.toUri
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageStyling
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.AnnotationTagEmail
import io.getstream.chat.android.compose.ui.util.AnnotationTagMention
import io.getstream.chat.android.compose.ui.util.AnnotationTagUrl
import io.getstream.chat.android.compose.ui.util.isFewEmoji
import io.getstream.chat.android.compose.ui.util.isSingleEmoji
import io.getstream.chat.android.compose.ui.util.showOriginalTextAsState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.utils.extensions.getUserByNameOrId

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
@Suppress("LongMethod")
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
    val styledText = if (ChatTheme.autoTranslationEnabled) {
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
    val style = when {
        message.isSingleEmoji() -> ChatTheme.typography.singleEmoji
        message.isFewEmoji() -> ChatTheme.typography.emojiOnly
        else -> MessageStyling.messageTextStyle()
    }

    val annotations = styledText.getStringAnnotations(0, styledText.lastIndex)
    if (annotations.fastAny {
            it.tag == AnnotationTagUrl || it.tag == AnnotationTagEmail || it.tag == AnnotationTagMention
        }
    ) {
        ClickableText(
            modifier = modifier
                .padding(horizontal = StreamTokens.spacingSm, vertical = StreamTokens.spacingXs)
                .testTag("Stream_MessageClickableText"),
            text = styledText,
            style = style,
            onLongPress = { onLongItemClick(message) },
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
                .padding(horizontal = StreamTokens.spacingSm, vertical = StreamTokens.spacingXs)
                .clipToBounds()
                .testTag("Stream_MessageText"),
            text = styledText,
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
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
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
