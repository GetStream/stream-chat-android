package io.getstream.chat.android.client.models

data class Command(
    val name: String,
    val description: String,
    val args: String,
    val set: String
)
