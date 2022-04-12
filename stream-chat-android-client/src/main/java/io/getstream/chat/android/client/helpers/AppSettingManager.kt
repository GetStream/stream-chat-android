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

package io.getstream.chat.android.client.helpers

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.models.App
import io.getstream.chat.android.client.models.AppSettings
import io.getstream.chat.android.client.models.FileUploadConfig

/**
 * Maintains application settings fetched from the backend.
 */
internal class AppSettingManager(private val chatApi: ChatApi) {

    /**
     * Application settings configured in the dashboard and fetched from the backend.
     */
    private var appSettings: AppSettings = createDefaultAppSettings()

    /**
     * Initializes [AppSettingManager] with application settings from the backend.
     */
    fun loadAppSettings() {
        chatApi.appSettings().enqueue {
            if (it.isSuccess) {
                this.appSettings = it.data()
            }
        }
    }

    /**
     * Returns application settings from the server or the default ones as a fallback.
     *
     * @return The application settings.
     */
    fun getAppSettings(): AppSettings = appSettings

    /**
     * Clears the application settings fetched from the backend.
     */
    fun clear() {
        appSettings = createDefaultAppSettings()
    }

    companion object {
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
                        blockedMimeTypes = emptyList()
                    ),
                    imageUploadConfig = FileUploadConfig(
                        allowedFileExtensions = emptyList(),
                        allowedMimeTypes = emptyList(),
                        blockedFileExtensions = emptyList(),
                        blockedMimeTypes = emptyList()
                    )
                )
            )
        }
    }
}
