package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.Message


data class UpdateChannelRequest(val data: Map<String, Any>, val message: Message)