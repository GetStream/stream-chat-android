package io.getstream.chat.android.client.api.models


class AddDeviceRequest(val id: String, var user_id: String) {
    val push_provider = "firebase"
}
