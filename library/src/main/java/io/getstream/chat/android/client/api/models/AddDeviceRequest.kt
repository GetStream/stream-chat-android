package io.getstream.chat.android.client.api.models


data class AddDeviceRequest(val firebaseToken: String) {
    val push_provider = "firebase"
}
