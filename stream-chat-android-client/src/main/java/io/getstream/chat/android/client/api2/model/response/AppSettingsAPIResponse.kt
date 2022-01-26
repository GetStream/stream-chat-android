package io.getstream.chat.android.client.api2.model.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class AppSettingsAPIResponse(val appDto: AppDto)

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
)

@JsonClass(generateAdapter = true)
internal data class ImageUploadConfigDto(
    val allowed_file_extensions: List<String>,
    val allowed_mime_types: List<String>,
    val blocked_file_extensions: List<String>,
    val blocked_mime_types: List<String>,
)
