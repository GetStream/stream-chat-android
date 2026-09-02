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
import org.intellij.markdown.parser.LinkMap
import org.intellij.markdown.parser.MarkdownParser

/**
 * Renders markdown as an [AnnotatedString]: inline constructs become spans, and block constructs
 * are laid out with line breaks and paragraph indentation.
 *
 * Constructs with no styled representation fall back to something readable rather than vanishing:
 * an image shows its alt text, and a table its source text.
 */
internal class MarkdownRenderer(private val styles: MarkdownStyles) {

    fun render(text: String): AnnotatedString {
        // The parser only recognises line feeds, so a carriage return would otherwise survive into
        // the output and hide the break it belongs to.
        val source = text.replace("\r\n", "\n").replace('\r', '\n')
        val emitter = MarkdownEmitter()
        val tree = MarkdownParser(GFMFlavourDescriptor()).buildMarkdownTreeFromString(source)
        val links = LinkMap.buildLinkMap(tree, source)
        Walker(source, styles, emitter, links).visitBlocks(tree.children)
        emitter.trimTrailingNewlines()
        return emitter.build()
    }
}

private class Walker(
    private val source: String,
    private val styles: MarkdownStyles,
    private val emitter: MarkdownEmitter,
    private val links: LinkMap,
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
            MarkdownElementTypes.CODE_BLOCK ->
                codeBlock(node, contentTypes = IndentedCodeContentTypes, stripIndent = true)

            MarkdownTokenTypes.HORIZONTAL_RULE -> {
                emitter.append(styles.thematicBreak)
                emitter.endBlock(newlines = 1)
            }

            // A table has no styled form, so its source stands in, but it is still a block and
            // must not run into the next one.
            GFMElementTypes.TABLE -> {
                emitter.appendText(node.text())
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
        // one quote instead of a quoted line followed by loose text. Nesting stacks the markers,
        // so a quote inside a quote is visibly two levels deep.
        emitter.withLinePrefix(emitter.currentLinePrefix + styles.blockQuotePrefix) {
            visitBlocks(node.children.filter { it.type != MarkdownTokenTypes.BLOCK_QUOTE })
            // Trim while the quote's prefix is still current, or its own marked blank lines are
            // not recognised as trailing ones.
            emitter.trimTrailingNewlines()
        }
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

        // The item's own text shares the marker's line; anything after it starts a line of its
        // own, indented to sit under that text rather than running into it.
        var markerLineTaken = false
        for (child in node.children) {
            when (child.type) {
                // Markers are replaced, and the breaks between an item's blocks are structural.
                MarkdownTokenTypes.LIST_BULLET,
                MarkdownTokenTypes.LIST_NUMBER,
                MarkdownTokenTypes.EOL,
                MarkdownTokenTypes.WHITE_SPACE,
                -> Unit

                MarkdownElementTypes.PARAGRAPH -> {
                    if (markerLineTaken) continueItemLine(level)
                    markerLineTaken = true
                    // Items hold their content in paragraphs; rendering those as blocks would put
                    // a blank line between every item.
                    visitInlineChildren(child)
                }

                // A nested list emits its own indent, so it only needs the line closing.
                MarkdownElementTypes.UNORDERED_LIST, MarkdownElementTypes.ORDERED_LIST -> {
                    markerLineTaken = true
                    endItemLine()
                    list(child, level + 1)
                }

                else -> {
                    markerLineTaken = true
                    continueItemLine(level)
                    visitBlock(child)
                }
            }
        }
        endItemLine()
    }

    private fun endItemLine() {
        emitter.trimTrailingNewlines()
        emitter.endBlock(newlines = 1)
    }

    /** Ends the current line and indents the next one to line up under the item's text. */
    private fun continueItemLine(level: Int) {
        endItemLine()
        emitter.append(styles.listIndent.repeat(level))
    }

    private fun codeBlock(
        node: ASTNode,
        contentTypes: Set<IElementType>,
        stripIndent: Boolean = false,
    ) {
        val code = StringBuilder()
        for (child in node.children) {
            when (child.type) {
                in contentTypes -> code.append(child.text())
                MarkdownTokenTypes.EOL -> code.append('\n')
                else -> Unit
            }
        }
        val start = emitter.length
        // Emitted line by line, so a code block inside a quote keeps the quote's marker on every
        // one of its lines. Blank lines are significant here, so they are opened unconditionally.
        code.trim('\n').split('\n').forEachIndexed { index, line ->
            if (index > 0) emitter.appendNewLine()
            emitter.append(if (stripIndent) line.removePrefix(IndentedCodePrefix) else line)
        }
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
        nodes.forEachIndexed { index, node ->
            val bracketsEmailAutolink = node.type == MarkdownTokenTypes.LT &&
                nodes.getOrNull(index + 1)?.type == MarkdownTokenTypes.EMAIL_AUTOLINK ||
                node.type == MarkdownTokenTypes.GT &&
                nodes.getOrNull(index - 1)?.type == MarkdownTokenTypes.EMAIL_AUTOLINK
            when {
                node.type == MarkdownTokenTypes.BLOCK_QUOTE -> afterQuoteMarker = true
                afterQuoteMarker && node.type == MarkdownTokenTypes.WHITE_SPACE -> afterQuoteMarker = false
                // An email autolink arrives as a bare token between its brackets, unlike a URL
                // autolink, which the parser wraps in a node of its own.
                bracketsEmailAutolink -> afterQuoteMarker = false
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

            MarkdownElementTypes.FULL_REFERENCE_LINK,
            MarkdownElementTypes.SHORT_REFERENCE_LINK,
            -> referenceLink(node)

            // Images cannot be drawn inside a single text, so the alt text stands in for them,
            // which is the fallback the spec itself defines.
            MarkdownElementTypes.IMAGE -> imageAltText(node)

            // The angle brackets are syntax, not content. The URL is left as plain text for the
            // entity pass to linkify, so bracketed and bare URLs end up handled the same way.
            MarkdownElementTypes.AUTOLINK -> visitInlineChildren(node, skip = AutolinkMarkerTypes)
            MarkdownTokenTypes.EMAIL_AUTOLINK, MarkdownTokenTypes.AUTOLINK -> emitter.append(node.text())

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

    /** Resolves `[text][label]` and `[label]` against the document's link definitions. */
    private fun referenceLink(node: ASTNode) {
        val label = node.children.firstOrNull { it.type == MarkdownElementTypes.LINK_LABEL }
        val destination = label
            ?.let { links.getLinkInfo(LinkMap.normalizeLabel(it.text())) }
            ?.destination
        if (destination == null) {
            emitter.appendText(node.text())
            return
        }
        // A full reference shows its own text; a short one shows the label it was written as.
        val shown = node.children.firstOrNull { it.type == MarkdownElementTypes.LINK_TEXT } ?: label
        val start = emitter.length
        visitInlineChildren(shown, skip = LinkLabelMarkerTypes)
        destination.toString().toOpenableUrl()?.let { url ->
            emitter.addAnnotation(AnnotationTagUrl, url, start)
        }
    }

    private fun imageAltText(node: ASTNode) {
        // The alt text sits inside whichever link form the image was written with.
        val link = node.children.firstOrNull { it.type in ImageLinkTypes } ?: node
        val label = link.children.firstOrNull { it.type == MarkdownElementTypes.LINK_TEXT }
            ?: link.children.firstOrNull { it.type == MarkdownElementTypes.LINK_LABEL }
        if (label == null) {
            emitter.appendText(node.text())
            return
        }
        visitInlineChildren(label, skip = LinkLabelMarkerTypes)
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
        destination.text().toString().toOpenableUrl()?.let { url ->
            emitter.addAnnotation(AnnotationTagUrl, url, start)
        }
    }

    private inline fun styled(style: SpanStyle, content: () -> Unit) {
        val start = emitter.length
        content()
        emitter.addSpan(style, start)
    }

    private fun ASTNode.text(): CharSequence = getTextInNode(source)
}

/**
 * Appends source text, resolving the backslash escapes and character references the parser leaves
 * in place. Code content is appended as-is instead, where neither carries special meaning.
 */
private fun MarkdownEmitter.appendText(value: CharSequence) {
    var index = 0
    while (index < value.length) {
        val char = value[index]
        val next = value.getOrNull(index + 1)
        val reference = if (char == '&') value.characterReferenceAt(index) else null
        when {
            char == '\\' && next != null && next in EscapablePunctuation -> {
                append(next.toString())
                index += 2
            }

            reference != null -> {
                append(reference.first)
                index += reference.second
            }

            else -> {
                append(char.toString())
                index++
            }
        }
    }
}

/**
 * Decodes the character reference starting at [start], returning its text and how many characters
 * it spanned, or null when what follows the `&` is not one.
 *
 * Only the named references that carry meaning in markdown source are decoded, plus the numeric
 * forms; anything else is left as typed, which is what a reader of a chat message expects.
 */
private fun CharSequence.characterReferenceAt(start: Int): Pair<String, Int>? {
    val semicolon = indexOf(';', start + 1)
    if (semicolon < 0 || semicolon - start > MaxCharacterReferenceLength) return null
    val body = subSequence(start + 1, semicolon).toString()
    if (body.isEmpty()) return null
    val length = semicolon - start + 1
    NamedCharacterReferences[body]?.let { return it to length }
    if (!body.startsWith("#")) return null
    val codePoint = when {
        body.startsWith("#x", ignoreCase = true) -> body.drop(2).toIntOrNull(radix = 16)
        else -> body.drop(1).toIntOrNull()
    } ?: return null
    if (codePoint <= 0 || codePoint > Character.MAX_CODE_POINT) return null
    return String(Character.toChars(codePoint)) to length
}

private fun CharSequence.getOrNull(index: Int): Char? = if (index in indices) this[index] else null

/**
 * Turns a link destination into something that can actually be opened, or null when it cannot be.
 *
 * A fragment, a relative path, or anything holding whitespace only means something inside a
 * document. Giving one a scheme would produce a URL that fails to resolve when tapped, and the
 * message list opens links by handing them straight to the system.
 */
private fun String.toOpenableUrl(): String? {
    val destination = removeSurrounding("<", ">").trim()
    return when {
        destination.isEmpty() || destination.any(Char::isWhitespace) -> null
        SchemePattern.containsMatchIn(destination) -> destination
        destination.contains('@') && !destination.contains('/') -> "mailto:$destination"
        HostPattern.containsMatchIn(destination) -> "https://$destination"
        else -> null
    }
}

private fun String.isLineBreakTag(): Boolean = LineBreakTagPattern.matches(trim())

private fun ASTNode.isWhitespace(): Boolean = type == MarkdownTokenTypes.WHITE_SPACE

private val ItalicSpan = SpanStyle(fontStyle = FontStyle.Italic)
private val BoldSpan = SpanStyle(fontWeight = FontWeight.Bold)
private val StrikethroughSpan = SpanStyle(textDecoration = TextDecoration.LineThrough)

private const val UnorderedListMarker = "•"

private val SchemePattern = Regex("^[a-zA-Z][a-zA-Z0-9+.\\-]*:")
private val HostPattern = Regex("^[\\w\\-]+(\\.[\\w\\-]+)+")
private val LineBreakTagPattern = Regex("<br\\s*/?>", RegexOption.IGNORE_CASE)
private const val EscapablePunctuation = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~"
private const val MaxCharacterReferenceLength = 32

private val NamedCharacterReferences = mapOf(
    "amp" to "&",
    "lt" to "<",
    "gt" to ">",
    "quot" to "\"",
    "apos" to "'",
    "nbsp" to "\u00A0",
)

private val HeadingContentTypes = setOf(MarkdownTokenTypes.ATX_CONTENT, MarkdownTokenTypes.SETEXT_CONTENT)
private val HeadingMarkerTypes = setOf(
    MarkdownTokenTypes.ATX_HEADER,
    MarkdownTokenTypes.SETEXT_1,
    MarkdownTokenTypes.SETEXT_2,
)
private val EmphasisMarkerTypes = setOf(MarkdownTokenTypes.EMPH, GFMTokenTypes.TILDE)
private val LinkLabelMarkerTypes = setOf(MarkdownTokenTypes.LBRACKET, MarkdownTokenTypes.RBRACKET)
private val AutolinkMarkerTypes = setOf(MarkdownTokenTypes.LT, MarkdownTokenTypes.GT)
private val ImageLinkTypes = setOf(
    MarkdownElementTypes.INLINE_LINK,
    MarkdownElementTypes.FULL_REFERENCE_LINK,
    MarkdownElementTypes.SHORT_REFERENCE_LINK,
)
private val FenceContentTypes = setOf(MarkdownTokenTypes.CODE_FENCE_CONTENT)
private val IndentedCodeContentTypes = setOf(MarkdownTokenTypes.CODE_LINE)

/** CommonMark strips this much indentation from every line of an indented code block. */
private const val IndentedCodePrefix = "    "

/** Constructs with no styled form; their source text is shown so the content still reads. */
private val SourceTextTypes = setOf(GFMElementTypes.TABLE)
