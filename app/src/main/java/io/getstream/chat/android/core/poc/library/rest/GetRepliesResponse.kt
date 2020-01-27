package io.getstream.chat.android.core.poc.library.rest

import io.getstream.chat.android.core.poc.library.Message


data class GetRepliesResponse(val messages: List<Message> = emptyList())
