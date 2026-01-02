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

package io.getstream.chat.android.client.api2.model.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class AppSettingsResponse(
    val app: AppDto,
)

@JsonClass(generateAdapter = true)
internal data class AppDto(
    val name: String,
    val file_upload_config: FileUploadConfigDto,
    val image_upload_config: FileUploadConfigDto,
)

@JsonClass(generateAdapter = true)
internal data class FileUploadConfigDto(
    val allowed_file_extensions: List<String>,
    val allowed_mime_types: List<String>,
    val blocked_file_extensions: List<String>,
    val blocked_mime_types: List<String>,
    val size_limit: Long?,
)
