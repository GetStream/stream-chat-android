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

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport",
)

package io.getstream.chat.android.network.models

import com.squareup.moshi.Json

/**
 *
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class SearchResponse(
    @Json(name = "duration")
    internal val duration: String,

    @Json(name = "results")
    internal val results: List<io.getstream.chat.android.network.models.SearchResult> = emptyList(),

    @Json(name = "next")
    internal val next: String? = null,

    @Json(name = "previous")
    internal val previous: String? = null,

    @Json(name = "results_warning")
    internal val resultsWarning: io.getstream.chat.android.network.models.SearchWarning? = null,
)
