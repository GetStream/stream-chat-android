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

package io.getstream.chat.android.ai.openui.library

import io.getstream.chat.android.ai.openui.lang.OpenUIJson
import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/** Prop types: their JSON Schemas, prompt signatures and component references. */
internal class PropTypeTest {
    private fun schema(type: PropType): String = OpenUIJson.stringify(type.jsonSchema)

    private val nested = PropType.objectOf(
        listOf(
            PropField.required("title", PropType.string),
            PropField.optional("icon", PropType.component("Icon")),
            PropField.defaulted("size", PropType.enumeration(listOf("s", "m")), "m"),
        ),
    )

    @Test
    fun `primitive types map to their JSON Schemas and signatures`() {
        assertEquals("""{"type":"string"}""", schema(PropType.string))
        assertEquals("""{"type":"number"}""", schema(PropType.number))
        assertEquals("""{"type":"boolean"}""", schema(PropType.boolean))
        assertEquals("{}", schema(PropType.any))
        assertEquals("""{"${'$'}ref":"#/${'$'}defs/ActionExpression"}""", schema(PropType.action))
        assertEquals("""{"type":"string","enum":["a","b"]}""", schema(PropType.enumeration(listOf("a", "b"))))
        assertEquals("""{"type":"string","const":"x"}""", schema(PropType.literal("x")))
        assertEquals(
            listOf("string", "number", "boolean", "any", "ActionExpression", "\"a\" | \"b\"", "\"x\""),
            listOf(
                PropType.string,
                PropType.number,
                PropType.boolean,
                PropType.any,
                PropType.action,
                PropType.enumeration(listOf("a", "b")),
                PropType.literal("x"),
            ).map { it.signature },
        )
    }

    @Test
    fun `composite types map to their JSON Schemas and signatures`() {
        assertEquals(
            """{"minItems":1,"type":"array","items":{"type":"string"}}""",
            schema(PropType.array(PropType.string, minItems = 1)),
        )
        assertEquals("""{"type":"array","items":{"type":"number"}}""", schema(PropType.array(PropType.number)))
        assertEquals(
            """{"type":"object","propertyNames":{"type":"string"},"additionalProperties":{"type":"number"}}""",
            schema(PropType.record(PropType.number)),
        )
        assertEquals(
            """{"anyOf":[{"${'$'}ref":"#/${'$'}defs/A"},{"${'$'}ref":"#/${'$'}defs/B"}]}""",
            schema(PropType.components(listOf("A", "B"))),
        )
        assertEquals(
            """{"anyOf":[{"${'$'}ref":"#/${'$'}defs/OpenUrl"},{"${'$'}ref":"#/${'$'}defs/Copy"}]}""",
            schema(PropType.actions(listOf("OpenUrl", "Copy"))),
        )
        assertEquals(
            """{"type":"object","properties":{"title":{"type":"string"},"icon":{"${'$'}ref":"#/${'$'}defs/Icon"},""" +
                """"size":{"default":"m","type":"string","enum":["s","m"]}},"required":["title","size"],""" +
                """"additionalProperties":false}""",
            schema(nested),
        )

        assertEquals("(A | B)[]", PropType.array(PropType.components(listOf("A", "B"))).signature)
        assertEquals("string[]", PropType.array(PropType.string).signature)
        assertEquals("Record<string, number>", PropType.record(PropType.number).signature)
        assertEquals("@OpenUrl | @Copy", PropType.actions(listOf("OpenUrl", "Copy")).signature)
        assertEquals("{title: string, icon?: Icon, size?: \"s\" | \"m\"}", nested.signature)
        assertEquals("Icon", PropType.component("Icon").toString())
    }

    @Test
    fun `references list components and action definitions in declaration order`() {
        val type = PropType.union(
            listOf(
                PropType.array(PropType.component("A")),
                PropType.record(PropType.component("B")),
                nested,
                PropType.action,
                PropType.actions(listOf("Copy")),
                PropType.string,
            ),
        )
        assertEquals(
            listOf(
                PropType.Definition.Component("A"),
                PropType.Definition.Component("B"),
                PropType.Definition.Component("Icon"),
                PropType.Definition.Action("ActionExpression"),
                PropType.Definition.Action("Copy"),
            ),
            type.definitionReferences,
        )
        assertEquals(listOf("A", "B", "Icon"), type.componentReferences)
    }

    @Test
    fun `removing components keeps a union that would be left empty`() {
        val union = PropType.components(listOf("A", "B"))
        assertEquals(PropType.union(listOf(PropType.component("B"))), union.removingComponents(setOf("A")))
        assertEquals(union, union.removingComponents(setOf("A", "B")))
        assertEquals(
            PropType.array(PropType.union(listOf(PropType.component("B"))), minItems = 2),
            PropType.array(union, minItems = 2).removingComponents(setOf("A")),
        )
        assertEquals(
            PropType.record(PropType.union(listOf(PropType.component("B")))),
            PropType.record(union).removingComponents(setOf("A")),
        )
        assertEquals(
            PropType.objectOf(listOf(PropField.required("child", PropType.union(listOf(PropType.component("B")))))),
            PropType.objectOf(listOf(PropField.required("child", union))).removingComponents(setOf("A")),
        )
        assertEquals(PropType.string, PropType.string.removingComponents(setOf("A")))
    }

