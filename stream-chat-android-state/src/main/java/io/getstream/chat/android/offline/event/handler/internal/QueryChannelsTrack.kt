package io.getstream.chat.android.offline.event.handler.internal

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.logging.StreamLog
import io.getstream.logging.TaggedLogger
import java.util.Date

internal class QueryChannelsTrack {

    private val logger: TaggedLogger = StreamLog.getLogger("SyncEventsTrack")

    private var lastSyncedDate: Date? = null

    internal fun isLastSyncBeforeEvent(chatEvent: ChatEvent): Boolean {
        return lastSyncedDate
            .also { lastSync ->
                logger.d {
                    "Last sync: $lastSync, event date: ${chatEvent.createdAt}"
                }
            }
            ?.before(chatEvent.createdAt)
            .also { isBefore ->
                logger.d {
                    "is last sync before event: $isBefore"
                }
            }?: false
    }

    internal fun markSynced(date: Date) {
        lastSyncedDate = date
    }
}
