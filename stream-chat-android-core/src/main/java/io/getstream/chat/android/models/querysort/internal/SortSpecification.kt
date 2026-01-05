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

package io.getstream.chat.android.models.querysort.internal

import io.getstream.chat.android.models.querysort.SortDirection

/**
 * Defines a sorting specification for collections of elements of type [T].
 *
 * @param T The type of elements to be sorted.
 * @param sortAttribute The attribute to be used for sorting.
 * @param sortDirection The direction of sorting.
 */
public data class SortSpecification<T>(
    val sortAttribute: SortAttribute<T>,
    val sortDirection: SortDirection,
)
