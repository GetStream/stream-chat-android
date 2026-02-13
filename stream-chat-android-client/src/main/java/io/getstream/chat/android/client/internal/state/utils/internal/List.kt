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
 * This implementation uses lazy allocation - if no elements match the filter, the original list
 * reference is returned without any allocation. This is important for StateFlow usage where
 * returning the same reference prevents unnecessary emissions.
 *
 * @param filter A predicate function that determines whether an element should be updated.
 * @param update A function that takes an element and returns its updated version.
 * @return The original list if no elements match the filter, or a new list with updated elements.
 */
internal inline fun <T> List<T>.updateIf(filter: (T) -> Boolean, update: (T) -> T): List<T> {
    var result: MutableList<T>? = null
    for (i in indices) {
        val item = this[i]
        if (filter(item)) {
            if (result == null) {
                result = toMutableList()
            }
            result[i] = update(item)
        }
    }
    return result ?: this
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

/**
 * Merges two sorted arrays while maintaining the sort order and handling duplicates.
 *
 * This function combines two pre-sorted lists into a single sorted list while resolving duplicate
 * elements. When duplicate elements are detected (elements with the same ID as determined by
 * [idSelector]), the element from the [other] list takes precedence.
 *
 * **Note:** Both lists must be pre-sorted according to the provided [comparator]. If either list is
 * not sorted, the behavior is undefined.
 *
 * @param T The type of elements in the lists.
 * @param other The second sorted list to merge with this list.
 * @param idSelector A function that extracts a unique identifier from an element. This is used to
 *   detect duplicate elements across the two lists.
 * @param comparator The comparator that both lists are sorted by and used for merging.
 * @return A new sorted list containing all elements from both lists, with duplicates resolved by
 *   taking elements from the [other] list.
 */
internal fun <T> List<T>.mergeSorted(
    other: List<T>,
    idSelector: (T) -> String,
    comparator: Comparator<in T>,
): List<T> {
    // Create a set of IDs from the other list for quick duplicate detection
    val otherIds = other.mapTo(mutableSetOf(), idSelector)

    // Filter this list to exclude elements that exist in other (by ID)
    val filteredThis = this.filterNot { idSelector(it) in otherIds }

    // Now merge the filtered list with other using standard merge algorithm
    val result = mutableListOf<T>()
    var i = 0
    var j = 0

    while (i < filteredThis.size && j < other.size) {
        val thisElement = filteredThis[i]
        val otherElement = other[j]
        val comparison = comparator.compare(thisElement, otherElement)

        when {
            comparison <= 0 -> {
                // thisElement comes before or is equal to otherElement
                result.add(thisElement)
                i++
            }

            else -> {
                // otherElement comes before thisElement
                result.add(otherElement)
                j++
            }
        }
    }

    // Add remaining elements from filteredThis
    while (i < filteredThis.size) {
        result.add(filteredThis[i])
        i++
    }

    // Add remaining elements from other
    while (j < other.size) {
        result.add(other[j])
        j++
    }

    return result.toImmutableList()
}
