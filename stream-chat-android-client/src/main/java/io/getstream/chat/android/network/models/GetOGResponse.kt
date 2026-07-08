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
internal data class GetOGResponse(
    @Json(name = "duration")
    internal val duration: String,

    @Json(name = "custom")
    internal val custom: Map<String, Any?> = emptyMap(),

    @Json(name = "asset_url")
    internal val assetUrl: String? = null,

    @Json(name = "author_icon")
    internal val authorIcon: String? = null,

    @Json(name = "author_link")
    internal val authorLink: String? = null,

    @Json(name = "author_name")
    internal val authorName: String? = null,

    @Json(name = "color")
    internal val color: String? = null,

    @Json(name = "fallback")
    internal val fallback: String? = null,

    @Json(name = "footer")
    internal val footer: String? = null,

    @Json(name = "footer_icon")
    internal val footerIcon: String? = null,

    @Json(name = "image_url")
    internal val imageUrl: String? = null,

    @Json(name = "og_scrape_url")
    internal val ogScrapeUrl: String? = null,

    @Json(name = "original_height")
    internal val originalHeight: Int? = null,

    @Json(name = "original_width")
    internal val originalWidth: Int? = null,

    @Json(name = "pretext")
    internal val pretext: String? = null,

    @Json(name = "text")
    internal val text: String? = null,

    @Json(name = "thumb_url")
    internal val thumbUrl: String? = null,

    @Json(name = "title")
    internal val title: String? = null,

    @Json(name = "title_link")
    internal val titleLink: String? = null,

    @Json(name = "type")
    internal val type: String? = null,

    @Json(name = "actions")
    internal val actions: List<io.getstream.chat.android.network.models.Action>? = emptyList(),

    @Json(name = "fields")
    internal val fields: List<io.getstream.chat.android.network.models.Field>? = emptyList(),

    @Json(name = "giphy")
    internal val giphy: io.getstream.chat.android.network.models.Images? = null,
)
