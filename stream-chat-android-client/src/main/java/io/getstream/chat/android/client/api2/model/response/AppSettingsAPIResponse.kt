package io.getstream.chat.android.client.api2.model.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class AppSettingsAPIResponse(
    @Json(name = "app") val appDto: AppDto
)

@JsonClass(generateAdapter = true)
public data class AppDto(
    @Json(name = "name") val name: String,
    @Json(name = "file_upload_config") val fileUploadConfigDto: FileUploadConfigDto,
    @Json(name = "image_upload_config") val imageUploadConfigDto: FileUploadConfigDto
)

@JsonClass(generateAdapter = true)
public data class FileUploadConfigDto(
    @Json(name = "allowed_file_extensions") val allowedFileExtensions: List<String>,
    @Json(name = "allowed_mime_types") val allowedMimeTypes: List<String>,
    @Json(name = "blocked_file_extensions") val blockedFileExtensions: List<String>,
    @Json(name = "blocked_mime_types") val blockedMimeTypes: List<String>,
)

@JsonClass(generateAdapter = true)
public data class ImageUploadConfigDto(
    @Json(name = "allowed_file_extensions") val allowedFileExtensions: List<String>,
    @Json(name = "allowed_mime_types") val allowedMimeTypes: List<String>,
    @Json(name = "blocked_file_extensions") val blockedFileExtensions: List<String>,
    @Json(name = "blocked_mime_types") val blockedMimeTypes: List<String>,
)
