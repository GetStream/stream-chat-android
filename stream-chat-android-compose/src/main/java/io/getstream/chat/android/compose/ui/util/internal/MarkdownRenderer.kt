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

package io.getstream.chat.android.compose.ui.util.internal

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import io.getstream.chat.android.compose.ui.util.AnnotationTagUrl
import io.getstream.chat.android.compose.ui.util.MarkdownStyles
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMTokenTypes
import org.intellij.markdown.parser.MarkdownParser

/**
 * Renders markdown as an [AnnotatedString]: inline constructs become spans, and block constructs
 * are laid out with line breaks and paragraph indentation.
 *
 * Constructs with no styled representation, such as images and tables, are emitted as their source
 * text so nothing the sender typed is lost.
 */
internal class MarkdownRenderer(private val styles: MarkdownStyles) {

    fun render(source: String): AnnotatedString {
        val emitter = MarkdownEmitter()
        val tree = MarkdownParser(GFMFlavourDescriptor()).buildMarkdownTreeFromString(source)
        Walker(source, styles, emitter).visitBlocks(tree.children)
        emitter.trimTrailingNewlines()
        return emitter.build()
    }
}

private class Walker(
    private val source: String,
    private val styles: MarkdownStyles,
    private val emitter: MarkdownEmitter,
) {

    fun visitBlocks(nodes: List<ASTNode>) = nodes.forEach(::visitBlock)

    private fun visitBlock(node: ASTNode) {
        when (node.type) {
            MarkdownElementTypes.PARAGRAPH -> {
                visitInlineChildren(node)
                // A paragraph break in the source is a blank line, so keep it as one.
                emitter.endBlock(newlines = 2)
            }

            MarkdownElementTypes.ATX_1 -> heading(node, level = 1)
            MarkdownElementTypes.ATX_2 -> heading(node, level = 2)
            MarkdownElementTypes.ATX_3 -> heading(node, level = 3)
            MarkdownElementTypes.ATX_4 -> heading(node, level = 4)
            MarkdownElementTypes.ATX_5 -> heading(node, level = 5)
            MarkdownElementTypes.ATX_6 -> heading(node, level = 6)
            MarkdownElementTypes.SETEXT_1 -> heading(node, level = 1)
            MarkdownElementTypes.SETEXT_2 -> heading(node, level = 2)

            MarkdownElementTypes.BLOCK_QUOTE -> blockQuote(node)
            MarkdownElementTypes.UNORDERED_LIST, MarkdownElementTypes.ORDERED_LIST -> list(node, level = 1)
            MarkdownElementTypes.CODE_FENCE -> codeBlock(node, contentTypes = FenceContentTypes)
            MarkdownElementTypes.CODE_BLOCK -> codeBlock(node, contentTypes = IndentedCodeContentTypes)

            MarkdownTokenTypes.HORIZONTAL_RULE -> {
                emitter.append(styles.thematicBreak)
                emitter.endBlock(newlines = 1)
            }

            // Link definitions declare a reference target and render nothing themselves.
            MarkdownElementTypes.LINK_DEFINITION -> Unit

            // Separation between blocks is driven by endBlock, so structural breaks are dropped.
            MarkdownTokenTypes.EOL, MarkdownTokenTypes.WHITE_SPACE -> Unit

            else -> visitInlineNode(node)
        }
    }

    private fun heading(node: ASTNode, level: Int) {
        val start = emitter.length
        val content = node.children.filter { it.type in HeadingContentTypes }
        if (content.isEmpty()) {
            // Setext headings hold their text directly, ATX headings wrap it in a content node.
            visitInlineChildren(node, skip = HeadingMarkerTypes)
        } else {
            // The content node keeps the space that separated the text from the marker.
            content.forEach { visitInlineNodes(it.children.dropWhile(ASTNode::isWhitespace)) }
        }
        emitter.addSpan(styles.heading(level), start)
        emitter.endBlock(newlines = 1)
    }

    private fun blockQuote(node: ASTNode) {
        val start = emitter.length
        emitter.append(styles.blockQuotePrefix)
        // Marking every line, rather than just the first, is what makes a multi-line quote read as
        // one quote instead of a quoted line followed by loose text.
        emitter.withLinePrefix(styles.blockQuotePrefix) {
            visitBlocks(node.children.filter { it.type != MarkdownTokenTypes.BLOCK_QUOTE })
        }
        emitter.trimTrailingNewlines()
        emitter.addSpan(styles.blockQuote, start)
        // Two newlines keep consecutive quotes apart, so that two of them cannot be mistaken for
        // one quote spanning two lines.
        emitter.endBlock(newlines = 2)
    }

    private fun list(node: ASTNode, level: Int) {
        val items = node.children.filter { it.type == MarkdownElementTypes.LIST_ITEM }
        val ordered = node.type == MarkdownElementTypes.ORDERED_LIST
        // Ordered lists are numbered from the first marker onwards, so that a list written
        // entirely as "1." still reads as 1, 2, 3.
        val firstNumber = items.firstNotNullOfOrNull(::orderedMarkerNumber) ?: 1
        items.forEachIndexed { index, item ->
            val marker = when {
                ordered -> "${firstNumber + index}. "
                else -> "$UnorderedListMarker "
            }
            listItem(item, level, marker)
        }
    }

    private fun orderedMarkerNumber(item: ASTNode): Int? = item.children
        .firstOrNull { it.type == MarkdownTokenTypes.LIST_NUMBER }
        ?.text()
        ?.trimStart()
        ?.takeWhile(Char::isDigit)
        ?.toString()
        ?.toIntOrNull()

    private fun listItem(node: ASTNode, level: Int, marker: String) {
        emitter.append(styles.listIndent.repeat(level - 1))
        emitter.append(marker)

        // A nested list starts its own lines, so the current line's indent is closed before it.
        var lineClosed = false
        for (child in node.children) {
            when (child.type) {
                MarkdownElementTypes.UNORDERED_LIST, MarkdownElementTypes.ORDERED_LIST -> {
                    if (!lineClosed) {
                        lineClosed = true
                        closeItemLine()
                    }
                    list(child, level + 1)
                }

                MarkdownTokenTypes.LIST_BULLET, MarkdownTokenTypes.LIST_NUMBER -> Unit

                // Items hold their content in paragraphs; rendering those as blocks would put a
                // blank line between every item.
                MarkdownElementTypes.PARAGRAPH -> visitInlineChildren(child)

                else -> visitBlock(child)
            }
        }
        if (!lineClosed) closeItemLine()
    }

    private fun closeItemLine() {
        emitter.trimTrailingNewlines()
        emitter.endBlock(newlines = 1)
    }

    private fun codeBlock(node: ASTNode, contentTypes: Set<IElementType>) {
        val code = StringBuilder()
        for (child in node.children) {
            when (child.type) {
                in contentTypes -> code.append(child.text())
                MarkdownTokenTypes.EOL -> code.append('\n')
                else -> Unit
            }
        }
        val start = emitter.length
        emitter.append(code.trim('\n'))
        emitter.addSpan(styles.codeBlock, start)
        emitter.endBlock(newlines = 1)
    }

    private fun visitInlineChildren(node: ASTNode, skip: Set<IElementType> = emptySet()) {
        visitInlineNodes(node.children.filter { it.type !in skip })
    }

    private fun visitInlineNodes(nodes: List<ASTNode>) {
        // Every line of a quote carries its own marker, and continuation lines keep theirs inside
        // the quoted paragraph. The marker and the space that separates it from the text both go.
        var afterQuoteMarker = false
        for (node in nodes) {
            when {
                node.type == MarkdownTokenTypes.BLOCK_QUOTE -> afterQuoteMarker = true
                afterQuoteMarker && node.type == MarkdownTokenTypes.WHITE_SPACE -> afterQuoteMarker = false
                else -> {
                    afterQuoteMarker = false
                    visitInlineNode(node)
                }
            }
        }
    }

    private fun visitInlineNode(node: ASTNode) {
        when (node.type) {
            MarkdownElementTypes.EMPH -> styled(ItalicSpan) {
                visitInlineChildren(node, skip = EmphasisMarkerTypes)
            }

            MarkdownElementTypes.STRONG -> styled(BoldSpan) {
                visitInlineChildren(node, skip = EmphasisMarkerTypes)
            }

            GFMElementTypes.STRIKETHROUGH -> styled(StrikethroughSpan) {
                visitInlineChildren(node, skip = EmphasisMarkerTypes)
            }

            MarkdownElementTypes.CODE_SPAN -> styled(styles.codeSpan) {
                node.children
                    .filter { it.type != MarkdownTokenTypes.BACKTICK }
                    .forEach { emitter.append(it.text()) }
            }

            MarkdownElementTypes.INLINE_LINK -> inlineLink(node)

            MarkdownTokenTypes.HARD_LINE_BREAK, MarkdownTokenTypes.EOL -> emitter.appendLineBreak()

            MarkdownTokenTypes.HTML_TAG -> {
                val tag = node.text().toString()
                if (tag.isLineBreakTag()) emitter.appendLineBreak() else emitter.appendText(tag)
            }

            else ->
                if (node.children.isEmpty() || node.type in SourceTextTypes) {
                    emitter.appendText(node.text())
                } else {
                    visitInlineChildren(node)
                }
        }
    }

    private fun inlineLink(node: ASTNode) {
        val label = node.children.firstOrNull { it.type == MarkdownElementTypes.LINK_TEXT }
        val destination = node.children.firstOrNull { it.type == MarkdownElementTypes.LINK_DESTINATION }
        if (label == null || destination == null) {
            emitter.appendText(node.text())
            return
        }
        val start = emitter.length
        visitInlineChildren(label, skip = LinkLabelMarkerTypes)
        emitter.addAnnotation(AnnotationTagUrl, destination.text().toString().toAbsoluteUrl(), start)
    }

    private inline fun styled(style: SpanStyle, content: () -> Unit) {
        val start = emitter.length
        content()
        emitter.addSpan(style, start)
    }

    private fun ASTNode.text(): CharSequence = getTextInNode(source)
}

