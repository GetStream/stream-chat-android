package io.getstream.chat.android.client.events

import io.getstream.chat.android.client.EventType
import io.getstream.chat.android.client.errors.ChatError

class ErrorEvent(val error: ChatError) : LocalEvent(EventType.CONNECTION_ERROR)