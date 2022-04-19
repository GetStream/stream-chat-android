/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.api2.model.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class AppSettingsResponse(
    @Json(name = "app") val app: AppDto,
)

@JsonClass(generateAdapter = true)
internal data class AppDto(
    @Json(name = "name") val name: String,
    @Json(name = "file_upload_config") val fileUploadConfig: FileUploadConfigDto,
    @Json(name = "image_upload_config") val imageUploadConfig: FileUploadConfigDto,
)

@JsonClass(generateAdapter = true)
internal data class FileUploadConfigDto(
    @Json(name = "allowed_file_extensions") val allowedFileExtensions: List<String>,
    @Json(name = "allowed_mime_types") val allowedMimeTypes: List<String>,
    @Json(name = "blocked_file_extensions") val blockedFileExtensions: List<String>,
    @Json(name = "blocked_mime_types") val blockedMimeTypes: List<String>,
)
