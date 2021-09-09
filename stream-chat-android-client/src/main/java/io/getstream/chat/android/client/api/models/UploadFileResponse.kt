package io.getstream.chat.android.client.api.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class UploadFileResponse(val file: String)
