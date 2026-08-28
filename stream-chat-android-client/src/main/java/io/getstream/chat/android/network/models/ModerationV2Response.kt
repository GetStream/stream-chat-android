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
internal data class ModerationV2Response(
    @Json(name = "action")
    internal val action: String,

    @Json(name = "original_text")
    internal val originalText: String,

    @Json(name = "blocklist_matched")
    internal val blocklistMatched: String? = null,

    @Json(name = "platform_circumvented")
    internal val platformCircumvented: Boolean? = null,

    @Json(name = "semantic_filter_matched")
    internal val semanticFilterMatched: String? = null,

    @Json(name = "blocklists_matched")
    internal val blocklistsMatched: List<String>? = emptyList(),

    @Json(name = "image_harms")
    internal val imageHarms: List<String>? = emptyList(),

    @Json(name = "text_harms")
    internal val textHarms: List<String>? = emptyList(),
)
