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
 * A type representing the app settings.
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class AppResponseFields(
    @Json(name = "async_url_enrich_enabled")
    internal val asyncUrlEnrichEnabled: Boolean,

    @Json(name = "auto_translation_enabled")
    internal val autoTranslationEnabled: Boolean,

    @Json(name = "id")
    internal val id: Int,

    @Json(name = "name")
    internal val name: String,

    @Json(name = "placement")
    internal val placement: String,

    // Patched: the spec marks `defaults` required, but backends before v239.68.0 (CHA-5968, chat#18441) leave it out,
    // and a parse failure fails the whole app settings response. On regen, make it required again only once every
    // region runs v239.68.0 or later.
    @Json(name = "defaults")
    internal val defaults: io.getstream.chat.android.network.models.AppDefaults? = null,

    @Json(name = "file_upload_config")
    internal val fileUploadConfig: io.getstream.chat.android.network.models.FileUploadConfig,

    @Json(name = "image_upload_config")
    internal val imageUploadConfig: io.getstream.chat.android.network.models.FileUploadConfig,
)
