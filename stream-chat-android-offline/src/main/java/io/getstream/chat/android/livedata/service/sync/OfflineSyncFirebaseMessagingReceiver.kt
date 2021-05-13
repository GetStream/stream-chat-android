package io.getstream.chat.android.livedata.service.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import io.getstream.chat.android.client.logger.ChatLogger

internal class OfflineSyncFirebaseMessagingHandler() {

    private val logger = ChatLogger.get("OfflineSyncFirebaseMessagingReceiver")

    fun syncMessages(context: Context, cid: String) {
        val syncModule = SyncProvider(context)
        val syncConfig = syncModule.encryptedBackgroundSyncConfigStore.get()

        if (syncConfig != null) {
            logger.logD("Starting the sync, config: $syncConfig")

            performSync(
                context = context,
                apiKey = syncConfig.apiKey,
                userId = syncConfig.userId,
                userToken = syncConfig.userToken,
                cid = cid
            )
        } else {
            logger.logE("Bad configuration. Either SyncProvider either EXTRA_CID are null")
        }
    }

    companion object {
        private const val SYNC_MESSAGES_WORK_NAME = "SYNC_MESSAGES_WORK_NAME"
    }

    private fun performSync(context: Context, apiKey: String, userId: String, userToken: String, cid: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_ROAMING)
            .build()

        val syncMessagesWork = OneTimeWorkRequestBuilder<SyncMessagesWork>()
            .setConstraints(constraints)
            .setInputData(
                workDataOf(
                    SyncMessagesWork.DATA_CID to cid,
                    SyncMessagesWork.DATA_USER_ID to userId,
                    SyncMessagesWork.DATA_API_KEY to apiKey,
                    SyncMessagesWork.DATA_USER_TOKEN to userToken,
                )
            )
            .build()

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(
                SYNC_MESSAGES_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                syncMessagesWork
            )
    }
}
