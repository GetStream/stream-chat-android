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

package io.getstream.chat.android.ai.openui.rendering

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.rememberMarkdownState
import io.getstream.chat.android.ai.openui.R
import io.getstream.chat.android.ai.openui.icons.ContentCopy
import io.getstream.chat.android.ai.openui.utils.OpenUILink
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMTokenTypes
import org.intellij.markdown.parser.MarkdownParser

/**
 * Renders Markdown (headings, paragraphs, lists, task lists, quotes, fenced code, tables, rules,
 * inline emphasis, code and links) styled with the OpenUI theme.
 *
 * It uses the same renderer as Stream's AI Compose components (`multiplatform-markdown-renderer`);
 * links open through the composition's URI handler, which `OpenUIView` limits to web and email
 * links.
 */
@Composable
internal fun OpenUIMarkdown(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle? = null,
    color: Color? = null,
) {
    val theme = OpenUITheme.current
    val base = style ?: theme.typography.body
    val textColor = color ?: theme.colors.textPrimary
    // A new state per text, parsed synchronously, so a streaming text never shows an older parse.
    val state = key(text) { rememberMarkdownState(content = text, immediate = true) }
    Markdown(
        markdownState = state,
        modifier = modifier.fillMaxWidth(),
        colors = markdownColor(
            text = textColor,
            codeBackground = theme.colors.sunkenSurface,
            inlineCodeBackground = theme.colors.subtleFill,
            dividerColor = theme.colors.border,
            tableBackground = Color.Transparent,
        ),
        typography = markdownTypography(
            h1 = theme.typography.title.copy(fontWeight = FontWeight.Bold, color = textColor),
            h2 = theme.typography.title.copy(color = textColor),
            h3 = theme.typography.heading.copy(color = textColor),
            h4 = base.copy(fontWeight = FontWeight.SemiBold, color = textColor),
            h5 = base.copy(fontWeight = FontWeight.SemiBold, color = textColor),
            h6 = base.copy(fontWeight = FontWeight.SemiBold, color = textColor),
            text = base.copy(color = textColor),
            code = theme.typography.code.copy(color = textColor),
            inlineCode = theme.typography.code.copy(color = textColor),
            quote = base.copy(color = theme.colors.textSecondary),
            paragraph = base.copy(color = textColor),
            ordered = base.copy(color = textColor),
            bullet = base.copy(color = textColor),
            list = base.copy(color = textColor),
            table = base.copy(color = textColor),
        ),
    )
}

/** [inlineMarkdown] with the links the enclosing view can open. */
@Composable
@ReadOnlyComposable
internal fun inlineMarkdownText(text: String, linkColor: Color = Color.Unspecified): AnnotatedString =
    inlineMarkdown(text, linkColor, LocalOpenUICanOpenUrl.current)

/**
 * Inline Markdown (emphasis, strong, strikethrough, code spans, links) as an annotated string,
 * for one-line titles and labels. Block syntax is kept as text, and so is a link [canOpen] rejects.
 */
internal fun inlineMarkdown(
    text: String,
    linkColor: Color = Color.Unspecified,
    canOpen: (String?) -> Boolean = OpenUILink::isOpenable,
): AnnotatedString {
    if (text.none { it in "*_`[~" } && "://" !in text && "www." !in text) return AnnotatedString(text)
    val root = runCatching { MarkdownParser(GFMFlavourDescriptor()).buildMarkdownTreeFromString(text) }.getOrNull()
        ?: return AnnotatedString(text)
    return buildAnnotatedString {
        fun append(node: ASTNode) {
            when (node.type) {
                MarkdownElementTypes.STRONG -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    node.children
                        .drop(2)
                        .dropLast(2)
                        .forEach(::append)
                }
                MarkdownElementTypes.EMPH -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    node.children
                        .drop(1)
                        .dropLast(1)
                        .forEach(::append)
                }
                GFMElementTypes.STRIKETHROUGH -> withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                    node.children
                        .drop(2)
                        .dropLast(2)
                        .forEach(::append)
                }
                MarkdownElementTypes.CODE_SPAN -> withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                    append(node.getTextInNode(text).toString().trim('`'))
                }
                MarkdownElementTypes.INLINE_LINK -> {
                    val label = node.children.firstOrNull { it.type == MarkdownElementTypes.LINK_TEXT }
                    val destination = node.children
                        .firstOrNull { it.type == MarkdownElementTypes.LINK_DESTINATION }
                        ?.getTextInNode(text)
                        ?.toString()
                    if (destination == null || !canOpen(destination)) {
                        // A link that can't open shows as its text.
                        label?.children.orEmpty().drop(1).dropLast(1).forEach(::append)
                        if (label == null) append(node.getTextInNode(text).toString())
                    } else {
                        withLink(LinkAnnotation.Url(destination)) {
                            withStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)) {
                                label
                                    ?.children
                                    .orEmpty()
                                    .drop(1)
                                    .dropLast(1)
                                    .forEach(::append)
                            }
                        }
                    }
                }
                MarkdownTokenTypes.EOL -> append(" ")
                GFMTokenTypes.GFM_AUTOLINK -> {
                    val url = node.getTextInNode(text).toString()
                    if (canOpen(url)) withLink(LinkAnnotation.Url(url)) { append(url) } else append(url)
                }
                else -> if (node.children.isEmpty()) {
                    append(node.getTextInNode(text).toString())
                } else {
                    node.children.forEach(::append)
                }
            }
        }
        // Paragraph and header wrappers are flattened; their text is kept.
        root.children.forEach(::append)
    }
}

/** A monospaced code block with a language label and a copy button. */
@Composable
internal fun OpenUICodeBlock(language: String?, code: String, modifier: Modifier = Modifier) {
    val theme = OpenUITheme.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var copied by remember { mutableStateOf(false) }
    val label = language?.takeIf { it.isNotEmpty() }
        ?: stringResource(R.string.stream_ai_openui_code_block_language_fallback)
    LaunchedEffect(copied) {
        if (copied) {
            delay(CopiedFeedbackMillis)
            copied = false
        }
    }
    Column(modifier.fillMaxWidth().background(theme.colors.sunkenSurface, RoundedCornerShape(theme.radius.small))) {
        Row(
            Modifier.fillMaxWidth().padding(start = theme.spacing.m, end = theme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label,
                style = theme.typography.caption.copy(fontWeight = FontWeight.Medium),
                color = theme.colors.textSecondary,
            )
            Spacer(Modifier.weight(1f))
            TextButton(onClick = {
                scope.launch {
                    // "Copied" shows once the clipboard has the code.
                    copied = runCatching { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(label, code))) }
                        .isSuccess
                }
            }) {
                Icon(
                    if (copied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                    null,
                    Modifier.size(14.dp),
                    tint = theme.colors.textSecondary,
                )
                Spacer(Modifier.size(4.dp))
                Text(
                    stringResource(if (copied) CopiedLabel else CopyLabel),
                    style = theme.typography.caption,
                    color = theme.colors.textSecondary,
                )
            }
        }
        HorizontalDivider(color = theme.colors.border)
        Text(
            code,
            style = theme.typography.code,
            color = theme.colors.textPrimary,
            softWrap = false,
            modifier = Modifier.horizontalScroll(rememberScrollState()).padding(theme.spacing.m),
        )
    }
}

private val CopyLabel = R.string.stream_ai_openui_code_block_copy
private val CopiedLabel = R.string.stream_ai_openui_code_block_copied

// How long a code block shows that its code was copied.
private const val CopiedFeedbackMillis = 1_500L
