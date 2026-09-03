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
import io.getstream.chat.android.compose.ui.util.AnnotationTagLiteral
import io.getstream.chat.android.compose.ui.util.AnnotationTagUrl
import io.getstream.chat.android.compose.ui.util.MarkdownStyles
import io.getstream.log.taggedLogger
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
 * Renders markdown as an [AnnotatedString]: inline constructs become spans, blocks are laid out
 * with line breaks and indentation. A construct with no styled form falls back to something
 * readable, an image to its alt text and a table to its source.
 */
internal class MarkdownRenderer(private val styles: MarkdownStyles) {

    private val logger by taggedLogger("Chat:MarkdownRenderer")

    fun render(text: String): AnnotatedString {
        // The parser only recognises line feeds; a carriage return would reach the output.
        val source = text.replace("\r\n", "\n").replace('\r', '\n')
        // Runs during composition on text from other people, and nesting deep enough exhausts the
        // stack while parsing. Failing here would take the message list down on every reopen.
        return try {
            renderOrThrow(source)
        } catch (@Suppress("TooGenericExceptionCaught") error: Throwable) {
            logger.e(error) { "[render] failed, falling back to plain text" }
            AnnotatedString(source)
        }
    }

    private fun renderOrThrow(source: String): AnnotatedString {
        val emitter = MarkdownEmitter()
        val tree = MarkdownParser(GFMFlavourDescriptor()).buildMarkdownTreeFromString(source)
        val links = LinkMap.buildLinkMap(tree, source)
        Walker(source, styles, emitter, links).visitBlocks(tree.children)
        emitter.trimTrailingNewlines()
        val rendered = emitter.build()
        // A message holding only link definitions renders nothing, so show what was typed.
        return when {
            rendered.text.isEmpty() && source.isNotEmpty() -> AnnotatedString(source)
            else -> rendered
        }
    }
}

