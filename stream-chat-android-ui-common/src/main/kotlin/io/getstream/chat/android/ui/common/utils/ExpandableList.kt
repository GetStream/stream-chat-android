/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.common.utils

import io.getstream.chat.android.core.ExperimentalStreamChatApi

/**
 * A read-only, immutable list wrapper that supports expandable and collapsible behavior.
 *
 * This class behaves like a standard [List] but adds metadata and logic to control visibility of its elements
 * based on a specified [minimumVisibleItems] threshold and current [isCollapsed] state.
 *
 * ### Behavior:
 * - When [isCollapsed] is `true` and the total item count exceeds [minimumVisibleItems], only the first
 *   [minimumVisibleItems] items are exposed through the list interface. The remaining items are considered collapsed.
 * - When [isCollapsed] is `false`, all items are exposed.
 * - If the total number of items is less than or equal to [minimumVisibleItems], all items are shown,
 *   and the list is not expandable.
 *
 * @param T the type of elements contained in the list.
 * @param items the full list of items to wrap.
 * @param minimumVisibleItems the minimum number of items to show when collapsed.
 *        Defaults to the full list size (no collapsing by default).
 * @param isCollapsed whether the list is currently collapsed (hiding excess items).
 *
 * @property canExpand `true` if the list contains more items than [minimumVisibleItems] and can be collapsed/expanded.
 * @property collapsedCount the number of items currently hidden due to collapsing,
 * or `0` if fully expanded or not expandable.
 */
@ExperimentalStreamChatApi
public data class ExpandableList<T>(
    private val items: List<T>,
    private val minimumVisibleItems: Int = items.size,
    val isCollapsed: Boolean = true,
) : List<T> by visibleItems(items, minimumVisibleItems, isCollapsed) {

    /**
     * Indicates whether the list can be expanded or collapsed,
     * i.e., when the number of items exceeds [minimumVisibleItems].
     */
    val canExpand: Boolean = canExpand(items, minimumVisibleItems)

    /**
     * The number of items that are currently collapsed (not visible).
     * Returns `0` if the list is expanded or cannot be expanded.
     */
    val collapsedCount: Int =
        if (isCollapsed && canExpand) {
            items.size - minimumVisibleItems
        } else {
            0
        }
}

/**
 * Creates an empty read-only [ExpandableList] instance.
 */
public fun <T> emptyExpandableList(): ExpandableList<T> = ExpandableList(items = emptyList())

/**
 * Returns `true` if the list has more items than the [minimumVisibleItems],
 * indicating that it is eligible to be collapsed or expanded.
 */
private fun canExpand(
    items: List<*>,
    minimumVisibleItems: Int,
): Boolean = items.size > minimumVisibleItems

/**
 * Returns the visible portion of the list based on the collapsed state.
 * If collapsed and expandable, returns only the first [minimumVisibleItems] items;
 * otherwise, returns the full list.
 */
private fun <T> visibleItems(
    items: List<T>,
    minimumVisibleItems: Int,
    isCollapsed: Boolean,
): List<T> =
    if (isCollapsed && canExpand(items, minimumVisibleItems)) {
        items.take(minimumVisibleItems)
    } else {
        items
    }
