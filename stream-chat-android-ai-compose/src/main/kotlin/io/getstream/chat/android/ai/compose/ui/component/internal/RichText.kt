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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.mikepenz.markdown.compose.LocalMarkdownColors
import com.mikepenz.markdown.compose.LocalMarkdownDimens
import com.mikepenz.markdown.compose.components.MarkdownComponent
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownCodeBackground
import com.mikepenz.markdown.compose.elements.MarkdownCodeFence
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCode
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeBlock
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.model.rememberMarkdownState
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode

/**
 * Type alias for a composable function that renders rich text content.
 * Custom implementations can be provided to override the default markdown rendering behavior.
 */
internal typealias RichTextComponent = @Composable (text: String, modifier: Modifier) -> Unit

/**
 * Composable that renders rich text content with markdown support.
 * Supports standard markdown features including code blocks, code fences, and Chart.js diagrams.
 *
 * When a code fence is marked with the "chartjs" language, it will be rendered as an interactive
 * chart diagram instead of plain code.
 *
 * @param text The markdown text content to render
 * @param modifier Modifier to be applied to the rich text container
 * @param component Custom component for rendering the text. Defaults to markdown rendering with
 *   Chart.js diagram support.
 */
@Composable
internal fun RichText(
    text: String,
    modifier: Modifier = Modifier,
    component: RichTextComponent = DefaultRichTextComponent,
) {
    component(text, modifier)
}

private val DefaultRichTextComponent: RichTextComponent = { text, modifier ->
    // A new state per text, parsed synchronously: the library parses only a state's first text
    // at once, so while StreamingText reveals a reply a later text would show nothing until its
    // parse lands, and an older, shorter parse could land last and leave the reply cut short.
    val markdownState = key(text) { rememberMarkdownState(content = text, immediate = true) }

    Markdown(
        modifier = modifier,
        markdownState = markdownState,
        components = MarkdownComponents,
    )
}

private val CodeFence: MarkdownComponent = {
    MarkdownCodeFence(
        content = it.content,
        node = it.node,
        style = it.typography.code,
    ) { code, language, style ->
        // If it's a chartjs and complete, render it as a chart diagram
        val codeContent = extractCodeFenceContent(it.content, it.node)
        if (language?.lowercase() == "chartjs" && codeContent != null) {
            val backgroundCodeColor = LocalMarkdownColors.current.codeBackground
            val codeBackgroundCornerSize = LocalMarkdownDimens.current.codeBackgroundCornerSize
            MarkdownCodeBackground(
                color = backgroundCodeColor,
                shape = RoundedCornerShape(codeBackgroundCornerSize),
            ) {
                ChartJsDiagram(
                    modifier = Modifier.fillMaxWidth(),
                    chartJsJson = codeContent,
                )
            }
        } else {
            // Otherwise, render as a regular code
            MarkdownHighlightedCode(
                code = code,
                language = language,
                style = style,
                showHeader = true,
            )
        }
    }
}

private val CodeBlock: MarkdownComponent = {
    MarkdownHighlightedCodeBlock(
        content = it.content,
        node = it.node,
        style = it.typography.code,
        showHeader = true,
    )
}

private val MarkdownComponents = markdownComponents(
    codeFence = CodeFence,
    codeBlock = CodeBlock,
)

/**
 * Extracts the code content from a code fence (without the language identifier).
 */
private fun extractCodeFenceContent(content: String, node: ASTNode): String? {
    if (node.type != MarkdownElementTypes.CODE_FENCE) {
        return null
    }

    // Get the full text of the code fence node
    val fullText = node.getTextInNode(content).toString()
    val lines = fullText.lines()

    // Must have at least opening fence and closing fence
    if (lines.size < 2) {
        return null
    }

    // Check if last line is closing fence (starts with ```)
    val lastLine = lines.last().trim()
    if (!lastLine.startsWith("```")) {
        return null
    }

    return lines.drop(1).dropLast(1).joinToString("\n").trim()
}
