package io.getstream.chat.android.client.models

public data class Command(
    val name: String,
    val description: String,
    val args: String,
    val set: String
)