// A visitor needs a member per construct, and markdown has more than the threshold allows.
@Suppress("TooManyFunctions")
private class Walker(
    private val source: String,
    private val styles: MarkdownStyles,
    private val emitter: MarkdownEmitter,
    private val links: LinkMap,
) {

    fun visitBlocks(nodes: List<ASTNode>) {
        // The breaks between two blocks are how the author spaced them, so carry that across
        // rather than imposing a fixed separation per block type.
        var breaks = 0
        var started = false
        for (node in nodes) {
            when (node.type) {
                MarkdownTokenTypes.EOL -> if (started) breaks++
                MarkdownTokenTypes.WHITE_SPACE -> Unit
                else -> {
                    if (started) emitter.endBlock(breaks.coerceIn(1, MaxBlockBreaks))
                    breaks = 0
                    started = true
                    visitBlock(node)
                }
            }
        }
    }

    private fun visitBlock(node: ASTNode) {
        when (node.type) {
            MarkdownElementTypes.PARAGRAPH -> visitInlineChildren(node)

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
            MarkdownElementTypes.CODE_FENCE -> codeBlock(node, contentType = MarkdownTokenTypes.CODE_FENCE_CONTENT)
            MarkdownElementTypes.CODE_BLOCK ->
                codeBlock(node, contentType = MarkdownTokenTypes.CODE_LINE, stripIndent = true)

            MarkdownTokenTypes.HORIZONTAL_RULE -> emitter.append(styles.thematicBreak)

            // No styled form, so the source stands in, but both are still blocks.
            GFMElementTypes.TABLE, MarkdownElementTypes.HTML_BLOCK -> verbatimBlock(node)

            // A definition declares a reference target and renders nothing itself.
            MarkdownElementTypes.LINK_DEFINITION -> Unit

            // endBlock drives the separation, so structural breaks are dropped.
            MarkdownTokenTypes.EOL, MarkdownTokenTypes.WHITE_SPACE -> Unit

            else -> visitInlineNode(node)
        }
    }

    /**
     * Emits a construct's source as written, less the quote markers its continuation lines carry,
     * which the line prefix already stands for.
     */
    private fun verbatimBlock(node: ASTNode) {
        node.text().toString().split('\n').forEachIndexed { index, line ->
            if (index > 0) emitter.appendLineBreak()
            emitter.appendText(if (index > 0) QuoteMarkers.replace(line, "") else line)
        }
    }

    private fun heading(node: ASTNode, level: Int) {
        val start = emitter.length
        val content = node.children.filter { it.type in HeadingContentTypes }
        if (content.isEmpty()) {
            // Setext headings hold their text directly, ATX headings wrap it in a content node.
            visitInlineChildren(node, skip = HeadingMarkerTypes)
        } else {
            // The content node keeps the spaces separating the text from either marker.
            content.forEach {
                visitInlineNodes(
                    it.children.dropWhile(ASTNode::isWhitespace).dropLastWhile(ASTNode::isWhitespace),
                )
            }
        }
        emitter.addSpan(styles.heading(level), start)
    }

    private fun blockQuote(node: ASTNode) {
        val start = emitter.length
        emitter.append(styles.blockQuotePrefix)
        // Marking every line, not just the first, is what makes a multi-line quote read as one.
        // Nesting stacks the markers, so a quote inside a quote reads as two levels.
        emitter.withLinePrefix(emitter.currentLinePrefix + styles.blockQuotePrefix) {
            visitBlocks(node.children.filter { it.type != MarkdownTokenTypes.BLOCK_QUOTE })
            // Trim under the quote's prefix, or its marked blank lines are not seen as trailing.
            emitter.trimTrailingNewlines()
        }
        emitter.addSpan(styles.blockQuote, start)
    }

    private fun list(node: ASTNode, level: Int) {
        val items = node.children.filter { it.type == MarkdownElementTypes.LIST_ITEM }
        val ordered = node.type == MarkdownElementTypes.ORDERED_LIST
        // Numbered from the first marker on, so a list written entirely as "1." reads 1, 2, 3.
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

        // Whatever comes first shares the marker's line, so no marker is left alone on one.
        // Everything after starts its own line, indented under the item's text.
        var markerLineTaken = false
        for (child in node.children) {
            when (child.type) {
                // A checkbox belongs beside the marker, so it must not take the line.
                GFMTokenTypes.CHECK_BOX -> emitter.append(child.text())

                // Markers are replaced, and the breaks between an item's blocks are structural.
                MarkdownTokenTypes.LIST_BULLET,
                MarkdownTokenTypes.LIST_NUMBER,
                MarkdownTokenTypes.EOL,
                MarkdownTokenTypes.WHITE_SPACE,
                -> Unit

                MarkdownElementTypes.PARAGRAPH -> {
                    if (markerLineTaken) continueItemLine(level)
                    markerLineTaken = true
                    // Items hold content in paragraphs; as blocks they would gain blank lines.
                    visitInlineChildren(child)
                }

                // A nested list emits its own indent, so it only needs the line closing.
                MarkdownElementTypes.UNORDERED_LIST, MarkdownElementTypes.ORDERED_LIST -> {
                    if (markerLineTaken) endItemLine()
                    markerLineTaken = true
                    list(child, level + 1)
                }

                else -> {
                    if (markerLineTaken) continueItemLine(level)
                    markerLineTaken = true
                    // continueItemLine indents the first line, the prefix carries the rest.
                    emitter.withLinePrefix(emitter.currentLinePrefix + styles.listIndent.repeat(level)) {
                        visitBlock(child)
                        // Trim under the item's prefix, or its last marked line is left dangling.
                        emitter.trimTrailingNewlines()
                    }
                }
            }
        }
        endItemLine()
    }

    private fun endItemLine() {
        emitter.trimTrailingNewlines()
        emitter.endBlock(newlines = 1)
    }

    private fun continueItemLine(level: Int) {
        endItemLine()
        emitter.append(styles.listIndent.repeat(level))
    }

    private fun codeBlock(node: ASTNode, contentType: IElementType, stripIndent: Boolean = false) {
        val code = StringBuilder()
        for (child in node.children) {
            when (child.type) {
                contentType -> code.append(child.text())
                MarkdownTokenTypes.EOL -> code.append('\n')
                else -> Unit
            }
        }
        val start = emitter.length
        // Line by line, so a code block inside a quote keeps the quote's marker on each.
        code.trim('\n').split('\n').forEachIndexed { index, line ->
            if (index > 0) emitter.appendLineBreak()
            emitter.append(if (stripIndent) line.stripCodeIndent() else line)
        }
        emitter.addSpan(styles.codeBlock, start)
        emitter.addAnnotation(AnnotationTagLiteral, "", start)
    }

    private fun visitInlineChildren(node: ASTNode, skip: Set<IElementType> = emptySet()) {
        visitInlineNodes(node.children.filter { it.type !in skip })
    }

    private fun visitInlineNodes(nodes: List<ASTNode>) {
        // A quote marks every line, so a continuation line carries one inside the paragraph.
        var afterQuoteMarker = false
        var afterHardBreak = false
        nodes.forEachIndexed { index, node ->
            val bracketsEmailAutolink = node.type == MarkdownTokenTypes.LT &&
                nodes.getOrNull(index + 1)?.type == MarkdownTokenTypes.EMAIL_AUTOLINK ||
                node.type == MarkdownTokenTypes.GT &&
                nodes.getOrNull(index - 1)?.type == MarkdownTokenTypes.EMAIL_AUTOLINK
            when {
                node.type == MarkdownTokenTypes.BLOCK_QUOTE -> afterQuoteMarker = true
                afterQuoteMarker && node.type == MarkdownTokenTypes.WHITE_SPACE -> afterQuoteMarker = false
                // A hard break is a marker plus the feed it sits on; only the marker breaks.
                afterHardBreak && node.type == MarkdownTokenTypes.EOL -> afterHardBreak = false
                // An email autolink is a bare token between brackets, unlike a URL autolink.
                bracketsEmailAutolink -> afterQuoteMarker = false
                else -> {
                    afterQuoteMarker = false
                    afterHardBreak = node.isHardBreak(source)
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

            MarkdownElementTypes.CODE_SPAN -> literal(styles.codeSpan) {
                // The specification turns a line ending in a code span into a space.
                val content = node.children
                    .dropWhile { it.type == MarkdownTokenTypes.BACKTICK }
                    .dropLastWhile { it.type == MarkdownTokenTypes.BACKTICK }
                content.forEachIndexed { index, child ->
                    val opensQuotedLine =
                        content.getOrNull(index - 1)?.type == MarkdownTokenTypes.BLOCK_QUOTE
                    when {
                        child.type == MarkdownTokenTypes.BLOCK_QUOTE -> Unit
                        opensQuotedLine && child.type == MarkdownTokenTypes.WHITE_SPACE -> Unit
                        child.type == MarkdownTokenTypes.EOL -> emitter.append(" ")
                        else -> emitter.append(child.text())
                    }
                }
            }

            MarkdownElementTypes.INLINE_LINK -> inlineLink(node)

            MarkdownElementTypes.FULL_REFERENCE_LINK,
            MarkdownElementTypes.SHORT_REFERENCE_LINK,
            -> referenceLink(node)

            // An image cannot be drawn, so its alt text stands in, as the specification says.
            MarkdownElementTypes.IMAGE -> imageAltText(node)

            // The brackets are syntax; the entity pass linkifies the URL like any bare one.
            MarkdownElementTypes.AUTOLINK -> visitInlineChildren(node, skip = AutolinkMarkerTypes)
            MarkdownTokenTypes.EMAIL_AUTOLINK, MarkdownTokenTypes.AUTOLINK -> emitter.append(node.text())

            MarkdownTokenTypes.HARD_LINE_BREAK, MarkdownTokenTypes.EOL -> emitter.appendLineBreak()

            MarkdownTokenTypes.HTML_TAG -> when {
                node.isHardBreak(source) -> emitter.appendLineBreak()
                else -> emitter.appendText(node.text())
            }

            else ->
                if (node.children.isEmpty()) {
                    emitter.appendText(node.text())
                } else {
                    visitInlineChildren(node)
                }
        }
    }

    /**
     * Emits what a link, reference or image shows, annotated with [destination] when there is one
     * worth opening. Showing nothing falls back to the source, so nothing vanishes.
     */
    private fun linkLike(node: ASTNode, label: ASTNode?, destination: String?) {
        if (label == null) {
            emitter.appendText(node.text())
            return
        }
        val start = emitter.length
        visitInlineChildren(label, skip = LinkLabelMarkerTypes)
        if (emitter.length == start) {
            emitter.appendText(node.text())
            return
        }
        destination?.resolveMarkdownText()?.toOpenableUrl()?.let { url ->
            emitter.addAnnotation(AnnotationTagUrl, url, start)
        }
    }

    private fun inlineLink(node: ASTNode) {
        val destination = node.children.firstOrNull { it.type == MarkdownElementTypes.LINK_DESTINATION }
        when (destination) {
            null -> emitter.appendText(node.text())
            else -> linkLike(
                node = node,
                label = node.children.firstOrNull { it.type == MarkdownElementTypes.LINK_TEXT },
                destination = destination.text().toString(),
            )
        }
    }

    /** Resolves `[text][label]` and `[label]` against the document's link definitions. */
    private fun referenceLink(node: ASTNode) {
        val label = node.children.firstOrNull { it.type == MarkdownElementTypes.LINK_LABEL }
        val destination = label
            ?.let { links.getLinkInfo(LinkMap.normalizeLabel(it.text())) }
            ?.destination
        when (destination) {
            // With no definition to resolve against, the reference reads as it was written.
            null -> emitter.appendText(node.text())
            else -> linkLike(
                node = node,
                // A full reference shows its own text; a short one shows the label it was written as.
                label = node.children.firstOrNull { it.type == MarkdownElementTypes.LINK_TEXT } ?: label,
                destination = destination.toString(),
            )
        }
    }

    private fun imageAltText(node: ASTNode) {
        val link = node.children.firstOrNull { it.type in ImageLinkTypes } ?: node
        linkLike(
            node = node,
            label = link.children.firstOrNull { it.type == MarkdownElementTypes.LINK_TEXT }
                ?: link.children.firstOrNull { it.type == MarkdownElementTypes.LINK_LABEL },
            destination = null,
        )
    }

    private inline fun styled(style: SpanStyle, content: () -> Unit) {
        val start = emitter.length
        content()
        emitter.addSpan(style, start)
    }

    /** Styles [content] and marks it literal, as code is. */
    private inline fun literal(style: SpanStyle, content: () -> Unit) {
        val start = emitter.length
        content()
        emitter.addSpan(style, start)
        emitter.addAnnotation(AnnotationTagLiteral, "", start)
    }

    private fun ASTNode.text(): CharSequence = getTextInNode(source)
}

/** Appends source text with its escapes and character references resolved. */
private fun MarkdownEmitter.appendText(value: CharSequence) {
    value.toString().resolveMarkdownText().split('\n').forEachIndexed { index, line ->
        if (index > 0) appendLineBreak()
        append(line)
    }
}

/**
 * Resolves the backslash escapes and character references the parser leaves in place, in a link's
 * destination as much as in the text.
 */
private fun String.resolveMarkdownText(): String = buildString {
    val source = this@resolveMarkdownText
    var index = 0
    while (index < source.length) {
        val char = source[index]
        val next = source.getOrNull(index + 1)
        val reference = if (char == '&') source.characterReferenceAt(index) else null
        when {
            char == '\\' && next != null && next in EscapablePunctuation -> {
                append(next)
                index += 2
            }

            reference != null -> {
                append(reference.first)
                index += reference.second
            }

            else -> {
                append(char)
                index++
            }
        }
    }
}

/**
 * Decodes the character reference at [start] into its text and length, or null when what follows
 * the `&` is not one. Only the numeric forms and the named ones below are decoded.
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
    // An invalid code point, a surrogate included, becomes the replacement character.
    val invalid = codePoint <= 0 ||
        codePoint > Character.MAX_CODE_POINT ||
        codePoint in MinSurrogate..MaxSurrogate
    if (invalid) return ReplacementCharacter to length
    return String(Character.toChars(codePoint)) to length
}

/**
 * Turns a link destination into something openable, or null when it is not. A fragment or a path
 * only means something inside a document, and a URL invented from one fails when tapped.
 *
 * A dotted destination with no path is taken for a host, so `getstream.io` links. A file name like
 * `readme.md` cannot be told apart and links too, as it does in the View-based and iOS kits.
 */
private fun String.toOpenableUrl(): String? {
    val destination = removeSurrounding("<", ">").trim()
    return when {
        destination.isEmpty() || destination.any(Char::isWhitespace) -> null
        destination.hasOpenableScheme() -> destination.lowercaseScheme()
        destination.contains('@') && !destination.contains('/') -> "mailto:$destination"
        // Also reached by a host carrying a port, which reads as a scheme above. HostPattern is
        // anchored on a dotted host, so a hostile scheme cannot match here.
        HostPattern.containsMatchIn(destination) -> "https://$destination"
        else -> null
    }
}

/**
 * A tapped link is handed to the system, so only a scheme a message has business carrying is
 * annotated. Otherwise text reading as ordinary prose could open a `javascript:` or `intent://`
 * target, or deep link into the host app.
 */
/** Android matches an intent filter's scheme case-sensitively, so it has to be lowercase. */
private fun String.lowercaseScheme(): String {
    val separator = indexOf(':')
    return substring(0, separator).lowercase() + substring(separator)
}

private fun String.hasOpenableScheme(): Boolean =
    OpenableSchemes.any { startsWith(it, ignoreCase = true) }

/**
 * Both spellings of a hard break: the marker left by trailing spaces or a backslash, and the tag.
 * Either absorbs the line feed it sits on, so ending a line with one breaks it once.
 */
private fun ASTNode.isHardBreak(source: String): Boolean = when (type) {
    MarkdownTokenTypes.HARD_LINE_BREAK -> true
    MarkdownTokenTypes.HTML_TAG -> getTextInNode(source).toString().isLineBreakTag()
    else -> false
}

private fun String.isLineBreakTag(): Boolean = LineBreakTagPattern.matches(trim())

private fun ASTNode.isWhitespace(): Boolean = type == MarkdownTokenTypes.WHITE_SPACE

private val ItalicSpan = SpanStyle(fontStyle = FontStyle.Italic)
private val BoldSpan = SpanStyle(fontWeight = FontWeight.Bold)
private val StrikethroughSpan = SpanStyle(textDecoration = TextDecoration.LineThrough)

private const val UnorderedListMarker = "•"

/** Blocks are separated by at most a blank line, however many breaks the source holds. */
private const val MaxBlockBreaks = 2

private val OpenableSchemes = listOf("http://", "https://", "mailto:", "tel:")
private val HostPattern = Regex("^[\\w\\-]+(\\.[\\w\\-]+)+")
private val LineBreakTagPattern = Regex("<br\\s*/?>", RegexOption.IGNORE_CASE)
private const val EscapablePunctuation = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~"
private const val MaxCharacterReferenceLength = 32
private const val ReplacementCharacter = "\uFFFD"
private const val MinSurrogate = 0xD800
private const val MaxSurrogate = 0xDFFF

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
private val QuoteMarkers = Regex("^(?:> ?)+")
private val ImageLinkTypes = setOf(
    MarkdownElementTypes.INLINE_LINK,
    MarkdownElementTypes.FULL_REFERENCE_LINK,
    MarkdownElementTypes.SHORT_REFERENCE_LINK,
)

/** CommonMark strips the indentation that declared an indented code block, in either spelling. */
private fun String.stripCodeIndent(): String = when {
    startsWith(IndentedCodeSpaces) -> removePrefix(IndentedCodeSpaces)
    else -> removePrefix("\t")
}

private const val IndentedCodeSpaces = "    "
