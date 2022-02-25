package io.getstream.chat.android.offline.service.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.event.EventHandlerProvider
import io.getstream.chat.android.offline.utils.validateCid

@ExperimentalStreamChatApi
internal class SyncMessagesWork(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    private val logger = ChatLogger.get("SyncMessagesWork")

    override suspend fun doWork(): Result {
        val cid = inputData.getString(DATA_CID)!!

        try {
            validateCid(cid)
        } catch (ex: IllegalArgumentException) {
            return Result.failure()
        }

        val eventHandlerImpl = EventHandlerProvider.get()
        eventHandlerImpl.addNewChannelToReplayEvents(cid)
        val result = eventHandlerImpl.replyEventsForActiveChannels()

        return if (result.isSuccess) {
            logger.logD("Sync success.")
            Result.success()
        } else {
            logger.logD("Sync failed.")
            Result.retry()
        }
    }

    companion object {
        private const val DATA_CID = "DATA_CID"
        private const val SYNC_MESSAGES_WORK_NAME = "SYNC_MESSAGES_WORK_NAME"

        fun start(context: Context, cid: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_ROAMING)
                .build()

            val syncMessagesWork = OneTimeWorkRequestBuilder<SyncMessagesWork>()
                .setConstraints(constraints)
                .setInputData(workDataOf(DATA_CID to cid))
                .build()

            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(
                    SYNC_MESSAGES_WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    syncMessagesWork,
                )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(SYNC_MESSAGES_WORK_NAME)
        }
    }
}
