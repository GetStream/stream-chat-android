package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.Message

internal data class UpdateChannelRequest(val data: Map<String, Any>, val message: Message?)
