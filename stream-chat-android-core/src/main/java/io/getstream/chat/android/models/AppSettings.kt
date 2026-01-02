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

package io.getstream.chat.android.models

import androidx.compose.runtime.Immutable

/**
 * App settings, as they are configured in the dashboard.
 *
 * @param app [App] The configurations of the app.
 */
@Immutable
public data class AppSettings(
    val app: App,
) {
    public companion object {
        /**
         * Default file size limit in bytes.
         */
        public const val DEFAULT_SIZE_LIMIT_IN_BYTES: Long = 100 * 1024 * 1024
    }
}

/**
 * The representation of the app, with its configurations.
 *
 * @param name The name of the app.
 * @param fileUploadConfig [FileUploadConfig] The configuration of file uploads.
 * @param imageUploadConfig [FileUploadConfig] The configuration of image uploads.
 */
@Immutable
public data class App(
    val name: String,
    val fileUploadConfig: FileUploadConfig,
    val imageUploadConfig: FileUploadConfig,
)

/**
 * The configuration of file upload.
 *
 * @param allowedFileExtensions Allowed file extensions.
 * @param allowedMimeTypes Allowed mime types.
 * @param blockedFileExtensions Blocked file extensions.
 * @param blockedMimeTypes Blocked mime types.
 * @param sizeLimitInBytes The size limit of the file in bytes.
 */
@Immutable
public data class FileUploadConfig(
    val allowedFileExtensions: List<String>,
    val allowedMimeTypes: List<String>,
    val blockedFileExtensions: List<String>,
    val blockedMimeTypes: List<String>,
    val sizeLimitInBytes: Long,
)
