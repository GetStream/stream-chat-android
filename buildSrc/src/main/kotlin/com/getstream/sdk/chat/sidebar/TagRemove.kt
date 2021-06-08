package com.getstream.sdk.chat.sidebar

fun filterTagLinks(line: String): String {
    var currentLine = line

    while (hasRemovableIndexLink(currentLine)) {
        currentLine = filterTagLink(currentLine)
    }

    return currentLine
}

private fun filterTagLink(line: String): String {
    var removalInit = 0
    var removalEnd = 0
    var contentInit = 0
    var contentEnd = 0
    var linkInit = 0
    var linkEnd = 0

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

            tagState == TagState.NO_TAG && char == '[' -> {
                tagState = TagState.INSIDE_MD_LINK_CONTENT
                removalInit = i
                contentInit = i + 1
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
                linkInit = i + 1
            }

            tagState == TagState.INSIDE_LINK && char == ')' -> {
                tagState = TagState.NO_TAG
                linkEnd = i
                removalEnd = i

                val content = line.substring(contentInit, contentEnd)
                val linkContent = line.substring(linkInit, linkEnd)

                if (isRemovableLink(linkContent)) {
                    return "${line.substring(0, removalInit)}$content${line.substring(removalEnd + 1, line.length)}"
                }
            }
        }

    }

    return line
}

private fun hasRemovableIndexLink(line: String): Boolean {
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

            tagState == TagState.OUTSIDE_HTML_TAG || tagState == TagState.NO_TAG && char == '[' -> {
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

                val linkContent = line.substring(linkInit, linkEnd)

                if (isRemovableLink(linkContent)) {
                    return true
                }
            }
        }
    }

    return false
}

private fun isRemovableLink(linkContent: String): Boolean {
    return linkContent.contains(".md") &&
        !linkContent.contains("index.md") ||
        linkContent.contains("FFunctions")
}

enum class TagState {
    NO_TAG,
    INSIDE_HTML_TAG,
    OUTSIDE_HTML_TAG,
    INSIDE_MD_LINK_CONTENT,
    OUTSIDE_MD_LINK_CONTENT,
    INSIDE_LINK
}
