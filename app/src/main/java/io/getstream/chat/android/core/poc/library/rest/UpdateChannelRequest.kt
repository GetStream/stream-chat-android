package io.getstream.chat.android.core.poc.library.rest

import io.getstream.chat.android.core.poc.library.Message


data class UpdateChannelRequest(val data: Map<String, Any>, val message: Message)