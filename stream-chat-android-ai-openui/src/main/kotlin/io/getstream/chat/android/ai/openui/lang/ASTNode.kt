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

/** Whether a runtime reference points at a `Query` or a `Mutation` declaration. */
internal enum class RuntimeRefType(val wireName: String) {
    Query("query"),
    Mutation("mutation"),
}

/**
 * Expression tree produced by the OpenUI Lang parser (mirrors `ASTNode` in `@openuidev/lang-core`).
 *
 * Literal & structural nodes:
 * - [Comp] — a call: a component, built-in, library function or action step: `Header("Hello")`, `@Count(rows)`
 * - [Str], [Num], [Bool], [Null] — literals
 * - [Arr], [Obj] — array and object literals
 * - [Ref] — a reference to another statement
 * - [Ph] — a placeholder for an unresolvable reference
 *
 * Reactive & expression nodes (preserved for runtime evaluation):
 * - [StateRef] — `$variable`
 * - [RuntimeRef] — a reference to a `Query`/`Mutation` result
 * - [BinOp], [UnaryOp], [Ternary]
 * - [Member] (`a.b`), [Index] (`a[b]`), [Assign] (`$a = expr`)
 */
internal sealed interface ASTNode {
    /**
     * A call. [mappedProps] holds the args named by the library schema (set by the materializer).
     * [isFunction] marks an `@Name(...)` call whose name is not a built-in.
     */
    data class Comp(
        val name: String,
        val args: List<ASTNode>,
        val mappedProps: Map<String, ASTNode>? = null,
        val isFunction: Boolean = false,
    ) : ASTNode

    data class Str(val value: String) : ASTNode
    data class Num(val value: Double) : ASTNode
    data class Bool(val value: Boolean) : ASTNode
    data object Null : ASTNode
    data class Arr(val elements: List<ASTNode>) : ASTNode

    /** An object literal. Entries keep source order and may repeat a key (the last one wins). */
    data class Obj(val entries: List<Pair<String, ASTNode>>) : ASTNode
    data class Ref(val name: String) : ASTNode
    data class Ph(val name: String) : ASTNode
    data class StateRef(val name: String) : ASTNode
    data class RuntimeRef(val name: String, val refType: RuntimeRefType) : ASTNode
    data class BinOp(val op: String, val left: ASTNode, val right: ASTNode) : ASTNode
    data class UnaryOp(val op: String, val operand: ASTNode) : ASTNode
    data class Ternary(val cond: ASTNode, val then: ASTNode, val otherwise: ASTNode) : ASTNode
    data class Member(val obj: ASTNode, val field: String) : ASTNode
    data class Index(val obj: ASTNode, val index: ASTNode) : ASTNode
    data class Assign(val target: String, val value: ASTNode) : ASTNode

    /** True for nodes that survive parser lowering and are resolved by the evaluator. */
    val isRuntimeExpression: Boolean
        get() = when (this) {
            is StateRef, is RuntimeRef, is BinOp, is UnaryOp, is Ternary, is Member, is Index, is Assign -> true
            else -> false
        }

    /** The JSON shape used by the reference implementation (`{ k: "Comp", name, args, ... }`). */
    val jsonValue: OpenUIValue
        get() = when (this) {
            is Comp -> OpenUIValue.obj(
                "k" to OpenUIValue.Str("Comp"),
                "name" to OpenUIValue.Str(name),
                "args" to OpenUIValue.Arr(args.map { it.jsonValue }),
                "fn" to if (isFunction) OpenUIValue.of(true) else null,
                "mappedProps" to mappedProps?.let { props ->
                    OpenUIValue.Obj(OpenUIObject(props.mapValues { it.value.jsonValue }))
                },
            )
            is Str -> node("Str", "v" to OpenUIValue.Str(value))
            is Num -> node("Num", "v" to if (value.isFinite()) OpenUIValue.Num(value) else OpenUIValue.Null)
            is Bool -> node("Bool", "v" to OpenUIValue.of(value))
            Null -> node("Null")
            is Arr -> node("Arr", "els" to OpenUIValue.Arr(elements.map { it.jsonValue }))
            is Obj -> node(
                "Obj",
                "entries" to
                    OpenUIValue.Arr(
                        entries.map { (key, value) -> OpenUIValue.arr(OpenUIValue.Str(key), value.jsonValue) },
                    ),
            )
            is Ref -> node("Ref", "n" to OpenUIValue.Str(name))
            is Ph -> node("Ph", "n" to OpenUIValue.Str(name))
            is StateRef -> node("StateRef", "n" to OpenUIValue.Str(name))
            is RuntimeRef -> node(
                "RuntimeRef",
                "n" to OpenUIValue.Str(name),
                "refType" to OpenUIValue.Str(refType.wireName),
            )
            is BinOp -> node("BinOp", "op" to OpenUIValue.Str(op), "left" to left.jsonValue, "right" to right.jsonValue)
            is UnaryOp -> node("UnaryOp", "op" to OpenUIValue.Str(op), "operand" to operand.jsonValue)
            is Ternary -> node(
                "Ternary",
                "cond" to cond.jsonValue,
                "then" to then.jsonValue,
                "else" to otherwise.jsonValue,
            )
            is Member -> node("Member", "obj" to obj.jsonValue, "field" to OpenUIValue.Str(this.field))
            is Index -> node("Index", "obj" to obj.jsonValue, "index" to index.jsonValue)
            is Assign -> node("Assign", "target" to OpenUIValue.Str(target), "value" to value.jsonValue)
        }

