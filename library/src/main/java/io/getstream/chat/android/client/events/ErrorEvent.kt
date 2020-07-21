package io.getstream.chat.android.client.events

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.EventType

class ErrorEvent(val error: ChatError) : ChatEvent(EventType.CONNECTION_ERROR)
