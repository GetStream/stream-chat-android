package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.model.dto.UpstreamMessageDto

/**
 * Represents the body part of the truncate channel request.
 *
 * @param message The system message that will be shown in the channel.
 */
@JsonClass(generateAdapter = true)
internal data class TruncateChannelRequest(
    val message: UpstreamMessageDto?,
)
