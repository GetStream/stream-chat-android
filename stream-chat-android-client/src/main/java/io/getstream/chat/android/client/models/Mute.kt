package io.getstream.chat.android.client.models

import java.util.Date

public data class Mute(
    var user: User,
    var target: User,
    var createdAt: Date,
    var updatedAt: Date,
    val expires: Date?,
)
