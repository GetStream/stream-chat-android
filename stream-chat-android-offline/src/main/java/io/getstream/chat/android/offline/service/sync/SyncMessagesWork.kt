package io.getstream.chat.android.offline.service.sync

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.livedata.ChatDomain

internal class SyncMessagesWork(
    appContext: Context,
    workerParams: WorkerParameters,
) : Worker(appContext, workerParams) {

    private val logger = ChatLogger.get("SyncMessagesWork")

    override fun doWork(): Result {
        val cid = inputData.getString(DATA_CID)!!
        val result = ChatDomain.instance().replayEventsForActiveChannels(cid).execute()

        return if (result.isSuccess) {
            logger.logD("Sync success.")

            Result.success()
        } else {
            logger.logD("Sync failed.")

            Result.retry()
        }
    }

    companion object {
        const val DATA_CID = "DATA_CID"
        const val DATA_USER_ID = "DATA_USER_ID"
    }
}
