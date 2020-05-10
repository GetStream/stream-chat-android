package io.getstream.chat.android.livedata.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain

class SyncWorker(var context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    private val logger = ChatLogger.get("ChatDomain SyncWorker")

    override suspend fun doWork(): Result {
        // 1. Loads syncState from the room DB
        // 2. Queries events for all active channels
        // 3. Sync those events to room DB
        val cid = inputData.getString("STREAM_CHANNEL_CID")!!
        val userId = inputData.getString("STREAM_USER_ID")!!
        logger.logI("SyncWorker.doWork running for channel $cid and user $userId")

        //val chatDomain = ChatDomain.Builder(context, ChatClient.instance(), User(userId)).recoveryDisabled().build()
        val chatDomain = ChatDomain.instance()

        val result = chatDomain.useCases.replayEventsForActiveChannels(cid).execute()

        return if (result.isSuccess) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}
