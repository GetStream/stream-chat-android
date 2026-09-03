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

package io.getstream.chat.android.compose.ui.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import io.getstream.chat.android.compose.ui.theme.StreamDesign
import io.getstream.chat.android.compose.ui.util.internal.MarkdownRenderer
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.utils.extensions.isMine

/**
 * Renders message text as markdown. Built by
 * [MessageTextFormatter.markdownFormatter], which documents what is and is not supported.
 */
@Suppress("LongParameterList")
internal class MarkdownMessageTextFormatter(
    private val autoTranslationEnabled: Boolean,
    private val colors: StreamDesign.Colors,
    private val typography: StreamDesign.Typography,
    private val styles: MarkdownStyles,
    private val textStyle: (isMine: Boolean, message: Message) -> TextStyle,
    private val linkStyle: (isMine: Boolean) -> TextStyle,
    private val mentionColor: (isMine: Boolean) -> Color,
    private val builder: AnnotatedMessageTextBuilder?,
) : MessageTextFormatter {

    private val renderer = MarkdownRenderer(styles)

    override fun format(message: Message, currentUser: User?): AnnotatedString {
        val displayedText = message.resolveDisplayedText(currentUser, autoTranslationEnabled)
        val isMine = message.isMine(currentUser)
        val baseStyle = SpanStyle(
            fontStyle = typography.bodyDefault.fontStyle,
            color = textStyle(isMine, message).color,
        )
        val markdown = renderer.render(displayedText)
        // Colour only: a full text style would overwrite the weight and size markdown set.
        val linkSpan = linkStyle(isMine).let { SpanStyle(color = it.color, textDecoration = it.textDecoration) }
        return buildAnnotatedString {
            append(markdown.text)
            // The base style goes on first so the markdown spans layered over it win.
            addStyle(baseStyle, start = 0, end = markdown.text.length)
            markdown.spanStyles.forEach { addStyle(it.item, it.start, it.end) }
            markdown.getStringAnnotations(0, markdown.length).forEach {
                addStringAnnotation(it.tag, it.item, it.start, it.end)
                // The entity pass below skips annotated ranges, so markdown links style here.
                if (it.tag == AnnotationTagUrl) addStyle(linkSpan, it.start, it.end)
            }
        }
            .annotateStreamEntities(
                message = message,
                colors = colors,
                linkStyle = linkStyle(isMine),
                mentionColor = mentionColor(isMine),
            )
            .let { annotated ->
                val extra = builder ?: return@let annotated
                buildAnnotatedString {
                    append(annotated)
                    extra.invoke(this, message, currentUser)
                }
            }
    }
}
