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

/** Pratt (top-down operator precedence) expression parser (port of `expressions.ts`). */
internal class ExpressionParser private constructor(private val tokens: List<Token>) {
    private var pos = 0
    private var depth = 0

    private fun current(): Token = tokens.getOrNull(pos) ?: EOF
    private fun peekType(offset: Int): TokenType? = tokens.getOrNull(pos + offset)?.type

    private fun advance(): Token = current().also { pos++ }

    private fun eat(type: TokenType) {
        if (current().type == type) pos++
    }

    private fun infixPrecedence(type: TokenType): Int = when (type) {
        TokenType.Question -> PREC_TERNARY
        TokenType.Or -> PREC_OR
        TokenType.And -> PREC_AND
        TokenType.EqEq, TokenType.NotEq -> PREC_EQ
        TokenType.Greater, TokenType.Less, TokenType.GreaterEq, TokenType.LessEq -> PREC_CMP
        TokenType.Plus, TokenType.Minus -> PREC_ADD
        TokenType.Star, TokenType.Slash, TokenType.Percent -> PREC_MUL
        TokenType.Dot, TokenType.LBrack -> PREC_MEMBER
        else -> 0
    }

    private fun parseExpression(minPrecedence: Int = 0): ASTNode {
        // A statement nested deeper than any real program reads as null instead of running out of stack.
        if (depth >= MAX_DEPTH) {
            pos = tokens.size
            return ASTNode.Null
        }
        depth++
        // Each link of a chain like `a + b + c` or `x.a.b` nests the expression one level deeper too.
        var links = 0
        try {
            var left = parsePrefix()
            while (infixPrecedence(current().type) > minPrecedence) {
                if (depth >= MAX_DEPTH) {
                    pos = tokens.size
                    return ASTNode.Null
                }
                depth++
                links++
                left = parseInfix(left)
            }
            return left
        } finally {
            depth -= links + 1
        }
    }

    private fun parsePrefix(): ASTNode {
        val token = current()
        return when (token.type) {
            TokenType.Str -> advance().let { ASTNode.Str(token.text!!) }
            TokenType.Num -> advance().let { ASTNode.Num(token.number) }
            TokenType.True -> advance().let { ASTNode.Bool(true) }
            TokenType.False -> advance().let { ASTNode.Bool(false) }
            TokenType.Null -> advance().let { ASTNode.Null }
            TokenType.LBrack -> parseArray()
            TokenType.LBrace -> parseObject()
            TokenType.StateVar -> {
                advance()
                // `$var = expr` (Equals, not EqEq) is an assignment.
                if (current().type == TokenType.Equals) {
                    advance()
                    ASTNode.Assign(token.text!!, parseExpression(0))
                } else {
                    ASTNode.StateRef(token.text!!)
                }
            }
            TokenType.Type -> {
                val name = token.text!!
                // Builtins (Count, Each, Set, ...) need the @ prefix; only Action is exempt.
                if (peekType(1) == TokenType.LParen && (!Builtins.isBuiltin(name) || name == "Action")) {
                    parseCall()
                } else {
                    advance()
                    ASTNode.Ref(name)
                }
            }
            TokenType.BuiltinCall -> {
                if (peekType(1) == TokenType.LParen) {
                    // `@Name(...)` that is not a built-in calls one of the library's functions or actions.
                    val call = parseCall()
                    if (Builtins.isBuiltin(call.name)) call else call.copy(isFunction = true)
                } else {
                    advance()
                    ASTNode.Ref(token.text!!)
                }
            }
            TokenType.Ident -> advance().let { ASTNode.Ref(token.text!!) }
            TokenType.Not -> {
                advance()
                ASTNode.UnaryOp("!", parseExpression(PREC_UNARY))
            }
            TokenType.Minus -> {
                advance()
                ASTNode.UnaryOp("-", parseExpression(PREC_UNARY))
            }
            TokenType.LParen -> {
                advance()
                val inner = parseExpression(0)
                eat(TokenType.RParen)
                inner
            }
            else -> {
                // Unknown token: skip it and produce null.
                advance()
                ASTNode.Null
            }
        }
    }

