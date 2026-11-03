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

package io.getstream.chat.android.ai.openui.lang

/** A raw `identifier = expression` statement before expression parsing. */
internal data class RawStatement(
    val id: String,
    /** Token type of the left-hand identifier, used to classify the statement. */
    val idTokenType: TokenType,
    val tokens: List<Token>,
)

/** Statement splitting and auto-closing (port of `statements.ts`). */
internal object Statements {

    /**
     * Auto-closes unclosed strings and brackets so that partial (streaming) input parses.
     * Returns the closed text and whether anything had to be closed.
     */
    @Suppress("LoopWithTooManyJumpStatements")
    fun autoClose(input: String): Pair<String, Boolean> {
        val stack = ArrayList<Char>()
        var inString: Char? = null
        var escaped = false
        for (c in input) {
            if (escaped) {
                escaped = false
                continue
            }
            if (c == '\\' && inString != null) {
                escaped = true
                continue
            }
            if (inString != null) {
                if (c == inString) inString = null
                continue
            }
            when (c) {
                '"', '\'' -> inString = c
                '(', '[', '{' -> stack += c
                ')' -> if (stack.lastOrNull() == '(') stack.removeAt(stack.lastIndex)
                ']' -> if (stack.lastOrNull() == '[') stack.removeAt(stack.lastIndex)
                '}' -> if (stack.lastOrNull() == '{') stack.removeAt(stack.lastIndex)
            }
        }
        val incomplete = inString != null || stack.isNotEmpty()
        if (!incomplete) return input to false

        val out = StringBuilder(input)
        if (inString != null) {
            if (escaped) out.append('\\')
            out.append(inString)
        }
        for (open in stack.asReversed()) {
            out.append(
                when (open) {
                    '(' -> ')'
                    '[' -> ']'
                    else -> '}'
                },
            )
        }
        return out.toString() to true
    }

    /**
     * Splits the token stream into statements. Each statement is `identifier = expression`,
     * separated by newlines at bracket depth 0. A newline followed by `?` (or `:` inside a
     * ternary) continues the statement. Lines without an identifier and `=` are skipped.
     */
    @Suppress("LoopWithTooManyJumpStatements", "NestedBlockDepth")
    fun split(tokens: List<Token>): List<RawStatement> {
        val statements = ArrayList<RawStatement>()
        var pos = 0

        fun skipLine() {
            while (pos < tokens.size && tokens[pos].type != TokenType.Newline && tokens[pos].type != TokenType.EOF) {
                pos++
            }
        }

        while (pos < tokens.size) {
            while (pos < tokens.size && tokens[pos].type == TokenType.Newline) pos++
            if (pos >= tokens.size || tokens[pos].type == TokenType.EOF) break

            val token = tokens[pos]
            if (token.type != TokenType.Ident && token.type != TokenType.Type && token.type != TokenType.StateVar) {
                skipLine()
                continue
            }
            val id = token.text!!
            pos++
            if (pos >= tokens.size || tokens[pos].type != TokenType.Equals) {
                skipLine()
                continue
            }
            pos++

            // Collect expression tokens until a depth-0 newline, tracking brackets and ternaries
            // so multi-line ternaries are not split into separate statements.
            val expression = ArrayList<Token>()
            var depth = 0
            var ternaryDepth = 0
            while (pos < tokens.size && tokens[pos].type != TokenType.EOF) {
                val type = tokens[pos].type
                if (type == TokenType.Newline && depth <= 0 && ternaryDepth <= 0) {
                    var peek = pos + 1
                    while (peek < tokens.size && tokens[peek].type == TokenType.Newline) peek++
                    val next = if (peek < tokens.size) tokens[peek].type else TokenType.EOF
                    if (next == TokenType.Question || (next == TokenType.Colon && ternaryDepth > 0)) {
                        pos++
                        continue
                    }
                    break
                }
                if (type == TokenType.Newline) {
                    pos++
                    continue
                }
                when {
                    type == TokenType.LParen || type == TokenType.LBrack || type == TokenType.LBrace -> depth++
                    (type == TokenType.RParen || type == TokenType.RBrack || type == TokenType.RBrace) &&
                        depth > 0 -> depth--
                    type == TokenType.Question && depth == 0 -> ternaryDepth++
                    type == TokenType.Colon && depth == 0 && ternaryDepth > 0 -> ternaryDepth--
                }
                expression += tokens[pos++]
            }
            if (expression.isNotEmpty()) statements += RawStatement(id, token.type, expression)
        }
        return statements
    }
}
