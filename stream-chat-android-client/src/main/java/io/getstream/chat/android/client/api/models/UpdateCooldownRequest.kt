package io.getstream.chat.android.client.api.models

internal data class UpdateCooldownRequest(
    val set: Map<String, Any>,
) {
    companion object {
        fun create(cooldownTimeInSeconds: Int): UpdateCooldownRequest {
            return UpdateCooldownRequest(mapOf("cooldown" to cooldownTimeInSeconds))
        }
    }
}
