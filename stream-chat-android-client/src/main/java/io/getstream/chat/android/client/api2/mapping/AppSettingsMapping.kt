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

package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.response.AppDto
import io.getstream.chat.android.client.api2.model.response.AppSettingsResponse
import io.getstream.chat.android.client.api2.model.response.FileUploadConfigDto
import io.getstream.chat.android.models.App
import io.getstream.chat.android.models.AppSettings
import io.getstream.chat.android.models.FileUploadConfig

internal fun AppSettingsResponse.toDomain(): AppSettings {
    return AppSettings(
        app = app.toDomain()
    )
}

internal fun AppDto.toDomain(): App {
    return App(
        name = name,
        fileUploadConfig = file_upload_config.toDomain(),
        imageUploadConfig = image_upload_config.toDomain(),
    )
}

internal fun FileUploadConfigDto.toDomain(): FileUploadConfig {
    return FileUploadConfig(
        allowedFileExtensions = allowed_file_extensions,
        allowedMimeTypes = allowed_mime_types,
        blockedFileExtensions = blocked_file_extensions,
        blockedMimeTypes = blocked_mime_types,
    )
}