/**
 * Appends source text, resolving the backslash escapes the parser leaves in place. Code content is
 * appended as-is instead, where a backslash carries no special meaning.
 */
private fun MarkdownEmitter.appendText(value: CharSequence) {
    var index = 0
    while (index < value.length) {
        val char = value[index]
        val next = value.getOrNull(index + 1)
        if (char == '\\' && next != null && next in EscapablePunctuation) {
            append(next.toString())
            index += 2
        } else {
            append(char.toString())
            index++
        }
    }
}

private fun CharSequence.getOrNull(index: Int): Char? = if (index in indices) this[index] else null

/** Angle-bracketed destinations and schemeless ones both have to become something openable. */
private fun String.toAbsoluteUrl(): String {
    val destination = removeSurrounding("<", ">").trim()
    if (SchemePattern.containsMatchIn(destination)) return destination
    val looksLikeEmail = destination.contains('@') && !destination.contains('/')
    return if (looksLikeEmail) "mailto:$destination" else "https://$destination"
}

private fun String.isLineBreakTag(): Boolean = LineBreakTagPattern.matches(trim())

private fun ASTNode.isWhitespace(): Boolean = type == MarkdownTokenTypes.WHITE_SPACE

private val ItalicSpan = SpanStyle(fontStyle = FontStyle.Italic)
private val BoldSpan = SpanStyle(fontWeight = FontWeight.Bold)
private val StrikethroughSpan = SpanStyle(textDecoration = TextDecoration.LineThrough)

