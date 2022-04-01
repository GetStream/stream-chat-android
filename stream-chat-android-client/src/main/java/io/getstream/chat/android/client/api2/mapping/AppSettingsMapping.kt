package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.response.AppDto
import io.getstream.chat.android.client.api2.model.response.AppSettingsResponse
import io.getstream.chat.android.client.api2.model.response.FileUploadConfigDto
import io.getstream.chat.android.client.models.App
import io.getstream.chat.android.client.models.AppSettings
import io.getstream.chat.android.client.models.FileUploadConfig

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
