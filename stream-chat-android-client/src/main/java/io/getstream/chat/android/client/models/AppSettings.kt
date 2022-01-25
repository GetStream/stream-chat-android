package io.getstream.chat.android.client.models

import com.squareup.moshi.JsonClass

/**
 * App settings, as they are configured in the dashboard.
 *
 * @param app [App] The configurations of the app.
 */
public data class AppSettings(
    val app: App
)

/**
 * The representation of the app, with its configurations.
 *
 * @param name The name of the app.
 * @param fileUploadConfig [FileUploadConfig] The configuration of file uploads.
 * @param imageUploadConfig [FileUploadConfig] The configuration of image uploads.
 */
@JsonClass(generateAdapter = true)
public data class App(
    val name: String,
    val fileUploadConfig: FileUploadConfig,
    val imageUploadConfig: FileUploadConfig
)

/**
 * The configuration of file upload.
 *
 * @param allowedFileExtensions Allowed file extensions.
 * @param allowedFileExtensions Allowed mime types.
 * @param blockedFileExtensions Blocked mime types.
 * @param blockedMimeTypes Blocked mime types.
 */
@JsonClass(generateAdapter = true)
public data class FileUploadConfig(
    val allowedFileExtensions: List<String>,
    val allowedMimeTypes: List<String>,
    val blockedFileExtensions: List<String>,
    val blockedMimeTypes: List<String>,
)

/**
 * The configuration of image upload.
 *
 * @param allowedFileExtensions Allowed file extensions.
 * @param allowedFileExtensions Allowed mime types.
 * @param blockedFileExtensions Blocked mime types.
 * @param blockedMimeTypes Blocked mime types.
 */
@JsonClass(generateAdapter = true)
public data class ImageUploadConfig(
    val allowedFileExtensions: List<String>,
    val allowedMimeTypes: List<String>,
    val blockedFileExtensions: List<String>,
    val blockedMimeTypes: List<String>,
)