private const val UnorderedListMarker = "•"

private val SchemePattern = Regex("^[a-zA-Z][a-zA-Z0-9+.\\-]*:")
private val LineBreakTagPattern = Regex("<br\\s*/?>", RegexOption.IGNORE_CASE)
private const val EscapablePunctuation = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~"

private val HeadingContentTypes = setOf(MarkdownTokenTypes.ATX_CONTENT, MarkdownTokenTypes.SETEXT_CONTENT)
private val HeadingMarkerTypes = setOf(
    MarkdownTokenTypes.ATX_HEADER,
    MarkdownTokenTypes.SETEXT_1,
    MarkdownTokenTypes.SETEXT_2,
)
private val EmphasisMarkerTypes = setOf(MarkdownTokenTypes.EMPH, GFMTokenTypes.TILDE)
private val LinkLabelMarkerTypes = setOf(MarkdownTokenTypes.LBRACKET, MarkdownTokenTypes.RBRACKET)
private val FenceContentTypes = setOf(MarkdownTokenTypes.CODE_FENCE_CONTENT)
private val IndentedCodeContentTypes = setOf(MarkdownTokenTypes.CODE_LINE)

/** Constructs with no styled form; their source text is shown so the content still reads. */
private val SourceTextTypes = setOf(
    MarkdownElementTypes.IMAGE,
    MarkdownElementTypes.FULL_REFERENCE_LINK,
    MarkdownElementTypes.SHORT_REFERENCE_LINK,
    GFMElementTypes.TABLE,
)
