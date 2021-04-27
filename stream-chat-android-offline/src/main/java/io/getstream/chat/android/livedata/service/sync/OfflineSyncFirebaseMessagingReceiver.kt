package io.getstream.chat.android.livedata.service.sync

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import io.getstream.chat.android.client.logger.ChatLogger

internal class OfflineSyncFirebaseMessagingReceiver : BroadcastReceiver() {

    private val logger = ChatLogger.get("OfflineSyncFirebaseMessagingReceiver")

    override fun onReceive(context: Context, intent: Intent) {
        val syncModule = SyncProvider(context)
        val cid = intent.getStringExtra(EXTRA_CID)

        val syncConfig = syncModule.encryptedBackgroundSyncConfigStore.get()

        if (syncConfig != null && cid != null) {
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
        const val EXTRA_CID: String = "EXTRA_CID"
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
