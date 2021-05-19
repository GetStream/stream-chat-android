package io.getstream.chat.android.offline.service.sync

import android.content.Context
import io.getstream.chat.android.client.logger.ChatLogger

internal class OfflineSyncFirebaseMessagingHandler {
    private val logger = ChatLogger.get("OfflineSyncFirebaseMessagingHandler")

    fun syncMessages(context: Context, cid: String) {
        logger.logD("Starting the sync")

        SyncMessagesWork.start(context, cid)
    }

    fun cancel(context: Context) {
        SyncMessagesWork.cancel(context)
    }
}
