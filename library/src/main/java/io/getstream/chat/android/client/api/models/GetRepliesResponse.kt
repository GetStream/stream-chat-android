package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.Message


data class GetRepliesResponse(val messages: List<Message> = emptyList())
