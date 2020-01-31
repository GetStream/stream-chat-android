package io.getstream.chat.android.client.rest

import io.getstream.chat.android.client.Message


data class UpdateChannelRequest(val data: Map<String, Any>, val message: Message)