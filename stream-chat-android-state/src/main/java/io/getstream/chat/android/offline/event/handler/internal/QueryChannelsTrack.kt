package io.getstream.chat.android.offline.event.handler.internal

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.logging.StreamLog
import io.getstream.logging.TaggedLogger
import java.util.Date

internal class QueryChannelsTrack {

    private var lastSyncedDate: Date? = null

    internal fun isLastSyncBeforeEvent(chatEvent: ChatEvent): Boolean {
        return lastSyncedDate?.before(chatEvent.createdAt) ?: false
    }

    internal fun markSynced(date: Date) {
        lastSyncedDate = date
    }
}
