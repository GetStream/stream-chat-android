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

/** Mutable state threaded through one materialization pass (`MaterializeCtx`). */
internal class MaterializeContext(
    val symbols: Map<String, ASTNode>,
    val catalog: ParamMap?,
    val partial: Boolean,
    /** Statement being materialized, for error attribution. */
    var currentStatementId: String?,
    /** Statement ids not yet reached; what remains is orphaned. */
    val unreached: MutableSet<String>,
) {
    val errors = ArrayList<ValidationError>()
    val unresolved = ArrayList<String>()
    val visited = HashSet<String>()

    /** How deep materializing has recursed, references included. */
    var depth = 0

    /** How many nodes materializing has visited, each reference counted every time it is inlined. */
    var nodes = 0
}

/** What validating a value against a schema fragment produced. */
internal sealed interface Checked {
    /** The value to keep (possibly a cleaned copy); `null` means drop the key. */
    data class Keep(val value: OpenUIValue?) : Checked

    /** Unsalvageable; the caller applies the edge rule one level up. */
    data object Invalid : Checked
}

/** Structured validation issues; every message of a kind comes from one template. */
internal sealed interface ValidationIssue {
    data class TypeMismatch(val expected: String, val actual: String) : ValidationIssue
    data class MissingRequired(val signature: String? = null) : ValidationIssue
    data class NullRequired(val signature: String? = null) : ValidationIssue
    data class UnknownComponent(val available: List<String>?) : ValidationIssue
    data object InlineReserved : ValidationIssue
    data class ExcessArgs(val declared: Int, val got: Int) : ValidationIssue
    data object UnknownFunction : ValidationIssue
    data class NoRoot(val reason: NoRootReason) : ValidationIssue
}

/** Why a program has no root to render. */
internal enum class NoRootReason {
    /** No `root` statement and no statement calls the root component. */
    Missing,

    /** The entry doesn't evaluate to a component. */
    NotComponent,

    /** No `root` statement; the first statement calling the root component renders instead. */
    Fallback,
}

/** Recursive JSON Schema validation of materialized props (port of `validation.ts`). */
@Suppress("TooManyFunctions")
internal object SchemaValidation {

    private fun ValidationIssue.code(): ValidationErrorCode = when (this) {
        is ValidationIssue.TypeMismatch -> ValidationErrorCode.TypeMismatch
        is ValidationIssue.MissingRequired -> ValidationErrorCode.MissingRequired
        is ValidationIssue.NullRequired -> ValidationErrorCode.NullRequired
        is ValidationIssue.UnknownComponent -> ValidationErrorCode.UnknownComponent
        ValidationIssue.InlineReserved -> ValidationErrorCode.InlineReserved
        is ValidationIssue.ExcessArgs -> ValidationErrorCode.ExcessArgs
        ValidationIssue.UnknownFunction -> ValidationErrorCode.UnknownFunction
        is ValidationIssue.NoRoot -> ValidationErrorCode.NoRoot
    }

