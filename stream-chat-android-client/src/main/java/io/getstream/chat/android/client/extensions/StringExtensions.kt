package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.core.internal.InternalStreamChatApi

private val snakeRegex = "_[a-zA-Z]".toRegex()
private val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()

/**
 * Converts string written in snake case to String in camel case with the first symbol in lower case.
 * For example string "created_at_some_time" is converted to "createdAtSomeTime".
 */
@InternalStreamChatApi
public fun String.snakeToLowerCamelCase(): String {
    return snakeRegex.replace(this) {
        it.value.replace("_", "").uppercase()
    }
}

/**
 * Converts String written in camel case to String in snake case.
 * For example string "createdAtSomeTime" is converted to "created_at_some_time".
 */
internal fun String.camelCaseToSnakeCase(): String {
    return camelRegex.replace(this) { "_${it.value}" }.lowercase()
}

internal fun String.isAnonymousChannelId(): Boolean = contains("!members")

/**
 * Parses CID of channel to channelType and channelId.
 *
 * @return Pair<String, String> Pair with channelType and channelId.
 * @throws IllegalStateException Throws an exception if format of cid is incorrect.
 */
@Throws(IllegalStateException::class)
public fun String.cidToTypeAndId(): Pair<String, String> {
    check(isNotEmpty()) { "cid can not be empty" }
    check(':' in this) { "cid needs to be in the format channelType:channelId. For example, messaging:123" }
    return checkNotNull(split(":").takeIf { it.size >= 2 }?.let { it.first() to it.last() })
}
