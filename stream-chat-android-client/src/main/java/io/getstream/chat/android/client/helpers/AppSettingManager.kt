package io.getstream.chat.android.client.helpers

import io.getstream.chat.android.client.models.App
import io.getstream.chat.android.client.models.AppSettings
import io.getstream.chat.android.client.models.FileUploadConfig

/**
 * Maintains application settings fetched from the backend.
 */
internal class AppSettingManager {

    /**
     * Application settings configured in the dashboard and fetched from the backend.
     */
    private var appSettings: AppSettings = createDefaultAppSettings()

    /**
     * Initializes [AppSettingManager] with application settings from the backend.
     *
     * @param appSettings The application settings to set.
     */
    fun setAppSettings(appSettings: AppSettings) {
        this.appSettings = appSettings
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
