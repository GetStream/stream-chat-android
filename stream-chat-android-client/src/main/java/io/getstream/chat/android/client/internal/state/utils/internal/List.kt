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

package io.getstream.chat.android.client.internal.state.utils.internal

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

/**
 * Updates elements in the list that match the given [filter] by applying the [update] function.
 * Elements that don't match the filter remain unchanged.
 *
 * @param filter A predicate function that determines whether an element should be updated.
 * @param update A function that takes an element and returns its updated version.
 * @return A new list containing the updated elements. Elements that did not match the filter are
 *   included unchanged.
 */
internal inline fun <T> List<T>.updateIf(filter: (T) -> Boolean, update: (T) -> T): List<T> = map {
    if (filter(it)) update(it) else it
}

private fun <T> MutableList<T>.toImmutableList(): List<T> {
    return this.toList()
}

/**
 * Inserts an element into a mutable list while maintaining sorted order.
 *
 * This function uses binary search to find the correct insertion point for the new element,
 * ensuring that the list remains sorted according to the provided [comparator]. The element is
 * inserted even if an equal element already exists in the list.
 *
 * **Note:** This function assumes that the list is already sorted according to the provided
 * comparator. If the list is not sorted, the behavior is undefined.
 *
 * @param T The type of elements in the list.
 * @param element The element to insert into the list.
 * @param comparator The comparator used to determine the sort order and insertion point.
 * @return A new immutable list containing all original elements plus the inserted element in the
 *   correct sorted position.
 */
internal fun <T> MutableList<T>.insertSorted(element: T, comparator: Comparator<in T>): List<T> {
    val insertionPoint = this.binarySearch(element, comparator)
    val index =
        if (insertionPoint >= 0) {
            insertionPoint + 1
        } else {
            -(insertionPoint + 1)
        }
    this.add(index, element)
    return this.toImmutableList()
}

/**
 * Updates an existing element in a sorted list or inserts a new one while maintaining sort order.
 *
 * This function performs an upsert operation by finding an element with the same ID (as determined
 * by the [idSelector]) and replacing it with the new [element]. If no matching element is found,
 * the new [element] is inserted in the correct sorted position. This ensures that the list remains
 * sorted according to the provided [comparator].
 *
 * The operation returns a new immutable list, leaving the original list unchanged.
 *
 * **Note:** This function assumes that the list is already sorted according to the provided
 * comparator. If the list is not sorted, the behavior is undefined.
 *
 * @param T The type of elements in the list.
 * @param element The element to insert or use for updating an existing element.
 * @param idSelector A function that extracts a unique identifier from an element. This is used to
 *   determine if an element already exists in the list.
 * @param comparator The comparator used to determine the sort order and insertion point.
 * @param update A function that takes the existing element and returns the updated element. Used
 *   only if an existing element is found.
 * @return A new sorted list containing the upserted element. If an existing element was found, it
 *   will be updated and repositioned; otherwise, the new element will be inserted in the correct
 *   sorted position as-is.
 */
internal fun <T, ID> List<T>.upsertSorted(
    element: T,
    idSelector: (T) -> ID,
    comparator: Comparator<in T>,
    update: (old: T) -> T = { element },
): List<T> {
    val elementId = idSelector(element)
    val existingIndex = this.indexOfFirst { idSelector(it) == elementId }

    return if (existingIndex >= 0) {
        // Element exists - check if sort order has changed
        val updatedElement = update(this[existingIndex])
        val sortComparison = comparator.compare(this[existingIndex], updatedElement)

        if (sortComparison == 0) {
            // Sort order hasn't changed - replace in place to preserve position
            this.toMutableList().apply { this[existingIndex] = updatedElement }
        } else {
            // Sort order has changed - remove and insert at correct position
            this.toMutableList()
                .apply { removeAt(existingIndex) }
                .insertSorted(updatedElement, comparator)
        }
    } else {
        // Element doesn't exist - insert at correct position
        this.toMutableList().insertSorted(element, comparator)
    }
}

/**
 * Upserts an element into a sorted list and enforces a maximum size, keeping the last [maxSize]
 * elements (newest by comparator order).
 *
 * This function combines [upsertSorted] with a size constraint. After the upsert operation,
 * if the resulting list exceeds [maxSize], the oldest elements (those at the beginning of the
 * sorted list) are removed to maintain the size limit.
 *
 * @param T The type of elements in the list.
 * @param ID The type of the unique identifier for elements.
 * @param element The element to insert or use for updating an existing element.
 * @param maxSize The maximum number of elements to retain. Must be positive.
 * @param idSelector A function that extracts a unique identifier from an element.
 * @param comparator The comparator used to determine the sort order.
 * @return A new sorted list containing at most [maxSize] elements, with the newest elements retained.
 */
internal fun <T, ID> List<T>.upsertSortedBounded(
    element: T,
    maxSize: Int,
    idSelector: (T) -> ID,
    comparator: Comparator<in T>,
): List<T> {
    val result = upsertSorted(element, idSelector, comparator)
    return if (result.size > maxSize) result.takeLast(maxSize) else result
}
