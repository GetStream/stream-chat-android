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

package io.getstream.chat.android.ai.openui.components

import io.getstream.chat.android.ai.openui.lang.FormValidation
import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.lang.ParsedRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class FormSupportTest {

    private fun s(value: String) = OpenUIValue.Str(value)
    private fun arr(vararg values: String) = OpenUIValue.Arr(values.map(OpenUIValue::Str))

    @Test
    fun `normalize single takes the first stored value then the default`() {
        assertEquals(listOf("a"), SelectionField.normalize(true, s("a"), s("b")))
        assertEquals(listOf("a"), SelectionField.normalize(true, arr("a", "b"), null))
        assertEquals(listOf("b"), SelectionField.normalize(true, null, s("b")))
        assertEquals(emptyList(), SelectionField.normalize(true, s(""), s("b")))
        assertEquals(emptyList(), SelectionField.normalize(true, null, null))
    }

    @Test
    fun `normalize multiple keeps every string`() {
        assertEquals(listOf("a", "b"), SelectionField.normalize(false, arr("a", "b"), null))
        assertEquals(listOf("a"), SelectionField.normalize(false, s("a"), null))
        assertEquals(listOf("x"), SelectionField.normalize(false, null, arr("x")))
        assertEquals(
            listOf("a"),
            SelectionField.normalize(false, OpenUIValue.Arr(listOf(s("a"), OpenUIValue.Num(1.0))), null),
        )
    }

    @Test
    fun `stored default is shaped for the selection mode`() {
        assertEquals(s("a"), SelectionField.storedDefault(true, arr("a", "b")))
        assertNull(SelectionField.storedDefault(true, s("")))
        assertEquals(arr("a"), SelectionField.storedDefault(false, s("a")))
        assertEquals(arr(), SelectionField.storedDefault(false, s("")))
        assertNull(SelectionField.storedDefault(false, OpenUIValue.Num(1.0)))
    }

    @Test
    fun `toggle selects and deselects`() {
        assertEquals(listOf("b"), SelectionField.toggle(true, listOf("a"), "b"))
        assertEquals(emptyList(), SelectionField.toggle(true, listOf("a"), "a"))
        assertEquals(listOf("a", "b"), SelectionField.toggle(false, listOf("a"), "b"))
        assertEquals(listOf("b"), SelectionField.toggle(false, listOf("a", "b"), "a"))
    }

    @Test
    fun `validate form checks every registered field and unwraps form entries`() {
        val model = FormValidationModel()
        var email: OpenUIValue? = OpenUIValue.obj("value" to s(""), "componentType" to s("Input"))
        model.register("email", listOf(ParsedRule("required"))) { email }
        model.register("name", listOf(ParsedRule("required"))) { s("Ana") }

        assertFalse(model.validateForm())
        assertNotNull(model.error("email"))
        assertNull(model.error("name"))
        assertTrue(model.hasErrors)

        email = OpenUIValue.obj("value" to s("a@b.co"), "componentType" to s("Input"))
        assertTrue(model.validateForm())
        assertNull(model.error("email"))
    }

    @Test
    fun `validate field and clear error`() {
        val model = FormValidationModel()
        assertFalse(model.validateField("age", s(""), listOf(ParsedRule("required"))))
        assertNotNull(model.error("age"))
        model.clearError("age")
        assertNull(model.error("age"))
        assertTrue(model.validateField("age", s("3"), listOf(ParsedRule("required"))))
    }

    @Test
    fun `unregistered fields are not validated`() {
        val model = FormValidationModel()
        model.register("x", listOf(ParsedRule("required"))) { null }
        model.unregister("x")
        assertTrue(model.validateForm())
    }

    @Test
    fun `slider rules check its first value`() {
        val numeric = FormValidation.builtInValidators.getValue("numeric")
        val stored = OpenUIValue.Arr(listOf(OpenUIValue.Num(5.0), OpenUIValue.Num(10.0)))

        assertEquals(OpenUIValue.Num(5.0), SliderValues.validated(stored))
        assertNull(numeric(SliderValues.validated(stored), null))
        assertEquals(OpenUIValue.Num(3.0), SliderValues.validated(OpenUIValue.Num(3.0)))
    }

    @Test
    fun `slider bounds that aren't finite or leave no range fall back`() {
        assertEquals(0.0..100.0, SliderValues.bounds(null, null))
        assertEquals(0.0..100.0, SliderValues.bounds(Double.NaN, Double.POSITIVE_INFINITY))
        assertEquals(0.0..100.0, SliderValues.bounds(-1e300, 1e300))
        assertEquals(10.0..11.0, SliderValues.bounds(10.0, 5.0))
    }

    @Test
    fun `slider steps are positive, finite and within the range`() {
        val bounds = 0.0..10.0
        assertEquals(2.0, SliderValues.step(2.0, null, bounds))
        assertNull(SliderValues.step(0.0, null, bounds))
        assertNull(SliderValues.step(Double.POSITIVE_INFINITY, null, bounds))
        assertNull(SliderValues.step(20.0, "continuous", bounds))
        assertEquals(1.0, SliderValues.step(-1.0, "discrete", bounds))
        assertEquals(4, SliderValues.tickCount(bounds, 2.0))
        assertEquals(0, SliderValues.tickCount(0.0..1e6, 1.0))
    }

    @Test
    fun `slider values are one or two finite numbers within the bounds, lower first`() {
        val bounds = 0.0..10.0
        assertEquals(listOf(3.0), SliderValues.values(OpenUIValue.Arr(listOf(OpenUIValue.Num(3.0))), bounds))
        assertEquals(listOf(4.0), SliderValues.values(OpenUIValue.Num(4.0), bounds))
        assertEquals(listOf(5.0), SliderValues.values(s("5"), bounds))
        assertEquals(listOf(0.0), SliderValues.values(null, bounds))
        assertEquals(listOf(10.0), SliderValues.values(OpenUIValue.Num(99.0), bounds))
        assertEquals(listOf(0.0), SliderValues.values(OpenUIValue.Num(Double.NaN), bounds))
        val range = OpenUIValue.Arr(listOf(OpenUIValue.Num(8.0), OpenUIValue.Num(2.0), OpenUIValue.Num(5.0)))
        assertEquals(listOf(2.0, 8.0), SliderValues.values(range, bounds))
    }

    @Test
    fun `slider values snap to the step and store rounded`() {
        assertEquals(4.0, SliderValues.snapped(3.4, 0.0..10.0, 2.0))
        assertEquals(10.0, SliderValues.snapped(12.0, 0.0..10.0, null))
        assertEquals(
            OpenUIValue.Arr(listOf(OpenUIValue.Num(0.333), OpenUIValue.Num(2.0))),
            SliderValues.stored(listOf(1.0 / 3, 2.0)),
        )
    }

    @Test
    fun `text areas keep between 2 and 100 rows`() {
        assertEquals(3, textAreaRows(null))
        assertEquals(3, textAreaRows(Double.NaN))
        assertEquals(2, textAreaRows(1.0))
        assertEquals(100, textAreaRows(1e9))
        assertEquals(6, textAreaRows(6.0))
    }

    @Test
    fun `a field error names its first broken rule`() {
        val rules = listOf(ParsedRule("required"), ParsedRule("minLength", OpenUIValue.Num(3.0)))
        assertEquals("required", FieldError.of(s(""), rules)?.rule?.type)
        assertEquals("minLength", FieldError.of(s("ab"), rules)?.rule?.type)
        assertNull(FieldError.of(s("abc"), rules))
    }

    @Test
    fun `dates round trip as utc days`() {
        val millis = FormDateFormatting.millis("2026-11-02")!!
        assertEquals("2026-11-02", FormDateFormatting.string(millis))
        assertEquals(millis, FormDateFormatting.millis("2026-11-02T10:00:00Z"))
        assertNull(FormDateFormatting.millis("nope"))
        assertNull(FormDateFormatting.millis(""))
    }
}
