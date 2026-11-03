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

import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ChartDataTest {

    private fun num(value: Double) = OpenUIValue.Num(value)
    private fun str(value: String) = OpenUIValue.Str(value)

    @Test
    fun `values a chart can't draw are left out of series and count as nothing in slices`() {
        val rows = listOf(
            OpenUIValue.Arr(listOf(str("Mon"), num(1.0))),
            OpenUIValue.Arr(listOf(str("Tue"), num(Double.POSITIVE_INFINITY))),
            OpenUIValue.Arr(listOf(str("Wed"), num(1e300))),
        )
        val (points, series) = ChartData.points(listOf(str("Day"), str("Visits")), rows)

        assertEquals(listOf("Visits"), series)
        assertEquals(listOf(1.0), points.map { it.value }.filter { it.isFinite() })

        val slices = ChartData.slices(listOf(str("a"), str("b")), listOf(num(2.0), num(Double.NaN)))
        assertEquals(listOf(2.0, 0.0), slices.map { it.value })
    }

    @Test
    fun `chart heights are finite, positive and at most 1000`() {
        assertNull(chartHeight(null))
        assertNull(chartHeight(-1.0))
        assertNull(chartHeight(Double.POSITIVE_INFINITY))
        assertEquals(1000.0, chartHeight(1e9))
        assertEquals(180.0, chartHeight(180.0))
    }

    @Test
    fun `axis ticks always cover the values`() {
        assertEquals(listOf(0.0, 20.0, 40.0, 60.0, 80.0, 100.0), niceTicks(0.0, 100.0))
        assertEquals(listOf(0.0, 1.0), niceTicks(-1e308, 1e308))
        assertTrue(niceTicks(5.0, 5.0).isNotEmpty())
    }
}
