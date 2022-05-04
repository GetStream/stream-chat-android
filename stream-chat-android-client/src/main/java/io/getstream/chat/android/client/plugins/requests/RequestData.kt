package io.getstream.chat.android.client.plugins.requests

import java.util.Date

internal data class RequestData(
    val name: String,
    val time: Date,
    val extraData: Map<String, String>
)
