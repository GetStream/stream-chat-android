package com.getstream.sdk.chat.sidebar

fun filterTagLinks(line: String): String {
    var currentLine = line

    while (hasNonIndexLink(currentLine)) {
        currentLine = filterTagLink(currentLine)
    }

    return currentLine
}

private fun filterTagLink(line: String): String {
    var removalInit = 0
    var removalEnd = 0
    var contentInit = 0
    var contentEnd = 0

    var tagState = TagState.NO_TAG

    line.forEachIndexed { i, char ->
        when {
            tagState == TagState.NO_TAG && char == '<' && line.subSequence(i, i + 2) == "<a" -> {
                removalInit = i
                tagState = TagState.INSIDE_HTML_TAG
            }

            tagState == TagState.INSIDE_HTML_TAG && char == '>' && line.subSequence(i - 3, i + 1) == "</a>" -> {
                tagState = TagState.OUTSIDE_HTML_TAG
            }

            tagState == TagState.OUTSIDE_HTML_TAG && char == '[' -> {
                tagState = TagState.INSIDE_MD_LINK_CONTENT
                contentInit = i + 1
            }

            tagState == TagState.INSIDE_MD_LINK_CONTENT && char == ']' -> {
                tagState = TagState.OUTSIDE_MD_LINK_CONTENT
                contentEnd = i
            }

            tagState == TagState.OUTSIDE_MD_LINK_CONTENT && char == '(' -> {
                tagState = TagState.INSIDE_LINK
            }

            tagState == TagState.INSIDE_LINK && char == ')' -> {
                tagState = TagState.NO_TAG
                removalEnd = i
                return@forEachIndexed
            }
        }

    }

    val content = line.substring(contentInit, contentEnd)

    return "${line.substring(0, removalInit)}$content${line.substring(removalEnd + 1, line.length)}"
}

private fun hasNonIndexLink(line: String): Boolean {
    var tagState = TagState.NO_TAG

    var linkInit = 0
    var linkEnd = 0

    line.forEachIndexed { i, char ->
        when {
            tagState == TagState.NO_TAG && char == '<' && line.subSequence(i, i + 2) == "<a" -> {
                tagState = TagState.INSIDE_HTML_TAG
            }

            tagState == TagState.INSIDE_HTML_TAG && char == '>' && line.subSequence(i - 3, i + 1) == "</a>" -> {
                tagState = TagState.OUTSIDE_HTML_TAG
            }

            tagState == TagState.OUTSIDE_HTML_TAG && char == '[' -> {
                tagState = TagState.INSIDE_MD_LINK_CONTENT
            }

            tagState == TagState.INSIDE_MD_LINK_CONTENT && char == ']' -> {
                tagState = TagState.OUTSIDE_MD_LINK_CONTENT
            }

            tagState == TagState.OUTSIDE_MD_LINK_CONTENT && char == '(' -> {
                tagState = TagState.INSIDE_LINK
                linkInit = i + 1
            }

            tagState == TagState.INSIDE_LINK && char == ')' -> {
                tagState = TagState.NO_TAG

                linkEnd = i

                if (!line.substring(linkInit, linkEnd).contains("index.md")) {
                    return true
                }
            }
        }
    }

    return false
}

enum class TagState {
    NO_TAG,
    INSIDE_HTML_TAG,
    OUTSIDE_HTML_TAG,
    INSIDE_MD_LINK_CONTENT,
    OUTSIDE_MD_LINK_CONTENT,
    INSIDE_LINK
}
