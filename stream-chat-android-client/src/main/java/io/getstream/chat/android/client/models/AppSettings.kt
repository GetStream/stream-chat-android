package io.getstream.chat.android.client.models

import com.squareup.moshi.JsonClass

public data class AppSettings(
    val app: App
)

@JsonClass(generateAdapter = true)
public data class App(
    val name: String,
    val fileUploadConfig: FileUploadConfig,
    val imageUploadConfig: FileUploadConfig
)

@JsonClass(generateAdapter = true)
public data class FileUploadConfig(
    val allowedFileExtensions: List<String>,
    val allowedMimeTypes: List<String>,
    val blockedFileExtensions: List<String>,
    val blockedMimeTypes: List<String>,
)

@JsonClass(generateAdapter = true)
public data class ImageUploadConfig(
    val allowedFileExtensions: List<String>,
    val allowedMimeTypes: List<String>,
    val blockedFileExtensions: List<String>,
    val blockedMimeTypes: List<String>,
)
