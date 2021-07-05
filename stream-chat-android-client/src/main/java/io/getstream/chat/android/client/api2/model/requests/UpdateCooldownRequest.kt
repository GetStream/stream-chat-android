package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class UpdateCooldownRequest(
    val set: Map<String, Any>,
) {
    companion object {
        fun create(cooldownTimeInSeconds: Int): UpdateCooldownRequest {
            return UpdateCooldownRequest(mapOf("cooldown" to cooldownTimeInSeconds))
        }
    }
}
