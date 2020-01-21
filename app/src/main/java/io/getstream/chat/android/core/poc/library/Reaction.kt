package io.getstream.chat.android.core.poc.library

import java.util.*


class Reaction(
    var messageId: String,
    val user: User,
    var userID: String,
    val type: String,
    val createdAt: Date,
    val extraData: Map<String, Any>
)
