package io.getstream.chat.android.command.utils

fun <T> List<T>.generateGradleCommand(commandFunction: (T) -> String): String {
    val command = joinToString(separator = " ", transform = commandFunction)

    return "./gradlew $command"
}
