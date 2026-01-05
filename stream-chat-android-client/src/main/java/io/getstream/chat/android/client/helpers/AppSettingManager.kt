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

package io.getstream.chat.android.client.helpers

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.models.App
import io.getstream.chat.android.models.AppSettings
import io.getstream.chat.android.models.FileUploadConfig
import io.getstream.log.StreamLog
import io.getstream.result.Result
import io.getstream.result.extractCause

/**
 * Maintains application settings fetched from the backend.
 */
internal class AppSettingManager(private val chatApi: ChatApi) {

    /**
     * Application settings configured in the dashboard and fetched from the backend.
     */
    private var appSettings: AppSettings? = null

    /**
     * Initializes [AppSettingManager] with application settings from the backend.
     */
    fun loadAppSettings() {
        if (appSettings == null) {
            chatApi.appSettings().enqueue { result ->
                if (result is Result.Success) {
                    this.appSettings = result.value
                } else if (result is Result.Failure) {
                    when (val cause = result.value.extractCause()) {
                        null -> StreamLog.e(TAG) { "[loadAppSettings] failed: ${result.value}" }
                        else -> StreamLog.e(TAG, cause) { "[loadAppSettings] failed: ${result.value}" }
                    }
                }
            }
        }
    }

    /**
     * Returns application settings from the server or the default ones as a fallback.
     *
     * @return The application settings.
     */
    fun getAppSettings(): AppSettings = appSettings ?: createDefaultAppSettings()

    /**
     * Clears the application settings fetched from the backend.
     */
    fun clear() {
        appSettings = null
    }

    companion object {
        private const val TAG = "Chat:AppSettingManager"

        /**
         * Builds the default application settings with the reasonable defaults.
         */
        fun createDefaultAppSettings(): AppSettings {
            return AppSettings(
                app = App(
                    name = "",
                    fileUploadConfig = FileUploadConfig(
                        allowedFileExtensions = emptyList(),
                        allowedMimeTypes = emptyList(),
                        blockedFileExtensions = emptyList(),
                        blockedMimeTypes = emptyList(),
                        sizeLimitInBytes = AppSettings.DEFAULT_SIZE_LIMIT_IN_BYTES,
                    ),
                    imageUploadConfig = FileUploadConfig(
                        allowedFileExtensions = emptyList(),
                        allowedMimeTypes = emptyList(),
                        blockedFileExtensions = emptyList(),
                        blockedMimeTypes = emptyList(),
                        sizeLimitInBytes = AppSettings.DEFAULT_SIZE_LIMIT_IN_BYTES,
                    ),
                ),
            )
        }
    }
}