    private fun message(component: String, path: String, issue: ValidationIssue): String = when (issue) {
        is ValidationIssue.TypeMismatch -> "field \"$path\" expects ${issue.expected} but got ${issue.actual}"
        is ValidationIssue.MissingRequired ->
            "missing required field \"$path\"" + (issue.signature?.let { " — signature: $it" } ?: "")
        is ValidationIssue.NullRequired ->
            "required field \"$path\" cannot be null" + (issue.signature?.let { " — signature: $it" } ?: "")
        is ValidationIssue.UnknownComponent ->
            "Unknown component \"$component\" — not found in catalog or builtins" +
                (
                    issue.available?.takeIf { it.isNotEmpty() }?.let {
                        ". Available components: ${it.joinToString(
                            ", ",
                        )}"
                    }
                        ?: ""
                    )
        ValidationIssue.InlineReserved ->
            "$component() must be declared as a top-level statement, not used inline " +
                "as a value"
        is ValidationIssue.ExcessArgs ->
            "$component takes ${issue.declared} arg(s), got ${issue.got} (${issue.got - issue.declared} excess dropped)"
        ValidationIssue.UnknownFunction ->
            "Unknown function \"@$component\": not a built-in or action step. The call evaluates to null"
        is ValidationIssue.NoRoot -> when (issue.reason) {
            NoRootReason.NotComponent -> "root does not evaluate to a component"
            NoRootReason.Fallback ->
                "No root statement. Rendering the first statement that calls the root component $component"
            NoRootReason.Missing -> if (component.isEmpty()) {
                "No root statement"
            } else {
                "No root statement, and no statement calls the root component $component"
            }
        }
    }

    /** Appends a validation error, deriving its message from the structured issue. */
    fun push(context: MaterializeContext, component: String, path: String, issue: ValidationIssue) {
        context.errors +=
            ValidationError(
                code = issue.code(),
                component = component,
                path = path,
                message = message(component, path, issue),
                statementId = context.currentStatementId,
                isWarning = issue == ValidationIssue.NoRoot(NoRootReason.Fallback),
            )
    }

    // Schema introspection -----------------------------------------------------------------

    /** `{ $ref: "#/$defs/Name" }` → `Name`. */
    private fun refName(schema: OpenUIValue?): String? = schema["\$ref"].stringValue?.substringAfterLast('/')

    /** Composite shapes can't be reliably matched to a single plain value. */
    private fun isComposite(schema: OpenUIObject): Boolean =
        "\$ref" in schema || "anyOf" in schema || "oneOf" in schema || "allOf" in schema

    private class ScalarTypeInfo(val expectedType: String? = null, val enumValues: List<OpenUIValue>? = null)

    private fun scalarTypeInfo(schema: OpenUIObject): ScalarTypeInfo {
        schema["enum"].arrayValue?.let { return ScalarTypeInfo(enumValues = it) }
        schema["const"]?.let { return ScalarTypeInfo(enumValues = listOf(it)) }
        return when (schema["type"].stringValue) {
            "string" -> ScalarTypeInfo("string")
            "number", "integer" -> ScalarTypeInfo("number")
            "boolean" -> ScalarTypeInfo("boolean")
            else -> ScalarTypeInfo()
        }
    }

    /** The schema `default` of a property, if it carries one. */
    fun defaultValue(property: OpenUIValue?): OpenUIValue? = property.objectValue?.get("default")

    /** The expected type of a param, for signatures. */
    private fun typeFromSchema(property: OpenUIValue?): String? {
        val schema = property.objectValue ?: return null
        refName(property)?.let { return it }
        val leaf = scalarTypeInfo(schema)
        leaf.enumValues?.let { values -> return values.joinToString("|") { JS.jsonStringify(it) ?: "" } }
        leaf.expectedType?.let { return it }
        return schema["type"].stringValue
    }

    /** `Header(title*: string, variant: "a"|"b")`: `*` marks required, positional order kept. */
    fun paramsSignature(component: String, params: List<ParamDef>): String {
        val rendered = params.joinToString(", ") { param ->
            val type = typeFromSchema(param.schema)
            val marked = if (param.required) "${param.name}*" else param.name
            if (type != null) "$marked: $type" else marked
        }
        return "$component($rendered)"
    }

    private fun sameValueZero(a: OpenUIValue, b: OpenUIValue): Boolean = if (a is OpenUIValue.Num &&
        b is OpenUIValue.Num
    ) {
        a.value == b.value || (a.value.isNaN() && b.value.isNaN())
    } else {
        a == b
    }

    private fun typeMismatch(value: OpenUIValue, info: ScalarTypeInfo): ValidationIssue.TypeMismatch? {
        val actual = JS.typeOf(value)
        info.enumValues?.let { values ->
            if (actual != "string" && actual != "number" && actual != "boolean") return null
            if (values.any { sameValueZero(it, value) }) return null
            return ValidationIssue.TypeMismatch(
                expected = "one of [${values.joinToString(", ") { JS.jsonStringify(it) ?: "" }}]",
                actual = JS.jsonStringify(value) ?: "undefined",
            )
        }
        if (info.expectedType != null && info.expectedType != actual) {
            return ValidationIssue.TypeMismatch(info.expectedType, JS.jsType(value))
        }
        return null
    }

    // The edge rule --------------------------------------------------------------------------

    /** Invalid value: a default replaces it; otherwise a required edge is invalid, an optional one is dropped. */
    fun resolveInvalid(required: Boolean, defaultValue: OpenUIValue?): Checked = when {
        defaultValue != null -> Checked.Keep(defaultValue)
        required -> Checked.Invalid
        else -> Checked.Keep(null)
    }

    // Validators -----------------------------------------------------------------------------

    /**
     * Recursively validates a materialized value against a JSON Schema fragment. Objects and
     * arrays descend (reporting nested errors with JSON-pointer paths), scalar leaves check
     * type/enum, and component elements get a position check only.
     *
     * Every violation is reported, then the edge rule applies at each recursion edge: a schema
     * default substitutes; a required edge makes the parent invalid; an optional edge is pruned.
     * Array items are always pruned. Never mutates: returns the value itself when nothing changes.
     */
    fun validate(
        value: OpenUIValue?,
        schema: OpenUIValue?,
        component: String,
        path: String,
        context: MaterializeContext,
    ): Checked {
        val s = schema.objectValue ?: return Checked.Keep(value)
        // Absence is handled by required checks on the parent.
        if (value == null || value is OpenUIValue.Null) return Checked.Keep(value)
        // Dynamic runtime expressions resolve later.
        if (value is OpenUIValue.Expression) return Checked.Keep(value)
        // Child components: only their position is checked here.
        if (value is OpenUIValue.Element) {
            return if (validateElementPosition(value.node, s, component, path, context)) {
                Checked.Invalid
            } else {
                Checked.Keep(value)
            }
        }
        if (isComposite(s)) {
            // Data in a component-only slot is invalid.
            if (!isOnlyComponentSlot(s, context)) return Checked.Keep(value)
            push(
                context,
                component,
                path,
                ValidationIssue.TypeMismatch(
                    expected = slotRefNames(s).joinToString(" | ") { it ?: "" },
                    actual = when (value) {
                        is OpenUIValue.Arr -> "array"
                        is OpenUIValue.Obj -> "plain object"
                        else -> JS.typeOf(value)
                    },
                ),
            )
            return Checked.Invalid
        }
        return when (s["type"].stringValue) {
            "object" -> validateObject(value, s, component, path, context)
            "array" -> validateArray(value, s, component, path, context)
            else -> if (validateLeaf(value, s, component, path, context)) Checked.Invalid else Checked.Keep(value)
        }
    }

    /** A component in a slot that declares a plain-data shape is a type mismatch. */
    private fun validateElementPosition(
        element: ElementNode,
        s: OpenUIObject,
        component: String,
        path: String,
        context: MaterializeContext,
    ): Boolean {
        if (isComposite(s)) return false
        val type = s["type"].stringValue
        if (type != null || s["enum"] is OpenUIValue.Arr || "const" in s) {
            push(
                context,
                component,
                path,
                ValidationIssue.TypeMismatch(type ?: "a literal value", "component \"${element.typeName}\""),
            )
            return true
        }
        return false
    }

    /** The `$ref` names a slot allows: one for `$ref`, several for anyOf/oneOf. */
    private fun slotRefNames(s: OpenUIObject): List<String?> {
        val options = (s["anyOf"] ?: s["oneOf"])?.arrayValue ?: listOf(OpenUIValue.Obj(s))
        return options.map(::refName)
    }

    /** True when every option of the slot is a `$ref` to a component. */
    private fun isOnlyComponentSlot(s: OpenUIObject, context: MaterializeContext): Boolean {
        val names = slotRefNames(s)
        return names.isNotEmpty() && names.all { it != null && context.catalog?.contains(it) == true }
    }

    @Suppress("LoopWithTooManyJumpStatements", "NestedBlockDepth")
    private fun validateObject(
        value: OpenUIValue,
        s: OpenUIObject,
        component: String,
        path: String,
        context: MaterializeContext,
    ): Checked {
        val obj = value.objectValue ?: run {
            push(context, component, path, ValidationIssue.TypeMismatch("object", JS.jsType(value)))
            return Checked.Invalid
        }
        val properties = s["properties"].objectValue ?: OpenUIObject.Empty
        val required = s["required"].arrayValue?.mapNotNull { it.stringValue } ?: emptyList()
        var out: OpenUIObject.Builder? = null
        fun copy() = out ?: OpenUIObject.Builder().also { builder ->
            obj.forEach { (k, v) -> builder[k] = v }
            out = builder
        }
        var invalid = false

        // Structural checks are streaming-sensitive: only on complete input.
        if (!context.partial) {
            for (key in required) {
                val present = key in obj
                if (!present || obj[key] is OpenUIValue.Null) {
                    val fallback = defaultValue(properties[key])
                    if (fallback != null) {
                        copy()[key] = fallback
                        continue
                    }
                    push(
                        context,
                        component,
                        "$path/$key",
                        if (present) ValidationIssue.NullRequired() else ValidationIssue.MissingRequired(),
                    )
                    invalid = true
                }
            }
        }
        for ((key, sub) in properties) {
            if (key !in obj) continue
            val original = obj[key]
            var next = validate(original, sub, component, "$path/$key", context)
            if (next is Checked.Invalid) {
                next = resolveInvalid(key in required, defaultValue(sub))
                if (next is Checked.Invalid) {
                    invalid = true
                    continue
                }
            }
            val kept = (next as Checked.Keep).value
            if (kept === original) continue
            copy()[key] = kept
        }
        if (invalid) return Checked.Invalid
        return Checked.Keep(out?.let { OpenUIValue.Obj(it.build()) } ?: value)
    }

    private fun validateArray(
        value: OpenUIValue,
        s: OpenUIObject,
        component: String,
        path: String,
        context: MaterializeContext,
    ): Checked {
        val items = value.arrayValue ?: run {
            push(context, component, path, ValidationIssue.TypeMismatch("array", JS.jsType(value)))
            return Checked.Invalid
        }
        val itemSchema = s["items"]
        if (itemSchema !is OpenUIValue.Obj) return Checked.Keep(value)
        var out: MutableList<OpenUIValue>? = null
        items.forEachIndexed { index, item ->
            val next = validate(item, itemSchema, component, "$path/$index", context)
            val changed = next !is Checked.Keep || next.value !== item
            if (changed && out == null) out = items.subList(0, index).toMutableList()
            if (next is Checked.Keep) out?.add(next.value ?: OpenUIValue.Null)
        }
        return Checked.Keep(out?.let { OpenUIValue.Arr(it) } ?: value)
    }

    /** Enum membership defers while streaming; scalar type checks stay on. */
    private fun validateLeaf(
        value: OpenUIValue,
        s: OpenUIObject,
        component: String,
        path: String,
        context: MaterializeContext,
    ): Boolean {
        val leaf = scalarTypeInfo(s)
        if (leaf.expectedType == null && leaf.enumValues == null) return false
        if (leaf.enumValues != null && context.partial) return false
        val mismatch = typeMismatch(value, leaf) ?: return false
        push(context, component, path, mismatch)
        return true
    }
}
