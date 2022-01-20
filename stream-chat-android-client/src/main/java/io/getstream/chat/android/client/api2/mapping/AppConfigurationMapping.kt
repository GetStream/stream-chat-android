package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.response.AppDto
import io.getstream.chat.android.client.api2.model.response.AppSettingsAPIResponse
import io.getstream.chat.android.client.api2.model.response.FileUploadConfigDto
import io.getstream.chat.android.client.api2.model.response.ImageUploadConfigDto
import io.getstream.chat.android.client.models.App
import io.getstream.chat.android.client.models.AppSettings
import io.getstream.chat.android.client.models.FileUploadConfig
import io.getstream.chat.android.client.models.ImageUploadConfig

internal fun AppSettingsAPIResponse.toDomain(): AppSettings {
    return AppSettings(
        app = appDto.toDomain()
    )
}

internal fun AppDto.toDomain(): App {
    return App(
        name = name,
        fileUploadConfig = fileUploadConfigDto.toDomain(),
        imageUploadConfig = imageUploadConfigDto.toDomain(),
    )
}

internal fun FileUploadConfigDto.toDomain(): FileUploadConfig {
    return FileUploadConfig(
        allowedFileExtensions = allowedFileExtensions,
        allowedMimeTypes = allowedMimeTypes,
        blockedFileExtensions = blockedFileExtensions,
        blockedMimeTypes = blockedMimeTypes,
    )
}

internal fun ImageUploadConfigDto.toDomain(): ImageUploadConfig {
    return ImageUploadConfig(
        allowedFileExtensions = allowedFileExtensions,
        allowedMimeTypes = allowedMimeTypes,
        blockedFileExtensions = blockedFileExtensions,
        blockedMimeTypes = blockedMimeTypes,
    )
}