    @Test
    fun `needed components are the ones a slot cannot do without`() {
        val removed = setOf("A", "B")
        assertEquals(listOf("A"), PropType.component("A").neededComponents(removed))
        assertEquals(listOf("A", "B"), PropType.components(listOf("A", "B")).neededComponents(removed))
        assertEquals(emptyList(), PropType.components(listOf("A", "C")).neededComponents(removed))
        assertEquals(
            emptyList(),
            PropType.union(listOf(PropType.component("A"), PropType.string)).neededComponents(removed),
        )
        assertEquals(
            listOf("B"),
            PropType.union(listOf(PropType.string, PropType.array(PropType.component("B")))).neededComponents(removed),
        )
        assertEquals(listOf("A"), PropType.array(PropType.component("A")).neededComponents(removed))
        assertEquals(listOf("A"), PropType.record(PropType.component("A")).neededComponents(removed))
        assertEquals(
            listOf("A"),
            PropType.objectOf(listOf(PropField.required("a", PropType.component("A")))).neededComponents(removed),
        )
        assertEquals(emptyList(), PropType.string.neededComponents(removed))
    }

    @Test
    fun `prop types compare by kind`() {
        assertEquals(PropType.array(PropType.string), PropType.array(PropType.string))
        assertEquals(PropType.array(PropType.string).hashCode(), PropType.array(PropType.string).hashCode())
        assertNotEquals(PropType.array(PropType.string), PropType.array(PropType.number))
        assertNotEquals<Any>(PropType.string, "string")
    }

    @Test
    fun `prop fields describe their signature and schema`() {
        assertEquals("a: string", PropField.required("a", PropType.string).signature)
        assertEquals("a?: string", PropField.optional("a", PropType.string).signature)
        assertEquals("a?: ${'$'}binding<string>", PropField.reactive("a", PropType.string).signature)
        assertEquals(
            """{"default":2,"type":"number"}""",
            OpenUIJson.stringify(PropField.defaulted("a", PropType.number, 2).jsonSchema),
        )
        assertEquals(
            """{"default":2}""",
            OpenUIJson.stringify(PropField.defaulted("a", PropType.any, 2).jsonSchema),
        )
    }

    @Test
    fun `component schemas describe their props`() {
        val component = ComponentSchema(
            name = "Card",
            description = "A card",
            props = listOf(PropField.required("title", PropType.string), PropField.reactive("open", PropType.boolean)),
        )
        assertEquals("Card(title: string, open?: ${'$'}binding<boolean>)", component.signature)
        assertEquals(setOf("open"), component.reactiveProps)
        assertEquals(
            """{"type":"object","properties":{"title":{"type":"string"},"open":{"type":"boolean"}},""" +
                """"required":["title"],"additionalProperties":false,"description":"A card"}""",
            OpenUIJson.stringify(component.jsonSchema),
        )
        val updated = component.updatingProp("title") { it.copy(isOptional = true) }
        assertEquals("Card(title?: string, open?: ${'$'}binding<boolean>)", updated.signature)
    }

    @Test
    fun `built-in action shapes are closed objects`() {
        val shapes = listOf(PropType.openUrlSchema, PropType.toAssistantSchema)
        shapes.forEach { assertEquals(OpenUIValue.False, (it as OpenUIValue.Obj).fields["additionalProperties"]) }
        val anyOf = (PropType.actionSchema as OpenUIValue.Obj).fields["anyOf"] as OpenUIValue.Arr
        assertEquals(3, anyOf.items.size)
    }

    @Test
    fun `functions and actions describe their signatures and step shapes`() {
        val percent = defineFunction(
            name = "Percent",
            description = "Share",
            params = listOf(PropField.required("part", PropType.number), PropField.required("total", PropType.number)),
            returns = PropType.number,
        ) { OpenUIValue.Null }
        assertEquals("Percent(part: number, total: number) → number", percent.signature)
        assertEquals("Share", percent.description)
        val noResult = defineFunction("Now", "Time", emptyList()) { OpenUIValue.Null }
        assertEquals("Now()", noResult.signature)

        val copy = defineAction("Copy", "Copies", listOf(PropField.required("text", PropType.string)))
        assertEquals("Copy(text: string)", copy.signature)
        assertEquals(
            """{"type":"object","properties":{"type":{"type":"string","const":"Copy"},"params":{"type":"object",""" +
                """"properties":{"text":{"type":"string"}},"required":["text"],"additionalProperties":false}},""" +
                """"required":["type","params"],"additionalProperties":false}""",
            OpenUIJson.stringify(copy.stepSchema),
        )
    }
}
