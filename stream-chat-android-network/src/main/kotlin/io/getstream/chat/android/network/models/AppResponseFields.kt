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
 *
 */

data class AppResponseFields (
    @Json(name = "async_url_enrich_enabled")
    val asyncUrlEnrichEnabled: kotlin.Boolean,

    @Json(name = "auto_translation_enabled")
    val autoTranslationEnabled: kotlin.Boolean,

    @Json(name = "id")
    val id: kotlin.Int,

    @Json(name = "name")
    val name: kotlin.String,

    @Json(name = "placement")
    val placement: kotlin.String,

    @Json(name = "file_upload_config")
    val fileUploadConfig: io.getstream.chat.android.network.models.FileUploadConfig,

    @Json(name = "image_upload_config")
    val imageUploadConfig: io.getstream.chat.android.network.models.FileUploadConfig
)
