package io.getstream.chat.android.offline.service.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import io.getstream.chat.android.client.logger.ChatLogger

internal class OfflineSyncFirebaseMessagingHandler {

    private val logger = ChatLogger.get("OfflineSyncFirebaseMessagingReceiver")

    fun syncMessages(context: Context, cid: String) {

        logger.logD("Starting the sync")

        performSync(context = context, cid = cid)
    }

    companion object {
        private const val SYNC_MESSAGES_WORK_NAME = "SYNC_MESSAGES_WORK_NAME"
    }

    private fun performSync(context: Context, cid: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_ROAMING)
            .build()

        val syncMessagesWork = OneTimeWorkRequestBuilder<SyncMessagesWork>()
            .setConstraints(constraints)
            .setInputData(workDataOf(SyncMessagesWork.DATA_CID to cid))
            .build()

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(
                SYNC_MESSAGES_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                syncMessagesWork,
            )
    }
}
