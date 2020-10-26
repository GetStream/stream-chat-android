package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.client.models.Channel

private val snakeRegex = "_[a-zA-Z]".toRegex()

/**
 * turns created_at into createdAt
 */
public fun String.snakeToLowerCamelCase(): String {
    return snakeRegex.replace(this) {
        it.value.replace("_", "")
            .toUpperCase()
    }
}

public val test: List<Channel> = listOf<Channel>().sortedBy(Channel::lastUpdated)