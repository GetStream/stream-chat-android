/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-video-android/blob/main/LICENSE
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
    "UnusedImport"
)

package io.getstream.chat.android.network.models

import com.squareup.moshi.Json

/**
 * An attachment is a message object that represents a file uploaded by a user.
 */

data class Attachment (
    @Json(name = "custom")
    val custom: kotlin.collections.Map<kotlin.String, Any?> = emptyMap(),

    @Json(name = "asset_url")
    val assetUrl: kotlin.String? = null,

    @Json(name = "author_icon")
    val authorIcon: kotlin.String? = null,

    @Json(name = "author_link")
    val authorLink: kotlin.String? = null,

    @Json(name = "author_name")
    val authorName: kotlin.String? = null,

    @Json(name = "color")
    val color: kotlin.String? = null,

    @Json(name = "fallback")
    val fallback: kotlin.String? = null,

    @Json(name = "footer")
    val footer: kotlin.String? = null,

    @Json(name = "footer_icon")
    val footerIcon: kotlin.String? = null,

    @Json(name = "image_url")
    val imageUrl: kotlin.String? = null,

    @Json(name = "og_scrape_url")
    val ogScrapeUrl: kotlin.String? = null,

    @Json(name = "original_height")
    val originalHeight: kotlin.Int? = null,

    @Json(name = "original_width")
    val originalWidth: kotlin.Int? = null,

    @Json(name = "pretext")
    val pretext: kotlin.String? = null,

    @Json(name = "text")
    val text: kotlin.String? = null,

    @Json(name = "thumb_url")
    val thumbUrl: kotlin.String? = null,

    @Json(name = "title")
    val title: kotlin.String? = null,

    @Json(name = "title_link")
    val titleLink: kotlin.String? = null,

    @Json(name = "type")
    val type: kotlin.String? = null,

    @Json(name = "actions")
    val actions: kotlin.collections.List<io.getstream.chat.android.network.models.Action>? = emptyList(),

    @Json(name = "fields")
    val fields: kotlin.collections.List<io.getstream.chat.android.network.models.Field>? = emptyList(),

    @Json(name = "giphy")
    val giphy: io.getstream.chat.android.network.models.Images? = null
)
