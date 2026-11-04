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

package io.getstream.chat.android.ai.openui.rendering

/** The sizes of OpenUI's components that no theme token covers. */
internal object OpenUIDimens {
    /** The corner radius of an icon badge, as a share of its size. */
    const val IconBadgeCornerRatio: Float = 0.28f

    /** The size of the icon in an icon badge, as a share of the badge's size. */
    const val IconBadgeIconRatio: Float = 0.5f

    /** The height, in dp, of a chart inside a card. */
    const val CardChartHeight: Int = 180

    /** The least height, in dp, of a horizontal bar chart. */
    const val BarChartMinHeight: Int = 160

    /** The most height, in dp, of a horizontal bar chart. */
    const val BarChartMaxHeight: Int = 1000

    /** The height, in dp, of each category of a horizontal bar chart. */
    const val BarRowHeight: Int = 34

    /** The height, in dp, a horizontal bar chart adds for its axis. */
    const val BarChartAxisHeight: Int = 40

    /** The width of a bar, as a share of its category's band. */
    const val BarWidthRatio: Float = 0.6f

    /** The width of a group of bars side by side, as a share of its category's band. */
    const val BarGroupWidthRatio: Float = 0.7f

    /** The gap between bars side by side, as a share of a bar's width. */
    const val BarGapRatio: Float = 0.15f

    /** The most width category labels take beside horizontal bars, as a share of the chart. */
    const val BarLabelMaxWidthRatio: Float = 0.4f

    /** The hole of a donut, as a share of its radius. */
    const val DonutHoleRatio: Float = 0.58f

    /** The area, in square dp, of the smallest bubble of a scatter chart. */
    const val BubbleMinArea: Double = 30.0

    /** How much area, in square dp, the largest bubble adds to the smallest. */
    const val BubbleAreaRange: Double = 170.0

    /** The area, in square dp, of a point without a size. */
    const val BubbleArea: Double = 50.0

    /** How far a radar chart's labels sit from its center, as a share of its radius. */
    const val RadarLabelRadiusRatio: Double = 1.18

    /** The most width of a radar chart's label, as a share of the chart. */
    const val RadarLabelMaxWidthRatio: Float = 1f / 3f
}
