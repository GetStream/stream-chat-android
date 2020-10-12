package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName

internal data class AddDeviceRequest(
    @SerializedName("id")
    val firebaseToken: String
) {
    val push_provider = "firebase"
}
