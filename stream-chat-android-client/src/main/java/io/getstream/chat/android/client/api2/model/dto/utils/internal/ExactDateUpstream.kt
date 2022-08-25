package io.getstream.chat.android.client.api2.model.dto.utils.internal

import java.util.Date

internal data class ExactDate(
    internal val date: Date,
    internal val rawDate: String,
)

internal data class ExactDateUpstream(
    internal val date: Date,
    internal val rawDate: String?,
)
