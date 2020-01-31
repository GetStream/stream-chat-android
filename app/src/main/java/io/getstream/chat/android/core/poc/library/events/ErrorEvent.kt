package io.getstream.chat.android.core.poc.library.events

import io.getstream.chat.android.core.poc.library.EventType
import io.getstream.chat.android.core.poc.library.errors.ChatError

class ErrorEvent(val error: ChatError) : LocalEvent(EventType.CONNECTION_ERROR)