    /** Visits this node and all of its descendants (pre-order). */
    fun walk(visit: (ASTNode) -> Unit) {
        visit(this)
        when (this) {
            is Comp -> {
                args.forEach { it.walk(visit) }
                // Mapped props mostly hold the args themselves; walking those twice would double the
                // work at every nesting level.
                mappedProps?.values?.forEach { prop -> if (args.none { it === prop }) prop.walk(visit) }
            }
            is Arr -> elements.forEach { it.walk(visit) }
            is Obj -> entries.forEach { it.second.walk(visit) }
            is BinOp -> {
                left.walk(visit)
                right.walk(visit)
            }
            is UnaryOp -> operand.walk(visit)
            is Ternary -> {
                cond.walk(visit)
                then.walk(visit)
                otherwise.walk(visit)
            }
            is Member -> obj.walk(visit)
            is Index -> {
                obj.walk(visit)
                index.walk(visit)
            }
            is Assign -> value.walk(visit)
            is Str, is Num, is Bool, Null, is Ref, is Ph, is StateRef, is RuntimeRef -> Unit
        }
    }

    /** Names of all `$variables` referenced by this expression, in first-seen order. */
    val stateReferences: List<String>
        get() {
            val result = LinkedHashSet<String>()
            walk { if (it is StateRef) result += it.name }
            return result.toList()
        }

    /** The value of a literal AST, or `null` when it holds a reference, call or expression. */
    val literalValue: OpenUIValue?
        get() {
            return when (this) {
                is Str -> OpenUIValue.Str(value)
                is Num -> OpenUIValue.Num(value)
                is Bool -> OpenUIValue.of(value)
                Null -> OpenUIValue.Null
                is Arr -> OpenUIValue.Arr(elements.map { it.literalValue ?: return null })
                is Obj -> {
                    val fields = OpenUIObject.Builder()
                    for ((key, node) in entries) fields[key] = node.literalValue ?: return null
                    OpenUIValue.Obj(fields.build())
                }
                else -> null
            }
        }

    /** Factory for literal nodes. */
    companion object {
        /** Converts a runtime value back into a literal AST node (`toLiteralAST`). */
        fun literal(value: OpenUIValue?): ASTNode = when (value) {
            null, OpenUIValue.Null -> Null
            is OpenUIValue.Str -> Str(value.value)
            is OpenUIValue.Num -> Num(value.value)
            is OpenUIValue.Bool -> Bool(value.value)
            is OpenUIValue.Arr -> Arr(value.items.map(::literal))
            is OpenUIValue.Obj -> Obj(value.fields.map { (key, item) -> key to literal(item) })
            is OpenUIValue.Element -> literal(value.node.jsonValue)
            is OpenUIValue.Expression -> literal(value.node.jsonValue)
        }
    }
}

private fun node(kind: String, vararg fields: Pair<String, OpenUIValue>): OpenUIValue =
    OpenUIValue.obj("k" to OpenUIValue.Str(kind), *fields)

/** Statement kinds, classified at parse time (mirrors `Statement` in `ast.ts`). */
internal sealed interface Statement {
    val id: String

    /** The expression stored in the symbol table for this statement. */
    val symbol: ASTNode

    data class Value(override val id: String, val expr: ASTNode) : Statement {
        override val symbol: ASTNode get() = expr
    }

    data class State(override val id: String, val initial: ASTNode) : Statement {
        override val symbol: ASTNode get() = initial
    }

    data class Query(override val id: String, val args: List<ASTNode>, val expr: ASTNode, val deps: List<String>?) :
        Statement {
        override val symbol: ASTNode get() = expr
    }

    data class Mutation(override val id: String, val args: List<ASTNode>, val expr: ASTNode) : Statement {
        override val symbol: ASTNode get() = expr
    }
}
