package com.getstream.sdk.chat.sidebar

import com.getstream.sdk.chat.sidebar.utils.substituteLinesInFile
import java.io.File
import kotlin.io.path.ExperimentalPathApi

@ExperimentalPathApi
fun fixFirstIndexLinksForFile(file: File, modulesToFix: List<String>) {
    file.substituteLinesInFile { line ->
        fixLink(line, modulesToFix)
    }
}

private fun fixLink(line: String, modulesToFix: List<String>): String {
    var linkState = NavigationLinkState.NO_LINK

    var contentStart = 0
    var contentEnd = 0
    var linkStart = 0
    var linkEnd = 0

    line.forEachIndexed { i, char ->
        when {
            linkState == NavigationLinkState.NO_LINK && char == '/' -> {
                linkState = NavigationLinkState.COMMENT_1
            }

            linkState == NavigationLinkState.COMMENT_1 && char == '/' && line[i - 1] == '/' -> {
                linkState = NavigationLinkState.COMMENT_2
            }

            linkState == NavigationLinkState.COMMENT_2 && char == '[' && line[i - 1] == '/' -> {
                linkState = NavigationLinkState.INSIDE_CONTENT
                contentStart = i + 1
            }

            linkState == NavigationLinkState.INSIDE_CONTENT && char == ']' -> {
                linkState = NavigationLinkState.OUTSIDE_CONTENT
                contentEnd = i
            }

            linkState == NavigationLinkState.OUTSIDE_CONTENT && char == '(' -> {
                linkState = NavigationLinkState.INSIDE_LINK
                linkStart = i + 1
            }

            linkState == NavigationLinkState.INSIDE_LINK && char == ')' -> {
                linkEnd = i

                return if (shouldBeReduced(line, contentStart, contentEnd, linkStart, linkEnd, modulesToFix)) {
                    reduceParentDir(line, linkStart, linkEnd)
                } else {
                    line
                }
            }
        }
    }

    return line
}

private fun shouldBeReduced(
    line: String,
    contentStart : Int,
    contentEnd : Int,
    linkStart: Int,
    linkEnd: Int,
    modulesToFix: List<String>
): Boolean {
    val linkContent = line.subSequence(contentStart, contentEnd)
    val linkDestiny = line.subSequence(linkStart, linkEnd)

    return modulesToFix.any { module ->
        module == linkContent
    } && linkDestiny.contains("../index.md")
}


private fun reduceParentDir(line: String, linkStart: Int, linkEnd: Int): String {
    return StringBuilder().apply {
        append(line.subSequence(0, linkStart - 1))
        append("(")
        append(line.subSequence(linkStart + parentPathSize(), linkEnd))
        append(line.subSequence(linkEnd, line.length))
    }.toString()
}

private fun parentPathSize() = "../".length

private enum class NavigationLinkState {
    NO_LINK, COMMENT_1, COMMENT_2, INSIDE_CONTENT, OUTSIDE_CONTENT, INSIDE_LINK
}
