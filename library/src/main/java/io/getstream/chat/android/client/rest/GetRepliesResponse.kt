package io.getstream.chat.android.client.rest

import io.getstream.chat.android.client.Message


data class GetRepliesResponse(val messages: List<Message> = emptyList())
