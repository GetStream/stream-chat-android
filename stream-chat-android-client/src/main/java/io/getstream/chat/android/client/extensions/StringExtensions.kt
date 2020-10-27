package io.getstream.chat.android.client.extensions

private val snakeRegex = "_[a-zA-Z]".toRegex()
private val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()

/**
 * turns created_at into createdAt
 */
public fun String.snakeToLowerCamelCase(): String {
    return snakeRegex.replace(this) {
        it.value.replace("_", "")
            .toUpperCase()
    }
}

/**
 * turns createdAt into created_at
 */
public fun String.camelCaseToSnakeCase(): String {
    return camelRegex.replace(this) {
        it.value.replace("_", "")
            .toUpperCase()
    }
}
