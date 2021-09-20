package io.getstream.chat.android.command.utils

fun changeModuleFileDoesNotExistInPath(path: String) {
    throw Exception(
        "The file $path does not exist. Please run \"./gradlew dag-command\" " +
            "and/or select the right path for the affected modules file."
    )
}