    private fun parseInfix(left: ASTNode): ASTNode {
        val token = current()
        binaryOperators[token.type]?.let { (op, precedence) ->
            advance()
            return ASTNode.BinOp(op, left, parseExpression(precedence))
        }
        return when (token.type) {
            TokenType.Question -> {
                // Ternary: cond ? then : else (right-associative).
                advance()
                val then = parseExpression(0)
                eat(TokenType.Colon)
                ASTNode.Ternary(left, then, parseExpression(0))
            }
            TokenType.Dot -> {
                advance()
                val fieldToken = current()
                advance()
                val field = when (fieldToken.type) {
                    TokenType.Ident, TokenType.Type, TokenType.Str, TokenType.Num -> fieldToken.valueString
                    TokenType.StateVar -> fieldToken.text!!.removePrefix("$")
                    else -> "?"
                }
                ASTNode.Member(left, field)
            }
            TokenType.LBrack -> {
                advance()
                val index = parseExpression(0)
                eat(TokenType.RBrack)
                ASTNode.Index(left, index)
            }
            else -> left
        }
    }

    /** `Name(arg1, arg2, ...)` */
    private fun parseCall(): ASTNode.Comp {
        val name = advance().text!!
        eat(TokenType.LParen)
        val args = ArrayList<ASTNode>()
        while (current().type != TokenType.RParen && current().type != TokenType.EOF) {
            args += parseExpression(0)
            if (current().type == TokenType.Comma) advance()
        }
        eat(TokenType.RParen)
        return ASTNode.Comp(name, args)
    }

    /** `[a, b, ...]` */
    private fun parseArray(): ASTNode {
        advance()
        val elements = ArrayList<ASTNode>()
        while (current().type != TokenType.RBrack && current().type != TokenType.EOF) {
            elements += parseExpression(0)
            if (current().type == TokenType.Comma) advance()
        }
        eat(TokenType.RBrack)
        return ASTNode.Arr(elements)
    }

    /** `{ key: value, ... }` */
    private fun parseObject(): ASTNode {
        advance()
        val entries = ArrayList<Pair<String, ASTNode>>()
        while (current().type != TokenType.RBrace && current().type != TokenType.EOF) {
            val keyToken = current()
            advance()
            val key = when (keyToken.type) {
                TokenType.Ident, TokenType.Str, TokenType.Type, TokenType.Num -> keyToken.valueString
                TokenType.StateVar -> keyToken.text!!.removePrefix("$")
                else -> "?"
            }
            eat(TokenType.Colon)
            entries += key to parseExpression(0)
            if (current().type == TokenType.Comma) advance()
        }
        eat(TokenType.RBrace)
        return ASTNode.Obj(entries)
    }

    companion object {
        /** How deep one statement's expressions nest. */
        internal const val MAX_DEPTH = 256

        private const val PREC_TERNARY = 1
        private const val PREC_OR = 2
        private const val PREC_AND = 3
        private const val PREC_EQ = 4
        private const val PREC_CMP = 5
        private const val PREC_ADD = 6
        private const val PREC_MUL = 7
        private const val PREC_UNARY = 8
        private const val PREC_MEMBER = 9

        private val EOF = Token(TokenType.EOF)

        private val binaryOperators = mapOf(
            TokenType.Plus to ("+" to PREC_ADD),
            TokenType.Minus to ("-" to PREC_ADD),
            TokenType.Star to ("*" to PREC_MUL),
            TokenType.Slash to ("/" to PREC_MUL),
            TokenType.Percent to ("%" to PREC_MUL),
            TokenType.EqEq to ("==" to PREC_EQ),
            TokenType.NotEq to ("!=" to PREC_EQ),
            TokenType.Greater to (">" to PREC_CMP),
            TokenType.Less to ("<" to PREC_CMP),
            TokenType.GreaterEq to (">=" to PREC_CMP),
            TokenType.LessEq to ("<=" to PREC_CMP),
            TokenType.And to ("&&" to PREC_AND),
            TokenType.Or to ("||" to PREC_OR),
        )

        fun parse(tokens: List<Token>): ASTNode = ExpressionParser(tokens).parseExpression(0)
    }
}
